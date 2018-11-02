package org.hswebframework.task.cluster.client;

import org.hswebframework.task.TaskClient;
import org.hswebframework.task.cluster.ClusterManager;
import org.hswebframework.task.cluster.Queue;
import org.hswebframework.task.job.JobDetail;


/**
 * @author zhouhao
 * @since 1.0.0
 */
public abstract class AbstractClusterTaskClient implements TaskClient {

    private ClusterManager clusterManager;

    protected AbstractClusterTaskClient(ClusterManager clusterManager) {
        this.clusterManager = clusterManager;
    }

    public Queue<JobDetail> getCreateJobRequestQueue() {
        return clusterManager.getQueue("client:job-request");
    }

    public Queue<ScheduleRequest> getScheduleRequestQueue() {
        return clusterManager.getQueue("client:schedule-request");
    }

}
