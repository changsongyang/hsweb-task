package org.hswebframework.web.task.batch;

import java.util.function.Consumer;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface Input<T> {
    void accept(Consumer<T> consumer);
}
