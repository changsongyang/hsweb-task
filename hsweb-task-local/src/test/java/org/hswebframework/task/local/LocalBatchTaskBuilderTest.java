package org.hswebframework.task.local;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.task.batch.BatchTaskBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
public class LocalBatchTaskBuilderTest {

    private BatchTaskBuilder<Integer, Integer> builder = new LocalFunctionTaskBuilder<>();

    @Test
    public void testSync() throws InterruptedException {
        System.setProperty("task.batch.flush-interval", "1000");
        AtomicLong counter = new AtomicLong();
        builder
                .input(consumer -> {
                    for (int i = 0; i < 100; i++) {
                        int fi = i;
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        for (int i1 = 0; i1 < 100000; i1++) {
                            consumer.accept(fi);
                        }

                    }
                })
                .batching(new LocalCacheBatching<>(1000))
                .handle((batch, output) -> {
//                    log.info("do batch:" + batch);
                    output.write(batch.size());
                })
                .build()
                .output(counter::addAndGet)
                .start();

        Thread.sleep(3000);
        Assert.assertEquals(counter.get(), 10000000);
    }

}