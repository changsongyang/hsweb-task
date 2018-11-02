package org.hswebframework.task.spring.configuration;

import org.hswebframework.task.LocalTaskClient;
import org.hswebframework.task.TaskClient;
import org.hswebframework.task.TaskFactory;
import org.hswebframework.task.TaskRepository;
import org.hswebframework.task.cluster.ClusterManager;
import org.hswebframework.task.cluster.client.SchedulerTaskClient;
import org.hswebframework.task.job.JobRepository;
import org.hswebframework.task.lock.LockManager;
import org.hswebframework.task.scheduler.SchedulerFactory;
import org.hswebframework.task.scheduler.TaskScheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Configuration
public class ClusterSchedulerConfiguration {

    @Bean(initMethod = "startup", destroyMethod = "shutdown")
    @Primary
    public TaskClient clusterTaskClient(ClusterManager clusterManager,
                                        TaskFactory taskFactory,
                                        LockManager lockManager,
                                        SchedulerFactory schedulerFactory,
                                        TaskScheduler taskScheduler,
                                        JobRepository jobRepository,
                                        TaskRepository taskRepository) {
        LocalTaskClient localTaskClient = new LocalTaskClient();
        localTaskClient.setJobRepository(jobRepository);
        localTaskClient.setTaskFactory(taskFactory);
        localTaskClient.setSchedulerFactory(schedulerFactory);
        localTaskClient.setTaskRepository(taskRepository);
        localTaskClient.setLockManager(lockManager);
        localTaskClient.setTaskScheduler(taskScheduler);
        return new SchedulerTaskClient(clusterManager, localTaskClient);
    }

}
