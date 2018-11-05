package org.hswebframework.task.spring.configuration;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.task.TaskRepository;
import org.hswebframework.task.cluster.ClusterManager;
import org.hswebframework.task.cluster.redisson.RedissonClusterManager;
import org.hswebframework.task.cluster.redisson.RedissonLockManager;
import org.hswebframework.task.cluster.redisson.repository.RedissonJobRepository;
import org.hswebframework.task.cluster.redisson.repository.RedissonScheduleHistoryRepository;
import org.hswebframework.task.cluster.redisson.repository.RedissonTaskRepository;
import org.hswebframework.task.job.JobRepository;
import org.hswebframework.task.lock.LockManager;
import org.hswebframework.task.scheduler.history.ScheduleHistoryRepository;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Configuration
public class ClusterManagerConfiguration {

    @ConfigurationProperties(prefix = "hsweb.task.cluster.redis")
    @ConditionalOnClass(RedissonClient.class)
    @ConditionalOnMissingBean(ClusterManager.class)
    @Getter
    @Setter
    public static class RedissonClusterManagerConfiguration {
        private String address = "redis://127.0.0.1:6379";

        private int database = 0;

        private String password;

        private RedissonClient redissonClient;

        @PostConstruct
        public void init() {
            Config config = new Config();
            config.useSingleServer()
                    .setDatabase(database)
                    .setAddress(address)
                    .setPassword(password);
            redissonClient = Redisson.create(config);
        }

        @Bean
        @ConditionalOnMissingBean(TaskRepository.class)
        public TaskRepository redissonTaskRepository() {
            return new RedissonTaskRepository(redissonClient.getMap("hsweb:task:repository"));
        }


        @Bean
        @ConditionalOnMissingBean(JobRepository.class)
        public JobRepository redissonJobRepositoryy() {
            return new RedissonJobRepository(redissonClient.getMap("hsweb:job:repository"));
        }

        @Bean
        @ConditionalOnMissingBean(ScheduleHistoryRepository.class)
        public ScheduleHistoryRepository redissonScheduleHistoryRepository() {
            return new RedissonScheduleHistoryRepository(redissonClient.getMap("hsweb:schedule-history:repository"));
        }

        @Bean
        public RedissonClusterManager redissonClusterManager() {
            return new RedissonClusterManager(redissonClient);
        }

        @Bean
        public LockManager redissonLockManager() {
            return new RedissonLockManager(redissonClient);
        }
    }
}
