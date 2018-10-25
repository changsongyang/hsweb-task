package org.hswebframework.task.cluster.redisson;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.task.lock.Lock;
import org.hswebframework.task.lock.LockManager;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
public class RedissonLockManager implements LockManager {

    private RedissonClient redissonClient;

    public RedissonLockManager(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    @SneakyThrows
    public Lock tryGetLock(String lockName, long timeout, TimeUnit timeUnit) {
        String id = UUID.randomUUID().toString();
        log.debug("try lock {} ,id={}", lockName, id);
        RSemaphore semaphore = redissonClient.getSemaphore(lockName);
        semaphore.trySetPermits(1);
        boolean success = semaphore.tryAcquire(timeout, timeUnit);
        if (!success) {
            throw new TimeoutException("try lock " + lockName + " timeout");
        }
        return () -> {
            semaphore.release();
            log.debug("unlock {},id={}", lockName, id);
        };
    }
}
