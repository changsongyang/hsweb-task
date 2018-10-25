package org.hswebframework.task.cluster.redisson;

import lombok.SneakyThrows;
import org.hswebframework.task.cluster.ClusterCountDownLatch;
import org.hswebframework.task.cluster.ClusterManager;
import org.hswebframework.task.cluster.Topic;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.StatusListener;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class RedissonClusterManager implements ClusterManager {
    private RedissonClient redissonClient;

    private String prefix = "hsweb:task:";

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public RedissonClusterManager(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public <T> T getObject(String name) {
        return redissonClient.<T>getBucket(prefix + name).get();
    }

    @Override
    public <T> T setObject(String name, T value) {
        redissonClient.<T>getBucket(prefix + name).set(value);
        return value;
    }

    @Override
    public <T> Map<String, T> getMap(String name) {
        return redissonClient.getMap(prefix + name);
    }

    @Override
    public <T> List<T> getList(String name) {
        return redissonClient.getList(prefix + name);
    }

    @Override
    public <T> Set<T> getSet(String name) {
        return redissonClient.getSet(prefix + name);
    }

    @Override
    public <T> BlockingQueue<T> getQueue(String name) {
        return redissonClient.getBlockingQueue(prefix + name);
    }

    @Override
    public <T> Topic<T> getTopic(String name) {
        RTopic<T> rTopic = redissonClient.getTopic(prefix + name);
        return new Topic<T>() {
            @Override
            public long subscribe(Consumer<T> consumer) {
                return rTopic.addListener((channel, msg) -> consumer.accept(msg));
            }

            @Override
            public void unSubscribe(long id) {
                rTopic.removeListener((int) id);
            }

            @Override
            public long publish(T payload) {
               return rTopic.publish(payload);
            }

            @Override
            public void close() {
                rTopic.removeAllListeners();
            }
        };
    }

    @Override
    public ClusterCountDownLatch getCountDownLatch(String name) {
        RCountDownLatch latch = redissonClient.getCountDownLatch(prefix + name);

        return new ClusterCountDownLatch() {
            @Override
            public ClusterCountDownLatch setCount(long count) {
                latch.trySetCount(count);
                return this;
            }

            @Override
            public long getCount() {
                return latch.getCount();
            }

            @Override
            public void countdown() {
                latch.countDown();
            }

            @Override
            @SneakyThrows
            public void await(long time, TimeUnit timeUnit) {
                latch.await(time, timeUnit);
            }
        };
    }
}
