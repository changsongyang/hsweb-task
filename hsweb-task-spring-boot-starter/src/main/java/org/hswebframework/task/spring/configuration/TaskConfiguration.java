package org.hswebframework.task.spring.configuration;

import org.hswebframework.task.*;
import org.hswebframework.task.job.JobRepository;
import org.hswebframework.task.lock.LocalLockManager;
import org.hswebframework.task.lock.LockManager;
import org.hswebframework.task.scheduler.DefaultTaskFactory;
import org.hswebframework.task.scheduler.DefaultTaskScheduler;
import org.hswebframework.task.scheduler.SchedulerFactory;
import org.hswebframework.task.scheduler.TaskScheduler;
import org.hswebframework.task.scheduler.history.ScheduleHistoryRepository;
import org.hswebframework.task.scheduler.memory.InMemoryJobRepository;
import org.hswebframework.task.scheduler.memory.InMemoryScheduleHistoryRepository;
import org.hswebframework.task.scheduler.memory.InMemoryTaskRepository;
import org.hswebframework.task.spring.SpringEventPublisher;
import org.hswebframework.task.worker.TaskWorkerManager;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Configuration
public class TaskConfiguration {

    @Bean
    @ConditionalOnMissingBean(ScheduledExecutorService.class)
    public ScheduledExecutorService scheduledExecutorService() {
        return Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    }

    @Bean
    @ConfigurationProperties(prefix = "hsweb.task")
    public TaskProperties schedulerProperties() {
        return new TaskProperties();
    }

    @Bean
    @ConditionalOnMissingBean(TaskRepository.class)
    public TaskRepository taskRepository() {
        return new InMemoryTaskRepository();
    }

    @Bean
    @ConditionalOnMissingBean(JobRepository.class)
    public JobRepository jobRepository() {
        return new InMemoryJobRepository();
    }

    @Bean
    @ConditionalOnMissingBean(ScheduleHistoryRepository.class)
    public ScheduleHistoryRepository scheduleHistoryRepository() {
        return new InMemoryScheduleHistoryRepository();
    }

    @Bean
    public SpringEventPublisher springEventPublisher() {
        return new SpringEventPublisher();
    }

    @Bean
    @ConditionalOnMissingBean(LockManager.class)
    public LockManager localLockManger() {
        return new LocalLockManager();
    }

    @Bean
    @ConditionalOnMissingBean(TaskFactory.class)
    public TaskFactory taskFactory() {
        return new DefaultTaskFactory();
    }

    @Bean
    public TimeoutOperations timeoutOperations() {
        return new ThreadPoolTimeoutOperations(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
    }
}
