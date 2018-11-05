package org.hswebframework.task.spring;

import org.hswebframework.task.cluster.ClusterManager;
import org.hswebframework.task.cluster.scheduler.ClusterTaskScheduler;
import org.springframework.boot.CommandLineRunner;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class ClusterTaskSchedulerBean extends ClusterTaskScheduler implements CommandLineRunner {
    public ClusterTaskSchedulerBean(ClusterManager clusterManager) {
        super(clusterManager);
    }

    @Override
    public void run(String... args) {
        startup();
    }
}
