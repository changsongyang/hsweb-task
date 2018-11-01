package org.hswebframework.task.lock;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.task.utils.IdUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class LocalLockManager implements LockManager {

    private Map<String, Semaphore> semaphoreMap = new ConcurrentHashMap<>();

    @Override
    public void releaseALl() {
        semaphoreMap.values().forEach(Semaphore::release);
    }

    @Override
    @SneakyThrows
    public Lock tryGetLock(String lockName, long timeout, TimeUnit timeUnit) {
        String id = IdUtils.newUUID();

        log.debug("try lock [{}],id:{}", lockName, id);
        Semaphore semaphore = semaphoreMap.computeIfAbsent(lockName, name -> new Semaphore(1));

        boolean success = semaphore.tryAcquire(timeout, timeUnit);

        if (!success) {
            throw new TimeoutException("lock [" + lockName + "] timeout " + timeout + timeUnit.name());
        }
        return () -> {
            semaphore.release();
            log.debug("unlock [{}],id:{}", lockName, id);
        };
    }
}
