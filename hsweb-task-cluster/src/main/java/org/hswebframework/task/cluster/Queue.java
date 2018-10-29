package org.hswebframework.task.cluster;

import java.util.function.Consumer;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface Queue<T> {
    boolean add(T payload);

    void consume(Consumer<T> consumer);

    T take();

    void close();
}
