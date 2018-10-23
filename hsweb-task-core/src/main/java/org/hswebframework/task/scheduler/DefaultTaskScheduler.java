package org.hswebframework.task.scheduler;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.task.*;
import org.hswebframework.task.job.JobDetail;
import org.hswebframework.task.job.JobRepository;
import org.hswebframework.task.lock.ScheduleLockManager;
import org.hswebframework.task.scheduler.history.ScheduleHistory;
import org.hswebframework.task.scheduler.history.ScheduleHistoryRepository;
import org.hswebframework.task.worker.TaskWorker;
import org.hswebframework.task.worker.TaskWorkerManager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
@Getter
@Setter
public class DefaultTaskScheduler implements TaskScheduler {

    private String schedulerId;

    private JobRepository jobRepository;

    private TaskWorkerManager taskWorkerManager;

    private ScheduleLockManager lockManager;

    private TaskFactory taskFactory;

    private TaskRepository taskRepository;

    private ScheduleHistoryRepository historyRepository;

    private Map<String, RunningScheduler> runningSchedulerMap = new ConcurrentHashMap<>();

    class RunningScheduler {
        private volatile Scheduler scheduler;
        private volatile String    scheduleId;
        private volatile String    historyId;

        void cancel(boolean force) {
            scheduler.cancel(force);
        }
    }

    protected void logExecuteResult(TaskOperationResult result) {
        log.debug("任务执行完成:", result);
    }

    protected void doSchedule(Task task, String historyId, Scheduler scheduler) {
        String group = task.getJob().getGroup();
        boolean parallel = task.getJob().isParallel();

        RunningScheduler runningScheduler = new RunningScheduler();
        runningScheduler.scheduleId = task.getScheduleId();
        runningScheduler.historyId = historyId;
        runningSchedulerMap.put(task.getSchedulerId(), runningScheduler);
        historyRepository.changeStatusById(runningScheduler.historyId, SchedulerStatus.running);
        runningScheduler.scheduler = scheduler
                .onCancel(() -> {
                    taskRepository.changeStatus(task.getId(), TaskExecuteStatus.cancel);
                    historyRepository.changeStatusById(runningScheduler.historyId, SchedulerStatus.cancel);
                    runningSchedulerMap.remove(runningScheduler.scheduleId);
                })
                .onPause(() -> {
                    taskRepository.changeStatus(task.getId(), TaskExecuteStatus.suspend);
                    historyRepository.changeStatusById(runningScheduler.historyId, SchedulerStatus.pause);
                })
                .onStop(() -> {
                    //调度器停止时,中断任务,已中断的任务才能重新被其他调度器获取到
                    taskRepository.changeStatus(task.getId(), TaskExecuteStatus.interrupt);
                    historyRepository.changeStatusById(runningScheduler.historyId, SchedulerStatus.stop);
                    runningSchedulerMap.remove(runningScheduler.scheduleId);
                })
                .onTriggered((context) -> {
                    try {
                        //选择worker
                        TaskWorker worker = taskWorkerManager.select(group);
                        if (worker != null) {
                            //如果不是并行,则锁住.避免重复执行
                            if (!parallel) {
                                context.lock();
                            }
                            //修改状态
                            taskRepository.changeStatus(task.getId(), TaskExecuteStatus.running);
                            //提交任务打worker
                            worker.getExecutor()
                                    .submitTask(task, result -> {
                                        taskRepository.changeStatus(task.getId(), result.getStatus());
                                        //如果不是并行,则释放锁.
                                        if (!parallel) {
                                            context.release();
                                        }
                                        logExecuteResult(result);
                                    });
                        } else {
                            taskRepository.changeStatus(task.getId(), TaskExecuteStatus.noWorker);
                            log.warn("未给任务:[{}]找到合适的worker.", task.getId());
                        }
                    } catch (Exception e) {
                        if (!parallel) {
                            context.release();
                        }
                        log.error("调度失败,taskId:[{}], jobId:[{}],group:[{}]", task.getId(), task.getJobId(), group, e);
                    }
                })
                .start();
    }

    @Override
    public void schedule(String jobId, Scheduler scheduler) {
        JobDetail job = jobRepository.findById(jobId);
        if (job == null) {
            throw new NullPointerException("任务[" + jobId + "]不存在");
        }
        //创建Task
        Task task = taskFactory.create(job);
        String scheduleId = schedulerId + "_" + UUID.randomUUID().toString();
        task.setScheduleId(scheduleId);
        task.setSchedulerId(schedulerId);
        task.setStatus(TaskExecuteStatus.preparing);
        taskRepository.save(task);
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
}
