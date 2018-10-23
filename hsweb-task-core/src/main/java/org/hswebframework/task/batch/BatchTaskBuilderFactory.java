package org.hswebframework.task.batch;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface BatchTaskBuilderFactory {
    <I, O> BatchTaskBuilder<I, O> create();
}
