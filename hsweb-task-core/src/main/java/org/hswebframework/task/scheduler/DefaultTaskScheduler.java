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
import org.hswebframework.task.utils.IdUtils;
import org.hswebframework.task.worker.TaskWorker;
import org.hswebframework.task.worker.TaskWorkerManager;
import org.hswebframework.task.worker.WorkerStatus;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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

        private volatile String            workerId;
        private volatile Callable<Boolean> cancelRunnable;
        private volatile ScheduleContext   context;

        private          Task       task;
        private volatile AtomicLong errorCounter = new AtomicLong();

        void resetRunning() {
            cancelRunnable = null;
            context = null;
            workerId = null;
        }

        void cancel(boolean force) {
            scheduler.cancel(force);
            if (cancelRunnable != null) {
                try {
                    cancelRunnable.call();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            resetRunning();
        }

        private boolean doError(String errorName, TaskStatus status) {
            long retryTimes = task.getJob().getRetryTimes();
            List<String> retryWithout = task.getJob().getRetryWithout();

            if (retryWithout != null && !retryWithout.isEmpty()) {
                if (retryWithout.stream().anyMatch(name -> errorName.endsWith(name) || errorName.equals(status.name()))) {
                    return false;
                }
            }
            if (status == TaskStatus.noWorker) {
                return false;
            }
            return retryTimes >= 0 && errorCounter.incrementAndGet() > retryTimes;
        }
    }

    public void shutdown(boolean force) {
        for (RunningScheduler runningScheduler : runningSchedulerMap.values()) {
            runningScheduler.scheduler.stop(force);
        }
        taskWorkerManager.shutdown();
        lockManager.releaseALl();
        log.debug("shutdown scheduler {}", this);
    }

    public void startup() {
        if (startup) {
            throw new UnsupportedOperationException("task scheduler already startup with " + startupTime);
        }
        if (autoShutdown) {
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownNow));
        }
        taskWorkerManager.startup();
        startupTime = new Date();
        startup = true;
        //重启未执行的任务
        List<ScheduleHistory> histories = historyRepository.findBySchedulerId(getSchedulerId(),
                SchedulerStatus.noWorker,
                SchedulerStatus.running,
                SchedulerStatus.pause);
        log.debug("start up scheduler {}, auto start {} tasks", this, histories.size());
        histories.forEach(this::doStart);

        taskWorkerManager.onWorkerLeave(worker -> {
            //执行任务的worker下线了,为了防止一直等待任务执行,手动进行继续执行
            List<RunningScheduler> runningInWorker = getRunningScheduler(worker.getId());
            for (RunningScheduler runningScheduler : runningInWorker) {
                log.debug("doing job worker[{}] leave,do next scheduler for task:{}",
                        worker.getId(),
                        runningScheduler.task.getId());
                try {
                    if (!runningScheduler.cancelRunnable.call()) {
                        runningScheduler.context.next(false);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    runningScheduler.context.next(false);
                }
            }
        });
        taskWorkerManager.onWorkerJoin(worker -> restartNoWorkerTask());
    }

    protected void restartNoWorkerTask() {
        List<ScheduleHistory> histories = historyRepository.findBySchedulerId(getSchedulerId(), SchedulerStatus.noWorker);
        log.debug("restart no worker {} tasks", histories.size());
        histories.forEach(this::doStart);
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

    private List<RunningScheduler> getRunningScheduler(String workerId) {
        return runningSchedulerMap.values().stream()
                .filter(sc -> workerId.equals(sc.workerId))
                .collect(Collectors.toList());
    }

    protected void doSchedule(Task task, String historyId, Scheduler scheduler) {
        String group = task.getJob().getGroup();
        boolean parallel = task.getJob().isParallel();

        RunningScheduler old = runningSchedulerMap.get(historyId);
        if (null != old) {
            //进行了相同的调度
            if (old.task.equals(task) && old.scheduler.equals(scheduler)) {
                log.debug("skip same task schedule,task:[{}],scheduler:[{}]", task.getId(), scheduler);
                return;
            }
            log.debug("repeated schedule[{}],do cancel old scheduler:[{}],new scheduler:[{}]", historyId, old.scheduler, scheduler);
            old.cancel(true);
        }

        RunningScheduler runningScheduler = new RunningScheduler();
        runningScheduler.scheduleId = task.getScheduleId();
        runningScheduler.historyId = historyId;
        runningScheduler.task = task;

        runningSchedulerMap.put(historyId, runningScheduler);

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
                            runningScheduler.workerId = worker.getId();
                            runningScheduler.context = context;
                            log.debug("select worker[{}] execute job[{}]", worker.getId(), task.getJobId());
                            //如果不是并行,则锁住.避免重复执行
                            if (!parallel) {
                                lock = lockManager.tryGetLock("parallel_lock_for_job_" + task.getJobId(), task.getTimeout(), TimeUnit.MILLISECONDS);
                            }
                            Lock finalLock = lock;
                            //修改状态
                            changeTaskStatus(task, TaskStatus.running);
                            //提交任务给worker
                            String id = worker.getExecutor()
                                    .submitTask(task, result -> {
                                        runningScheduler.workerId = null;
                                        try {
                                            eventPublisher.publish(new TaskExecuteAfterEvent(task, result));
                                            changeTaskStatus(task, result.getStatus());
                                            logExecuteResult(result);
                                        } finally {
                                            runningScheduler.cancelRunnable = null;
                                            finalLock.release();
                                            if (!result.isSuccess()) {
                                                if (runningScheduler.doError(result.getErrorName(), result.getStatus())) {
                                                    context.next(false);
                                                }
                                            } else {
                                                context.next(true);
                                            }
                                        }
                                    });
                            runningScheduler.cancelRunnable = () -> worker.getExecutor().cancel(id);
                            eventPublisher.publish(new TaskExecuteBeforeEvent(id, task));
                        } else {
                            log.warn("can not find any worker for task:[{}]", task.getId());
                            lock.release();
                            runningScheduler.resetRunning();
                            changeTaskStatus(task, TaskStatus.noWorker);
                            eventPublisher.publish(new TaskFailedEvent(task, null));
                            if (runningScheduler.doError(null, TaskStatus.noWorker)) {
                                context.next(false);
                            } else {
                                historyRepository.changeStatus(runningScheduler.historyId, SchedulerStatus.noWorker);
                            }
                        }
                    } catch (Exception e) {
                        log.error("scheduleJob error,taskId:[{}], jobId:[{}],group:[{}]", task.getId(), task.getJobId(), group, e);
                        runningScheduler.resetRunning();
                        lock.release();
                        eventPublisher.publish(new TaskFailedEvent(task, e));
                        if (runningScheduler.doError(e.getClass().getName(), TaskStatus.failed)) {
                            context.next(false);
                        } else {
                            historyRepository.changeStatus(runningScheduler.historyId, SchedulerStatus.cancel);
                        }
                    }
                })
                .start();
        log.debug("do scheduleJob task {},scheduler:{}", task, scheduler);
    }

    @Override
    public String scheduleTask(String taskId, Scheduler scheduler) {
        Task task = taskRepository.findById(taskId);
        if (null == task) {
            throw new NullPointerException("task [" + taskId + "] not exists");
        }
        List<ScheduleHistory> histories = historyRepository.findByTaskId(taskId);
        ScheduleHistory history;
        if (histories == null || histories.isEmpty()) {
            history = saveScheduledHistory(task, scheduler);
        } else {
            history = histories.stream().filter(his ->
                    //同一个调度器或者是是可以竞争调度的任务
                    his.getSchedulerId().equals(getSchedulerId()) || his.getStatus().isContestable())
                    .findFirst()
                    .orElseThrow(UnsupportedOperationException::new);
            //不是同一个调度id获取的任务
            if (!history.getSchedulerId().equals(getSchedulerId())) {
                history.setSchedulerId(getSchedulerId());
                historyRepository.save(history);
            }
        }

        doSchedule(task, history.getId(), scheduler);
        return history.getId();
    }

    protected ScheduleHistory saveScheduledHistory(Task task, Scheduler scheduler) {
        ScheduleHistory scheduleHistory = new ScheduleHistory();
        scheduleHistory.setId(IdUtils.newUUID());
        scheduleHistory.setCreateTime(System.currentTimeMillis());
        scheduleHistory.setJobId(task.getJobId());
        scheduleHistory.setJobName(task.getJob().getName());
        scheduleHistory.setTaskId(task.getId());
        scheduleHistory.setSchedulerConfiguration(scheduler.getConfiguration());
        scheduleHistory.setScheduleId(task.getScheduleId());
        scheduleHistory.setSchedulerId(schedulerId);
        historyRepository.save(scheduleHistory);
        return scheduleHistory;
    }

    @Override
    public String scheduleJob(String jobId, Scheduler scheduler) {
        JobDetail job = jobRepository.findById(jobId);
        if (job == null) {
            throw new NullPointerException("job [" + jobId + "] not exists");
        }
        //创建Task
        Task task = taskFactory.create(job);
        String scheduleId = schedulerId + "_" + IdUtils.newUUID();
        task.setScheduleId(scheduleId);
        task.setSchedulerId(schedulerId);
        task.setStatus(TaskStatus.preparing);
        taskRepository.save(task);
        eventPublisher.publish(new TaskCreatedEvent(task));
        //记录日志
        ScheduleHistory history = saveScheduledHistory(task, scheduler);
        //执行调度
        doSchedule(task, history.getId(), scheduler);
        return history.getId();
    }

    @Override
    public void cancel(String scheduleId, boolean force) {
        RunningScheduler runner = runningSchedulerMap.remove(scheduleId);
        if (null != runner) {
            runner.cancel(force);
        } else {
            tryCancelNotExistsScheduler(scheduleId);
        }
    }

    protected boolean tryCancelNotExistsScheduler(String scheduleId) {

        return false;
    }

    protected boolean tryPauseNotExistsScheduler(String scheduleId) {

        return false;
    }

    protected boolean tryStartNotExistsScheduler(String scheduleId) {

        return false;
    }


    @Override
    public boolean pause(String scheduleId) {
        RunningScheduler runner = runningSchedulerMap.get(scheduleId);
        if (null != runner) {
            runner.scheduler.pause();
            return true;
        } else {
            tryPauseNotExistsScheduler(scheduleId);
        }
        return false;
    }

    protected void doStart(ScheduleHistory history) {
        Task task = taskRepository.findById(history.getTaskId());
        task.setSchedulerId(getSchedulerId());
        taskRepository.save(task);
        doSchedule(task, history.getId(), schedulerFactory.create(history.getSchedulerConfiguration()));
    }

    @Override
    public void start(String scheduleId) {
        ScheduleHistory history = historyRepository.findById(scheduleId);
        if (history == null) {
            throw new NullPointerException("不存在的调度配置");
        }
        if (history.getSchedulerId().equals(getSchedulerId())) {
            doStart(history);
        } else {
            tryStartNotExistsScheduler(scheduleId);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":(startTime:" + startupTime + ",running:" + runningSchedulerMap.size() + ")";
    }
}
