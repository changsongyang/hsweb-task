package org.hswebframework.task.spring.configuration;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.task.cluster.ClusterManager;
import org.hswebframework.task.cluster.redisson.RedissonClusterManager;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

        @Bean
        public RedissonClusterManager redissonClusterManager() {
            Config config = new Config();
            config.useSingleServer()
                    .setDatabase(database)
                    .setAddress(address)
                    .setPassword(password);
            RedissonClient redissonClient = Redisson.create(config);
            return new RedissonClusterManager(redissonClient);
        }
    }
}
