package org.hswebframework.task.batch.local

import org.hswebframework.task.batch.BatchTaskBuilder
import spock.lang.Specification

import java.util.concurrent.atomic.AtomicLong

/**
 * TODO 完成注释
 * @author zhouhao
 * @since
 */
class LocalBatchTaskBuilderFactoryTest extends Specification {

    private BatchTaskBuilder<Integer, Integer> builder = new LocalFunctionTaskBuilder<>();

    def "测试任务分批执行"() throws InterruptedException {
        given:
        System.setProperty("task.batch.flush-interval", "1000");
        AtomicLong counter = new AtomicLong();
        builder.input({
            consumer ->
                //不停的生产数据
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
                .handle({ batch, output -> output.write(batch.size()) })
                .build()
                .output({ i -> counter.addAndGet(i) })
                .start()

        Thread.sleep(3000);
        expect:
        counter.get() == 10000000
    }

}
