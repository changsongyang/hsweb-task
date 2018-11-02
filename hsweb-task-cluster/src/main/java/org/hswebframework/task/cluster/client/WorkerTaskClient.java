package org.hswebframework.task.cluster.client;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.task.cluster.ClusterManager;
import org.hswebframework.task.job.JobDetail;

import java.util.Map;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
public class WorkerTaskClient extends AbstractClusterTaskClient {
    public WorkerTaskClient(ClusterManager clusterManager) {
        super(clusterManager);
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

        log.debug("submit schedule: taskId={},jobId={},configuration={}", taskId, jobId, schedulerConfiguration);
        getScheduleRequestQueue()
                .add(ScheduleRequest.builder()
                        .taskId(taskId)
                        .jobId(jobId)
                        .configuration(schedulerConfiguration)
                        .build());
    }

    @Override
    public void startup() {

    }

    @Override
    public void shutdown() {

    }
}
