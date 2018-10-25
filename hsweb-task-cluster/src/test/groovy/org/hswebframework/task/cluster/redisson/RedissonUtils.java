package org.hswebframework.task.cluster.redisson;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class RedissonUtils {

    public static RedissonClient createRedissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress(System.getProperty("hsweb.task.cluster.redis.address", "tcp://127.0.0.1:6379"))
                .setDatabase(Integer.getInteger("hsweb.task.cluster.redis.database", 0));
        return Redisson.create(config);
    }
}
