package org.hswebframework.task.scheduler;

import lombok.Getter;
import lombok.NonNull;
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

import java.util.*;
import java.util.concurrent.*;
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
    @NonNull
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

    @Getter
    @Setter
    private ScheduledExecutorService executorService;

    private volatile boolean startup;

    private Date startupTime;

    protected Map<String, RunningScheduler> runningSchedulerMap = new ConcurrentHashMap<>();

    @Getter
    protected class RunningScheduler {
        protected volatile Scheduler scheduler;
        protected volatile String    historyId;

        protected volatile String            workerId;
        protected volatile Callable<Boolean> cancelRunnable;
        protected volatile ScheduleContext   context;

        protected          Task       task;
        protected volatile AtomicLong errorCounter = new AtomicLong();

        void resetRunning() {
            cancelRunnable = null;
            context = null;
            workerId = null;
        }

        void tryCancelTask() {
            if (cancelRunnable != null) {
                try {
                    cancelRunnable.call();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }

        public void stop(boolean force) {
            if (force) {
                tryCancelTask();
            }
            scheduler.stop(force);
            resetRunning();
        }

        public void cancel(boolean force) {
            if (force) {
                tryCancelTask();
            }
            scheduler.cancel(force);
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
            try {
                runningScheduler.stop(force);

                historyRepository.changeStatus(runningScheduler.historyId, SchedulerStatus.cancel);

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        taskWorkerManager.shutdown();
        lockManager.releaseALl();
        log.debug("shutdown scheduler {}", this);
    }

    public void startup() {
        if (startup) {
            throw new UnsupportedOperationException("task scheduler already startup with " + startupTime);
        }
        startupTime = new Date();
        startup = true;
        Objects.requireNonNull(getSchedulerId(), "schedulerId can not be null");
        Objects.requireNonNull(getEventPublisher(), "schedulerId can not be null");
        Objects.requireNonNull(getLockManager(), "lockManager can not be null");
        Objects.requireNonNull(getTaskRepository(), "taskRepository can not be null");
        Objects.requireNonNull(getTaskWorkerManager(), "taskWorkerManager can not be null");
        Objects.requireNonNull(getTaskFactory(), "taskFactory can not be null");
        Objects.requireNonNull(getHistoryRepository(), "historyRepository can not be null");
        Objects.requireNonNull(getSchedulerFactory(), "schedulerFactory can not be null");
        Objects.requireNonNull(getJobRepository(), "jobRepository can not be null");

        if (executorService == null) {
            executorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        }
        if (autoShutdown) {
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownNow));
        }
        //重启未执行的任务
        List<ScheduleHistory> histories = historyRepository.findBySchedulerId(getSchedulerId(),
                SchedulerStatus.noWorker,
                SchedulerStatus.running,
                SchedulerStatus.cancel);
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
                    runningScheduler.cancelRunnable.call();
                    runningScheduler.context.next(false);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    runningScheduler.context.next(false);
                }
            }
        });
        //有worker加入后尝试重启之前状态为noWorker的任务
        taskWorkerManager.onWorkerJoin(worker -> restartNoWorkerTask());

        taskWorkerManager.startup();
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
        runningScheduler.historyId = historyId;
        runningScheduler.task = task;

        runningSchedulerMap.put(historyId, runningScheduler);

        historyRepository.changeStatus(runningScheduler.historyId, SchedulerStatus.running);
        runningScheduler.scheduler = scheduler
                .onCancel(() -> {
                    taskRepository.changeStatus(task.getId(), TaskStatus.cancel);
                    historyRepository.changeStatus(runningScheduler.historyId, SchedulerStatus.cancel);
                    runningSchedulerMap.remove(runningScheduler.historyId);
                })
                .onPause(() -> {
                    taskRepository.changeStatus(task.getId(), TaskStatus.suspend);
                    historyRepository.changeStatus(runningScheduler.historyId, SchedulerStatus.pause);
                })
                .onStop(() -> {
                    //调度器停止时,中断任务,已中断的任务才能重新被其他调度器获取到
                    taskRepository.changeStatus(task.getId(), TaskStatus.interrupt);
                    historyRepository.changeStatus(runningScheduler.historyId, SchedulerStatus.stop);
                    runningSchedulerMap.remove(runningScheduler.historyId);
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
                            log.warn("can not find any worker for task:[{}],taskWorkerManager:{}", task.getId(), taskWorkerManager);
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
        Lock lock = lockManager.tryGetLock("schedule-task-lock:" + taskId, 10, TimeUnit.SECONDS);
        try {
            List<ScheduleHistory> histories = historyRepository.findByTaskId(taskId);
            ScheduleHistory history;
            //第一次调度
            if (histories == null || histories.isEmpty()) {
                history = saveScheduledHistory(task, scheduler);
            } else {
                history = histories.stream().filter(his ->
                        //同一个调度器或者是可以竞争调度的任务
                        his.getSchedulerId().equals(getSchedulerId())
                                || his.getStatus() == null
                                || his.getStatus().isContestable())
                        .findFirst()
                        .orElse(null);
                if (history == null) {
                    //其他调度器正在调度,可能是重复提交的任务.
                    return null;
                }
                //不是同一个调度器获取的任务,则抢过来进行调度
                if (!history.getSchedulerId().equals(getSchedulerId())) {
                    history.setSchedulerId(getSchedulerId());
                    historyRepository.save(history);
                }
            }

            doSchedule(task, history.getId(), scheduler);
            return history.getId();
        } finally {
            lock.release();
        }
    }

    protected ScheduleHistory saveScheduledHistory(Task task, Scheduler scheduler) {
        ScheduleHistory scheduleHistory = new ScheduleHistory();
        scheduleHistory.setId(IdUtils.newUUID());
        scheduleHistory.setCreateTime(System.currentTimeMillis());
        scheduleHistory.setJobId(task.getJobId());
        scheduleHistory.setJobName(task.getJob().getName());
        scheduleHistory.setTaskId(task.getId());
        scheduleHistory.setSchedulerConfiguration(new HashMap<>(scheduler.getConfiguration()));
        scheduleHistory.setCreateSchedulerId(getSchedulerId());
        scheduleHistory.setSchedulerId(getSchedulerId());
        scheduleHistory.setStatus(SchedulerStatus.noWorker);
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
            tryCancelNotExistsScheduler(historyRepository.findById(scheduleId));
        }
    }

    protected boolean tryCancelNotExistsScheduler(ScheduleHistory history) {
        //取消了本调度器不存在的任务
        return false;
    }

    protected boolean tryPauseNotExistsScheduler(ScheduleHistory history) {
        //暂停了本调度器不存在的任务
        return false;
    }

    protected boolean tryStartNotExistsScheduler(ScheduleHistory history) {
        //开始本调度器不存在的任务
        return false;
    }


    @Override
    public boolean pause(String scheduleId) {
        RunningScheduler runner = runningSchedulerMap.get(scheduleId);
        if (null != runner) {
            runner.scheduler.pause();
            return true;
        } else {
            tryPauseNotExistsScheduler(historyRepository.findById(scheduleId));
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
            tryStartNotExistsScheduler(history);
        }
    }

    @Override
    public String getId() {
        return getSchedulerId();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":(startTime:" + startupTime + ",running:" + runningSchedulerMap.size() + ")";
    }
}
