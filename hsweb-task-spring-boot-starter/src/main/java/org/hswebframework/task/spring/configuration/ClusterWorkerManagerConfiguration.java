package org.hswebframework.task.spring.configuration;

import org.hswebframework.task.cluster.ClusterManager;
import org.hswebframework.task.cluster.worker.ClusterWorkerManager;
import org.hswebframework.task.worker.TaskWorkerManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 * @since
 */
@Configuration
public class ClusterWorkerManagerConfiguration {
    @Bean
    public TaskWorkerManager taskWorkerManager(ClusterManager clusterManager) {
        return new ClusterWorkerManager(clusterManager);
    }
}
