package org.hswebframework.web.task.local;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.task.batch.BatchTaskBuilder;
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
        System.setProperty("task.batch.flush-interval", "200");
        AtomicLong counter = new AtomicLong();
        builder
                .input(consumer -> {
                    for (int i = 0; i < 101; i++) {
                        int fi = i;
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        consumer.accept(fi);
                    }
                })
                .batching(new LocalCacheBatching<>(10))
                .handle((batch, output) -> {
                    log.info("do batch:" + batch);
                    output.write(batch.stream().mapToInt(Integer::intValue).sum());
                })
                .build()
                .output(counter::addAndGet)
                .start();

        Thread.sleep(3000);
        Assert.assertEquals(counter.get(), 5050);
    }

}