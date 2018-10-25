package org.hswebframework.task.scheduler;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.task.*;
import org.hswebframework.task.events.*;
import org.hswebframework.task.job.JobDetail;
import org.hswebframework.task.job.JobRepository;
import org.hswebframework.task.lock.Lock;
import org.hswebframework.task.lock.LockManager;
import org.hswebframework.task.scheduler.history.ScheduleHistory;
import org.hswebframework.task.scheduler.history.ScheduleHistoryRepository;
import org.hswebframework.task.worker.TaskWorker;
import org.hswebframework.task.worker.TaskWorkerManager;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
public class DefaultTaskScheduler implements TaskScheduler {

    @Getter
    @Setter
    private boolean autoShutdown = true;

    @Getter
    @Setter
    private String schedulerId;

    @Getter
    @Setter
    private JobRepository jobRepository;

    @Getter
    @Setter
    private TaskWorkerManager taskWorkerManager;

    @Getter
    @Setter
    private TaskFactory taskFactory;

    @Getter
    @Setter
    private TaskRepository taskRepository;

    @Getter
    @Setter
    private ScheduleHistoryRepository historyRepository;

    @Getter
    @Setter
    private SchedulerFactory schedulerFactory;

    @Getter
    @Setter
    private EventPublisher eventPublisher;

    @Getter
    @Setter
    private LockManager lockManager;

    private volatile boolean startup;

    private Date startupTime;

    private Map<String, RunningScheduler> runningSchedulerMap = new ConcurrentHashMap<>();

    class RunningScheduler {
        private volatile Scheduler scheduler;
        private volatile String    scheduleId;
        private volatile String    historyId;

        private volatile AtomicLong errorCounter = new AtomicLong();

        void cancel(boolean force) {
            scheduler.cancel(force);
        }
    }

    public void shutdown(boolean force) {
        for (RunningScheduler runningScheduler : runningSchedulerMap.values()) {
            runningScheduler.scheduler.stop(force);
        }
        lockManager.releaseALl();
        log.debug("shutdown scheduler {}", this);
    }


    public void startup() {
        if (startup) {
            throw new UnsupportedOperationException("task already startup with " + startupTime);
        }
        if (autoShutdown) {
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownNow));
        }
        startupTime = new Date();
        startup = true;
        //重启未执行的任务
        List<ScheduleHistory> histories = historyRepository
                .findBySchedulerId(getSchedulerId(), SchedulerStatus.running, SchedulerStatus.pause);
        log.debug("start up scheduler {}, auto start {} tasks", this, histories.size());
        for (ScheduleHistory history : histories) {
            Scheduler scheduler = schedulerFactory.create(history.getSchedulerConfiguration());
            Task task = taskRepository.findById(history.getTaskId());
            doSchedule(task, history.getId(), scheduler);
        }
    }

    protected void logExecuteResult(TaskOperationResult result) {
        log.debug("task complete :{} use time {}ms", result, result.getEndTime() - result.getStartTime());
    }

    protected void changeTaskStatus(Task task, TaskStatus taskStatus) {
        eventPublisher.publish(new TaskStatusChangedEvent(task.getStatus(), taskStatus, task));
        taskRepository.changeStatus(task.getId(), taskStatus);
        task.setStatus(taskStatus);
        log.debug("task [{}] status changed : {} ", task.getId(), taskStatus);
    }

    protected void doSchedule(Task task, String historyId, Scheduler scheduler) {
        String group = task.getJob().getGroup();
        boolean parallel = task.getJob().isParallel();

        RunningScheduler runningScheduler = new RunningScheduler();
        runningScheduler.scheduleId = task.getScheduleId();
        runningScheduler.historyId = historyId;
        runningSchedulerMap.put(task.getScheduleId(), runningScheduler);
        historyRepository.changeStatus(runningScheduler.historyId, SchedulerStatus.running);

        runningScheduler.scheduler = scheduler
                .onCancel(() -> {
                    taskRepository.changeStatus(task.getId(), TaskStatus.cancel);
                    historyRepository.changeStatus(runningScheduler.historyId, SchedulerStatus.cancel);
                    runningSchedulerMap.remove(runningScheduler.scheduleId);
                })
                .onPause(() -> {
                    taskRepository.changeStatus(task.getId(), TaskStatus.suspend);
                    historyRepository.changeStatus(runningScheduler.historyId, SchedulerStatus.pause);
                })
                .onStop(() -> {
                    //调度器停止时,中断任务,已中断的任务才能重新被其他调度器获取到
                    taskRepository.changeStatus(task.getId(), TaskStatus.interrupt);
                    historyRepository.changeStatus(runningScheduler.historyId, SchedulerStatus.stop);
                    runningSchedulerMap.remove(runningScheduler.scheduleId);
                })
                .onTriggered((context) -> {
                    Lock lock = Lock.fake;
                    try {
                        //选择worker
                        TaskWorker worker = taskWorkerManager.select(group);
                        if (worker != null) {
                            log.debug("select worker[{}] execute job[{}]",worker.getId(),task.getJobId());
                            //如果不是并行,则锁住.避免重复执行
                            if (!parallel) {
                                lock = lockManager.tryGetLock("parallel_lock_for_job_" + task.getJobId(), task.getTimeout(), TimeUnit.MILLISECONDS);
                            }
                            Lock finalLock = lock;
                            //修改状态
                            changeTaskStatus(task, TaskStatus.running);

                            eventPublisher.publish(new TaskExecuteBeforeEvent(task));

                            //提交任务给worker
                            worker.getExecutor()
                                    .submitTask(task, result -> {
                                        try {
                                            eventPublisher.publish(new TaskExecuteAfterEvent(task, result));
                                            changeTaskStatus(task, result.getStatus());
                                            logExecuteResult(result);
                                        } finally {
                                            finalLock.release();
                                            context.next(result.isSuccess());
                                        }
                                    });
                        } else {
                            lock.release();
                            context.next(false);
                            changeTaskStatus(task, TaskStatus.noWorker);
                            eventPublisher.publish(new TaskFailedEvent(task, null));
                            log.warn("can not find any worker for task:[{}]", task.getId());
                        }
                    } catch (Exception e) {
                        lock.release();
                        eventPublisher.publish(new TaskFailedEvent(task, e));
                        log.error("schedule error,taskId:[{}], jobId:[{}],group:[{}]", task.getId(), task.getJobId(), group, e);
                        context.next(false);
                    }
                })
                .start();
        log.debug("do schedule task {},scheduler:{}", task, scheduler);
    }

    @Override
    public void schedule(String jobId, Scheduler scheduler) {
        JobDetail job = jobRepository.findById(jobId);
        if (job == null) {
            throw new NullPointerException("job [" + jobId + "] not exists");
        }
        //创建Task
        Task task = taskFactory.create(job);
        String scheduleId = schedulerId + "_" + UUID.randomUUID().toString();
        task.setScheduleId(scheduleId);
        task.setSchedulerId(schedulerId);
        task.setStatus(TaskStatus.preparing);
        taskRepository.save(task);
        eventPublisher.publish(new TaskCreatedEvent(task));
        //记录日志
        ScheduleHistory scheduleHistory = new ScheduleHistory();
        scheduleHistory.setId(UUID.randomUUID().toString());
        scheduleHistory.setCreateTime(System.currentTimeMillis());
        scheduleHistory.setJobId(jobId);
        scheduleHistory.setJobName(job.getName());
        scheduleHistory.setTaskId(task.getId());
        scheduleHistory.setSchedulerConfiguration(scheduler.getConfiguration());
        scheduleHistory.setScheduleId(scheduleId);
        scheduleHistory.setSchedulerId(schedulerId);
        historyRepository.save(scheduleHistory);

        //执行调度
        doSchedule(task, scheduleHistory.getId(), scheduler);
    }

    @Override
    public void cancel(String scheduleId, boolean force) {
        RunningScheduler runner = runningSchedulerMap.remove(scheduleId);
        if (null != runner) {
            runner.cancel(force);
        }
    }

    @Override
    public boolean pause(String scheduleId) {
        RunningScheduler runner = runningSchedulerMap.get(scheduleId);
        if (null != runner) {
            runner.scheduler.pause();
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":(startTime:" + startupTime + ",running:" + runningSchedulerMap.size() + ")";
    }
}
