package org.hswebframework.task.spring.configuration;

import org.hswebframework.task.*;
import org.hswebframework.task.cluster.ClusterManager;
import org.hswebframework.task.cluster.client.SchedulerTaskClient;
import org.hswebframework.task.job.JobRepository;
import org.hswebframework.task.scheduler.SchedulerFactory;
import org.hswebframework.task.scheduler.TaskScheduler;
import org.hswebframework.task.worker.executor.RunnableTaskBuilder;
import org.hswebframework.task.worker.executor.TaskExecutor;
import org.hswebframework.task.worker.executor.supports.ThreadPoolTaskExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.ExecutorService;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Configuration
public class LocalWorkerConfiguration {

    @Bean(initMethod = "startup", destroyMethod = "shutdown")
    @Primary
    public TaskClient clusterTaskClient(TaskFactory taskFactory,
                                        SchedulerFactory schedulerFactory,
                                        TaskScheduler taskScheduler,
                                        JobRepository jobRepository,
                                        TaskRepository taskRepository) {
        LocalTaskClient localTaskClient = new LocalTaskClient();
        localTaskClient.setJobRepository(jobRepository);
        localTaskClient.setTaskFactory(taskFactory);
        localTaskClient.setSchedulerFactory(schedulerFactory);
        localTaskClient.setTaskRepository(taskRepository);
        localTaskClient.setTaskScheduler(taskScheduler);
        return localTaskClient;
    }


    @Bean
    public TaskExecutor taskExecutor(ExecutorService executorService,
                                     RunnableTaskBuilder runnableTaskBuilder) {
        return new ThreadPoolTaskExecutor(executorService, runnableTaskBuilder);
    }

}
