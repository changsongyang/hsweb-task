package org.hswebframework.task.cluster.client;

import org.hswebframework.task.TaskClient;
import org.hswebframework.task.cluster.ClusterManager;
import org.hswebframework.task.cluster.Queue;
import org.hswebframework.task.cluster.scheduler.TaskSchedulerInfo;
import org.hswebframework.task.job.JobDetail;

import java.util.Map;


/**
 * @author zhouhao
 * @since 1.0.0
 */
public abstract class AbstractClusterTaskClient implements TaskClient {

    private ClusterManager clusterManager;

    private Map<String, TaskSchedulerInfo> schedulerRegistry;

    protected AbstractClusterTaskClient(ClusterManager clusterManager) {
        this.clusterManager = clusterManager;
    }

    public Queue<JobDetail> getCreateJobRequestQueue() {
        return clusterManager.getQueue("cluster:client:job-request");
    }

    public Queue<ScheduleRequest> getScheduleRequestQueue(String schedulerId) {
        return clusterManager.getQueue("cluster:client:schedule-request:" + schedulerId);
    }

}
