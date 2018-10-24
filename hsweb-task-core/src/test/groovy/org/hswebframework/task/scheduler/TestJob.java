package org.hswebframework.task.scheduler;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
public class TestJob {

    public static AtomicLong atomicLong = new AtomicLong();

    public static long execute() {
        log.debug("do execute {} times", atomicLong.incrementAndGet());
        return atomicLong.get();
    }
}
