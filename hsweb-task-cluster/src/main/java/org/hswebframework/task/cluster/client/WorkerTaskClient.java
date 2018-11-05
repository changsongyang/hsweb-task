package org.hswebframework.task.cluster.client;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.task.cluster.ClusterManager;
import org.hswebframework.task.cluster.Queue;
import org.hswebframework.task.cluster.scheduler.TaskSchedulerInfo;
import org.hswebframework.task.job.JobDetail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
public class WorkerTaskClient extends AbstractClusterTaskClient {

    private Map<String, TaskSchedulerInfo> registry;

    @Getter
    @Setter
    private TaskSchedulerSelectorRule schedulerSelectorRule = RoundTaskSchedulerSelectorRule.instance;

    public WorkerTaskClient(ClusterManager clusterManager) {
        super(clusterManager);
        registry = clusterManager.getMap("cluster:scheduler:registry");
    }

    @Override
    public void submitJob(JobDetail jobDetail) {
        log.debug("submit job to scheduler:{}", jobDetail.getId());
        getCreateJobRequestQueue().add(jobDetail);
    }

    @Override
    public void schedule(String taskId, String jobId, Map<String, Object> schedulerConfiguration) {
        if (taskId == null && jobId == null) {
            throw new IllegalArgumentException("jobId and taskId can not be null");
        }
        if (taskId != null && jobId == null) {
            throw new IllegalArgumentException("taskId is not null,jobId can not be null");
        }

        TaskSchedulerInfo info = selectTaskScheduler(new ArrayList<>(registry.values()));
        Queue<ScheduleRequest> requestQueue;
        if (info != null) {
            log.debug("submit schedule: taskId={},jobId={},configuration={},to scheduler:{}",
                    taskId,
                    jobId,
                    schedulerConfiguration,
                    info.getId());

            requestQueue = getScheduleRequestQueue(info.getId());
        } else {
            log.debug("submit schedule: taskId={},jobId={},configuration={},to scheduler:{}",
                    taskId,
                    jobId,
                    schedulerConfiguration,
                    "__no_scheduler");
            requestQueue = getScheduleRequestQueue("__no_scheduler");
        }
        requestQueue.add(ScheduleRequest.builder()
                .taskId(taskId)
                .jobId(jobId)
                .configuration(schedulerConfiguration)
                .build());
    }

    protected TaskSchedulerInfo selectTaskScheduler(List<TaskSchedulerInfo> schedulerInfoList) {
        if (schedulerInfoList.size() == 1) {
            return schedulerInfoList.get(0);
        }
        return schedulerSelectorRule.select(schedulerInfoList);
    }

    @Override
    public void startup() {

    }

    @Override
    public void shutdown() {

    }
}
