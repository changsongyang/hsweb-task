package org.hswebframework.web.task.local;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.task.batch.Batching;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
public class LocalCacheBatching<T> implements Batching<T> {

    private int batchSize;

    private final List<T> cache;

    private Consumer<List<T>> batch;

    private long lastInputTime = 0;

    private final Object lock = new Object();

    private static List<LocalCacheBatching> all = new ArrayList<>();

    static {
        long flushInterval = Long.getLong("task.batch.flush-interval", 10000);

        Runnable flashAll = () -> {
            for (LocalCacheBatching localCacheBatching : all) {
                //一定间隔内有新的数据进入则不flush
                if (System.currentTimeMillis() - localCacheBatching.lastInputTime < flushInterval / 2) {
                    continue;
                }
                synchronized (localCacheBatching.lock) {
                    if (!localCacheBatching.cache.isEmpty()) {
                        log.info("flush cache batching,size:{}", localCacheBatching.cache.size());
                        localCacheBatching.flush();
                    }
                }
            }
        };
        Thread cacheBatchingFlushThread = new Thread(() -> {
            while (true) {
                flashAll.run();
                try {
                    Thread.sleep(Long.getLong("task.batch.flush-interval", flushInterval));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        cacheBatchingFlushThread.setName("cacheBatchingFlushThread");
        cacheBatchingFlushThread.start();
        Runtime.getRuntime().addShutdownHook(new Thread(flashAll));
    }

    public LocalCacheBatching() {
        this(100);
    }

    public LocalCacheBatching(int batchSize) {
        this(batchSize, new ArrayList<>());
    }

    public LocalCacheBatching(int batchSize, List<T> cache) {
        this.batchSize = batchSize;
        this.cache = cache;
        all.add(this);
    }

    @Override
    public void input(T data) {
        synchronized (lock) {
            cache.add(data);
            lastInputTime = System.currentTimeMillis();
            if (cache.size() >= batchSize) {
                flush();
            }
        }
    }

    @Override
    public void onBatch(Consumer<List<T>> batch) {
        synchronized (lock) {
            if (this.batch == null) {
                this.batch = batch;
            } else {
                this.batch = this.batch.andThen(batch);
            }
        }
    }

    private void flush() {
        synchronized (lock) {
            List<T> tmp = new ArrayList<>(cache);
            cache.clear();
            batch.accept(tmp);
        }
    }
}
