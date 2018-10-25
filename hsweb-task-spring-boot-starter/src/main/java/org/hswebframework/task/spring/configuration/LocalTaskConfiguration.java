package org.hswebframework.task.spring.configuration;

import org.hswebframework.task.scheduler.SchedulerFactory;
import org.hswebframework.task.spring.SchedulerFactoryBean;
import org.hswebframework.task.worker.DefaultTaskWorkerManager;
import org.hswebframework.task.worker.TaskWorkerManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Configuration
public class LocalTaskConfiguration {
    @Bean
    public LocalWorkerAutoRegister localWorkerAutoRegister() {
        return new LocalWorkerAutoRegister();
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

}
