package org.hswebframework.task.cluster;

import java.util.function.Consumer;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface Topic<T> {

    long subscribe(Consumer<T> consumer);

    void unSubscribe(long id);

    long publish(T payload);

    void close();
}
