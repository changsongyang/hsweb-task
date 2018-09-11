package org.hswebframework.web.task.local;

import org.hswebframework.web.task.batch.BatchTaskBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class LocalBatchTaskBuilderTest {

    private BatchTaskBuilder<Integer, Integer> builder = new LocalFunctionTaskBuilder<>();

    @Test
    public void testSync() throws InterruptedException {
        AtomicLong counter = new AtomicLong();
        builder
                .input(consumer -> {
                    for (int i = 0; i < 101; i++) {
                        consumer.accept(i);
                    }
                })
                .batching(new LocalCacheBatching<>(20))
                .handle((batch, output) -> output.write(batch.stream().mapToInt(Integer::intValue).sum()))
                .build()
                .output(counter::addAndGet)
                .start();

        Thread.sleep(2000);
        Assert.assertEquals(counter.get(), 5050);
    }

}