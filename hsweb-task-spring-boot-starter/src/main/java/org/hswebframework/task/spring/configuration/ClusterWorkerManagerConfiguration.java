package org.hswebframework.task.spring.configuration;

import org.hswebframework.task.TimeoutOperations;
import org.hswebframework.task.cluster.ClusterManager;
import org.hswebframework.task.cluster.worker.ClusterTaskWorkerManager;
import org.hswebframework.task.worker.TaskWorkerManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author zhouhao
 * @since 1.0.0
 */
@Configuration
public class ClusterWorkerManagerConfiguration {

    @Bean
    public TaskWorkerManager taskWorkerManager(TimeoutOperations timeoutOperations, ClusterManager clusterManager) {
        return new ClusterTaskWorkerManager(timeoutOperations, clusterManager);
    }


}
