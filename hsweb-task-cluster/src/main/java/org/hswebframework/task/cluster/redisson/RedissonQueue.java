package org.hswebframework.task.cluster.redisson;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.task.cluster.Queue;
import org.redisson.api.RBlockingQueue;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
public class RedissonQueue<T> implements Queue<T> {

    private RBlockingQueue<T> realQueue;

    private volatile Consumer<T> consumer;

    private Set<Consumer<T>> consumers = new HashSet<>();

    private ExecutorService executorService;

    private boolean running;

    private Stream<T> stream;
    private Future<?> stage;

    public RedissonQueue(RBlockingQueue<T> realQueue, ExecutorService executorService) {
        this.realQueue = realQueue;
        this.executorService = executorService;
        consumer = t -> {
            for (Consumer<T> consumer : consumers) {
                try {
                    consumer.accept(t);
                } catch (Exception e) {
                    log.warn("accept queue data error:{}", e.getMessage(), e);
                }
            }
        };
    }

    @Override
    public boolean add(T payload) {
        return realQueue.add(payload);
    }

    @Override
    public synchronized void consume(Consumer<T> consumer) {
        consumers.add(consumer);
        if (!running) {
            startConsumer();
        }
    }

    @SneakyThrows
    protected T take() {
        return realQueue.take();
    }

    protected void startConsumer() {
        running = true;
        stream = Stream.generate(this::take).filter(Objects::nonNull);
        stage = CompletableFuture.runAsync(() -> stream.parallel().forEach(t -> consumer.accept(t)), executorService);
    }

    @Override
    @SneakyThrows
    public T poll(long timeout, TimeUnit timeUnit) {
        return realQueue.poll(timeout, timeUnit);
    }

    @Override
    public void close() {
        realQueue.expire(1, TimeUnit.MILLISECONDS);
        running = false;
        if (stream != null) {
            stream.close();
            stage.cancel(true);
        }
    }
}
