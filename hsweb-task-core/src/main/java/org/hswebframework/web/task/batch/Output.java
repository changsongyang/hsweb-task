package org.hswebframework.web.task.batch;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface Output<T> {
    void write(T data);
}
