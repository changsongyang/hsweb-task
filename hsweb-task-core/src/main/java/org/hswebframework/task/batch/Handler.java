package org.hswebframework.task.batch;

import java.util.List;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface Handler<T, O> {
    void handle(List<T> batch, Output<O> output);
}
