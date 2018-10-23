package org.hswebframework.task.batch;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface Batching<T> {
    void input(T data);

    void onBatch(Consumer<List<T>> batch);
}
