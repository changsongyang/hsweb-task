package org.hswebframework.task.cluster.redisson;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
public class TestJob {

    public static AtomicLong atomicLong = new AtomicLong();

    @SneakyThrows
    public static long execute() {
        log.debug("do execute {} times", atomicLong.incrementAndGet());
        return atomicLong.get();
    }
}
