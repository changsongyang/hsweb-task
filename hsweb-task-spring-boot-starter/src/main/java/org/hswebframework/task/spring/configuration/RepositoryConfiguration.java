package org.hswebframework.task.spring.configuration;

import org.hswebframework.task.TaskRepository;
import org.hswebframework.task.job.JobRepository;
import org.hswebframework.task.scheduler.history.ScheduleHistoryRepository;
import org.hswebframework.task.scheduler.memory.InMemoryJobRepository;
import org.hswebframework.task.scheduler.memory.InMemoryScheduleHistoryRepository;
import org.hswebframework.task.scheduler.memory.InMemoryTaskRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class RepositoryConfiguration {
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

}
