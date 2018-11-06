package org.hswebframework.task.cluster.client;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.task.*;
import org.hswebframework.task.cluster.ClusterManager;
import org.hswebframework.task.job.JobDetail;
import org.hswebframework.task.job.JobRepository;
import org.hswebframework.task.lock.Lock;
import org.hswebframework.task.lock.LockManager;
import org.hswebframework.task.scheduler.Scheduler;
import org.hswebframework.task.scheduler.SchedulerFactory;
import org.hswebframework.task.scheduler.TaskScheduler;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 调度器端任务客户端,用于接收在其他客户端提交的任务
 *
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
public class SchedulerTaskClient extends AbstractClusterTaskClient {

    @Getter
    @Setter
    private JobRepository jobRepository;

    @Getter
    @Setter
    private TaskScheduler taskScheduler;

    @Getter
    @Setter
    private SchedulerFactory schedulerFactory;

    @Getter
    @Setter
    private TaskRepository taskRepository;

    @Getter
    @Setter
    private TaskFactory taskFactory;
    @Getter
    @Setter
    private LockManager lockManager;

    public SchedulerTaskClient(ClusterManager clusterManager) {
        super(clusterManager);
    }

    @Override
    public void submitJob(JobDetail jobDetail) {
        jobRepository.save(jobDetail);
    }

    @Override
    public void schedule(String taskId, String jobId, Map<String, Object> schedulerConfiguration) {
        Scheduler scheduler = schedulerFactory.create(schedulerConfiguration);

        if (taskId != null && jobId != null) {
            Lock lock = lockManager.tryGetLock("create-task-lock:" + taskId, 30, TimeUnit.SECONDS);
            try {
                Task task = taskRepository.findById(taskId);
                JobDetail job = jobRepository.findById(jobId);
                if (job == null) {
                    log.error("schedule error,job[{}] not fount", jobId);
                    return;
                }
                if (task == null) {
                    log.debug("create new task [{}] for job [{}]", taskId, jobId);
                    //创建新的task
                    task = taskFactory.create(job);
                    task.setId(taskId);
                    task.setJobId(jobId);
                    task.setSchedulerId(getTaskScheduler().getId());
                    task.setStatus(TaskStatus.preparing);
                    taskRepository.save(task);
                }
                //本地执行
                if (getTaskScheduler().getId().equals(task.getSchedulerId())) {
                    getTaskScheduler().scheduleTask(taskId, scheduler);
                } else {
                    //发往其他节点执行
                    getScheduleRequestQueue(task.getSchedulerId())
                            .add(ScheduleRequest.builder()
                                    .taskId(taskId)
                                    .jobId(jobId)
                                    .configuration(scheduler.getConfiguration())
                                    .build());
                }
            } finally {
                lock.release();
            }
        } else if (jobId != null) {
            taskScheduler.scheduleJob(jobId, scheduler);
        } else {
            throw new UnsupportedOperationException("taskId and jobId can not be null");
        }
    }

    @Override
    public void startup() {
        getCreateJobRequestQueue()
                .consume(jobDetail -> {
                    log.debug("accept job request:{}", jobDetail.getId());
                    submitJob(jobDetail);
                });

        Consumer<ScheduleRequest> requestConsumer = scheduleRequest -> {
            log.debug("accept schedule request: taskId={},jobId={},configuration={}",
                    scheduleRequest.getTaskId()
                    , scheduleRequest.getJobId()
                    , scheduleRequest.getConfiguration());
            schedule(scheduleRequest.getTaskId(), scheduleRequest.getJobId(), scheduleRequest.getConfiguration());
        };
        //当前调度器队列
        getScheduleRequestQueue(taskScheduler.getId()).consume(requestConsumer);
        //监听无调度器队列
        getScheduleRequestQueue("__no_scheduler").consume(requestConsumer);

    }

    @Override
    public void shutdown() {

    }
}
