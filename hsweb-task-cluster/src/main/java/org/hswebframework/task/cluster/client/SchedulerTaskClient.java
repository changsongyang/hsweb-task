package org.hswebframework.task.cluster.client;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.task.TaskClient;
import org.hswebframework.task.cluster.ClusterManager;
import org.hswebframework.task.job.JobDetail;

import java.util.Map;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
public class SchedulerTaskClient extends AbstractClusterTaskClient {

    private TaskClient localClient;

    public SchedulerTaskClient(ClusterManager clusterManager, TaskClient localClient) {
        super(clusterManager);
        this.localClient = localClient;
    }

    @Override
    public void submitJob(JobDetail jobDetail) {
        localClient.submitJob(jobDetail);
    }

    @Override
    public void schedule(String taskId, String jobId, Map<String, Object> schedulerConfiguration) {
        localClient.schedule(taskId, jobId, schedulerConfiguration);
    }

    @Override
    public void startup() {
        getCreateJobRequestQueue()
                .consume(jobDetail -> {
                    log.debug("accept job request:{}", jobDetail.getId());
                    submitJob(jobDetail);
                });

        getScheduleRequestQueue()
                .consume(scheduleRequest -> {
                    log.debug("accept schedule request: taskId={},jobId={},configuration={}",
                            scheduleRequest.getTaskId()
                            , scheduleRequest.getJobId()
                            , scheduleRequest.getConfiguration());
                    schedule(scheduleRequest.getTaskId(), scheduleRequest.getJobId(), scheduleRequest.getConfiguration());
                });
    }

    @Override
    public void shutdown() {

    }
}
