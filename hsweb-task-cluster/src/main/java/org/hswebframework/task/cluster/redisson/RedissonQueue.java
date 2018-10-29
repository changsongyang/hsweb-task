package org.hswebframework.task.cluster.redisson;

import lombok.SneakyThrows;
import org.hswebframework.task.cluster.Queue;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonReactiveClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class RedissonQueue<T> implements Queue<T> {

    private RBlockingQueue<T> realQueue;

    private Set<Consumer<T>> consumers = new HashSet<>();

    private Future<?> consumeFuture;

    private ExecutorService executorService;

    private boolean running;

    public RedissonQueue(RBlockingQueue<T> realQueue, ExecutorService executorService) {
        this.realQueue = realQueue;
        this.executorService = executorService;
    }

    @Override
    public boolean add(T payload) {
        return realQueue.add(payload);
    }

    @Override
    public synchronized void consume(Consumer<T> consumer) {
        boolean doStart = consumers.isEmpty();
        consumers.add(consumer);
        if (doStart) {
            startConsumer();
        }
    }

    protected void startConsumer() {
        running = true;
        consumeFuture = executorService.submit(() -> {
            for (; running; ) {
                if (consumers.isEmpty()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }
                try {
                    T data = realQueue.take();
                    for (Consumer<T> consumer : consumers) {
                        consumer.accept(data);
                    }
                } catch (InterruptedException e) {
                    running = false;
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    @Override
    @SneakyThrows
    public T take() {
        return realQueue.take();
    }

    @Override
    public void close() {
        consumers.clear();
        realQueue.delete();
        running = false;
        consumeFuture.cancel(false);
    }
}
