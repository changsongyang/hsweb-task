package org.hswebframework.task.spring.configuration;

import org.hswebframework.task.TaskClient;
import org.hswebframework.task.TimeoutOperations;
import org.hswebframework.task.cluster.ClusterManager;
import org.hswebframework.task.cluster.client.WorkerTaskClient;
import org.hswebframework.task.cluster.worker.ClusterNodeTaskWorker;
import org.hswebframework.task.cluster.worker.WorkerTaskExecutor;
import org.hswebframework.task.worker.executor.RunnableTaskBuilder;
import org.hswebframework.task.worker.executor.TaskExecutor;
import org.hswebframework.task.worker.executor.supports.DefaultRunnableTaskBuilder;
import org.hswebframework.task.worker.executor.supports.JavaMethodInvokeTaskProvider;
import org.hswebframework.task.worker.executor.supports.ThreadPoolTaskExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Configuration
public class ClusterWorkerConfiguration {

    @Bean(initMethod = "startup", destroyMethod = "shutdown")
    @ConditionalOnMissingBean(TaskClient.class)
    public TaskClient workerTaskClient(ClusterManager clusterManager) {
        return new WorkerTaskClient(clusterManager);
    }

    @Bean
    public TaskExecutor taskExecutor(
            TimeoutOperations timeoutOperations,
            TaskProperties properties,
            RunnableTaskBuilder taskBuilder,
            ClusterManager clusterManager) {

        return new WorkerTaskExecutor(timeoutOperations,
                clusterManager,
                properties.getWorker().validate().getId(),
                new ThreadPoolTaskExecutor(taskBuilder));
    }
}
