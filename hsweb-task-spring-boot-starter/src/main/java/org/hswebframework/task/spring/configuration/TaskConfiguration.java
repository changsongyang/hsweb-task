package org.hswebframework.task.spring.configuration;

import org.hswebframework.task.TaskFactory;
import org.hswebframework.task.ThreadPoolTimeoutOperations;
import org.hswebframework.task.TimeoutOperations;
import org.hswebframework.task.lock.LocalLockManager;
import org.hswebframework.task.lock.LockManager;
import org.hswebframework.task.scheduler.DefaultTaskFactory;
import org.hswebframework.task.spring.SpringEventPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
