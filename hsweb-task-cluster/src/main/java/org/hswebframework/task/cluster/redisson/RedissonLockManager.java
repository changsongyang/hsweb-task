package org.hswebframework.task.cluster.redisson;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.task.lock.Lock;
import org.hswebframework.task.lock.LockManager;
import org.hswebframework.task.utils.IdUtils;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
public class RedissonLockManager implements LockManager {

    private RedissonClient redissonClient;

    private Map<String, RSemaphore> rSemaphoreMap = new ConcurrentHashMap<>();

    public RedissonLockManager(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    @SneakyThrows
    public Lock tryGetLock(String lockName, long timeout, TimeUnit timeUnit) {
        String id = IdUtils.newUUID();

        RSemaphore semaphore = rSemaphoreMap.computeIfAbsent(lockName, key -> {
            RSemaphore rSemaphore = redissonClient.getSemaphore(key);
            rSemaphore.trySetPermits(1);
            return rSemaphore;
        });
        log.debug("try lock {} permits:{},id={}", lockName, semaphore.availablePermits(), id);
        boolean success = semaphore.tryAcquire(timeout, timeUnit);
        if (!success) {
            throw new TimeoutException("try lock " + lockName + " timeout");
        }
        return () -> {
            semaphore.release();
            log.debug("unlock {},id={}", lockName, id);
        };
    }

    @Override
    public void releaseALl() {
        rSemaphoreMap.values().forEach(RSemaphore::release);
    }
}
