package org.hswebframework.task.spring.configuration;

import org.hswebframework.task.*;
import org.hswebframework.task.cluster.ClusterManager;
import org.hswebframework.task.cluster.client.SchedulerTaskClient;
import org.hswebframework.task.cluster.scheduler.ClusterTaskScheduler;
import org.hswebframework.task.job.JobRepository;
import org.hswebframework.task.lock.LockManager;
import org.hswebframework.task.scheduler.DefaultTaskScheduler;
import org.hswebframework.task.scheduler.SchedulerFactory;
import org.hswebframework.task.scheduler.TaskScheduler;
import org.hswebframework.task.scheduler.history.ScheduleHistoryRepository;
import org.hswebframework.task.spring.ClusterTaskSchedulerBean;
import org.hswebframework.task.worker.TaskWorkerManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.ScheduledExecutorService;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Configuration
public class ClusterSchedulerConfiguration {

    @Bean(destroyMethod = "shutdownNow")
    public ClusterTaskSchedulerBean clusterTaskScheduler(ClusterManager clusterManager,
                                              ScheduledExecutorService executorService,
                                              SchedulerFactory schedulerFactory,
                                              TaskWorkerManager taskWorkerManager,
                                              EventPublisher eventPublisher,
                                              TaskRepository taskRepository,
                                              TaskFactory taskFactory,
                                              JobRepository jobRepository,
                                              LockManager lockManager,
                                              TaskProperties taskProperties,
                                              ScheduleHistoryRepository scheduleHistoryRepository) {
        ClusterTaskSchedulerBean defaultTaskScheduler = new ClusterTaskSchedulerBean(clusterManager);
        defaultTaskScheduler.setExecutorService(executorService);
        defaultTaskScheduler.setEventPublisher(eventPublisher);
        defaultTaskScheduler.setJobRepository(jobRepository);
        defaultTaskScheduler.setAutoShutdown(false);
        defaultTaskScheduler.setHistoryRepository(scheduleHistoryRepository);
        defaultTaskScheduler.setLockManager(lockManager);
        defaultTaskScheduler.setTaskRepository(taskRepository);
        defaultTaskScheduler.setTaskFactory(taskFactory);
        defaultTaskScheduler.setSchedulerFactory(schedulerFactory);
        defaultTaskScheduler.setSchedulerId(taskProperties.getScheduler().validate().getId());
        defaultTaskScheduler.setTaskWorkerManager(taskWorkerManager);
        return defaultTaskScheduler;
    }

    @Bean(initMethod = "startup", destroyMethod = "shutdown")
    @Primary
    public TaskClient clusterTaskClient(ClusterManager clusterManager,
                                        TaskFactory taskFactory,
                                        LockManager lockManager,
                                        SchedulerFactory schedulerFactory,
                                        TaskScheduler taskScheduler,
                                        JobRepository jobRepository,
                                        TaskRepository taskRepository) {
        SchedulerTaskClient client = new SchedulerTaskClient(clusterManager);
        client.setTaskFactory(taskFactory);
        client.setJobRepository(jobRepository);
        client.setTaskFactory(taskFactory);
        client.setSchedulerFactory(schedulerFactory);
        client.setTaskRepository(taskRepository);
        client.setLockManager(lockManager);
        client.setTaskScheduler(taskScheduler);
        return client;
    }

}
