package org.hswebframework.task.spring.configuration;

import org.hswebframework.task.EventPublisher;
import org.hswebframework.task.TaskFactory;
import org.hswebframework.task.TaskRepository;
import org.hswebframework.task.job.JobRepository;
import org.hswebframework.task.lock.LockManager;
import org.hswebframework.task.scheduler.DefaultTaskScheduler;
import org.hswebframework.task.scheduler.SchedulerFactory;
import org.hswebframework.task.scheduler.TaskScheduler;
import org.hswebframework.task.scheduler.history.ScheduleHistoryRepository;
import org.hswebframework.task.scheduler.supports.CronSchedulerProvider;
import org.hswebframework.task.scheduler.supports.PeriodSchedulerProvider;
import org.hswebframework.task.spring.SchedulerFactoryBean;
import org.hswebframework.task.worker.DefaultTaskWorkerManager;
import org.hswebframework.task.worker.TaskWorkerManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Configuration
public class SchedulerConfiguration {


    @Bean
    public PeriodSchedulerProvider periodSchedulerProvider(ScheduledExecutorService executorService) {
        return new PeriodSchedulerProvider(executorService);
    }

    @Bean
    public CronSchedulerProvider cronSchedulerProvider(ScheduledExecutorService executorService) {
        return new CronSchedulerProvider(executorService);
    }

    @Bean
    @ConditionalOnMissingBean(SchedulerFactory.class)
    public SchedulerFactoryBean schedulerFactoryBean() {
        return new SchedulerFactoryBean();
    }

    @Bean
    @ConditionalOnMissingBean(TaskWorkerManager.class)
    public TaskWorkerManager taskWorkerManager() {
        return new DefaultTaskWorkerManager();
    }

    @Bean(initMethod = "startup", destroyMethod = "shutdownNow")
    @ConditionalOnMissingBean(TaskScheduler.class)
    public DefaultTaskScheduler defaultTaskScheduler(SchedulerFactory schedulerFactory,
                                                     TaskWorkerManager taskWorkerManager,
                                                     EventPublisher eventPublisher,
                                                     TaskRepository taskRepository,
                                                     TaskFactory taskFactory,
                                                     JobRepository jobRepository,
                                                     LockManager lockManager,
                                                     TaskProperties taskProperties,
                                                     ScheduleHistoryRepository scheduleHistoryRepository) {
        DefaultTaskScheduler defaultTaskScheduler = new DefaultTaskScheduler();
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
}
