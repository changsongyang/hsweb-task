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

    private long lastFlushTime = 0;

    private static List<LocalCacheBatching> all = new ArrayList<>();

    static {
        Runnable flashAll = () -> {
            for (LocalCacheBatching localCacheBatching : all) {
                if (!localCacheBatching.cache.isEmpty()) {
                    log.info("flush cache batching,size:{}", localCacheBatching.cache.size());
                    localCacheBatching.flush();
                }
            }
        };
        Thread cacheBatchingFlushThread = new Thread(() -> {
            while (true) {
                flashAll.run();
                try {
                    Thread.sleep(Long.getLong("task.batch.flush-interval", 10000));
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
    public synchronized void input(T data) {
        cache.add(data);
        if (cache.size() >= batchSize) {
            flush();
        }
    }

    @Override
    public synchronized void onBatch(Consumer<List<T>> batch) {
        if (this.batch == null) {
            this.batch = batch;
        } else {
            this.batch = this.batch.andThen(batch);
        }
    }

    private synchronized void flush() {
        List<T> tmp = new ArrayList<>(cache);
        lastFlushTime = System.currentTimeMillis();
        cache.clear();
        batch.accept(tmp);
    }
}
