package org.hswebframework.task.spring.configuration;

import org.hswebframework.task.cluster.ClusterManager;
import org.hswebframework.task.cluster.worker.ClusterNodeTaskWorker;
import org.hswebframework.task.cluster.worker.WorkerTaskExecutor;
import org.hswebframework.task.worker.executor.RunnableTaskBuilder;
import org.hswebframework.task.worker.executor.TaskExecutor;
import org.hswebframework.task.worker.executor.supports.DefaultRunnableTaskBuilder;
import org.hswebframework.task.worker.executor.supports.JavaMethodInvokeTaskProvider;
import org.hswebframework.task.worker.executor.supports.ThreadPoolTaskExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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

    @Bean
    public JavaMethodInvokeTaskProvider javaMethodInvokeTaskProvider() {
        return new JavaMethodInvokeTaskProvider();
    }

    @Bean
    @ConditionalOnMissingBean(RunnableTaskBuilder.class)
    public SpringRunnableTaskBuilder springRunnableTaskBuilder() {
        return new SpringRunnableTaskBuilder();
    }

    @Bean
    public TaskExecutor taskExecutor(
            TaskProperties properties,
            RunnableTaskBuilder taskBuilder,
            ClusterManager clusterManager) {

        return new WorkerTaskExecutor(clusterManager, properties.getWorker().validate().getId(), new ThreadPoolTaskExecutor(taskBuilder));
    }
}
