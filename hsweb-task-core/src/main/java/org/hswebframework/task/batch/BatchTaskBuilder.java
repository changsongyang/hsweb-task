package org.hswebframework.task.batch;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface BatchTaskBuilder<I, O> {

    BatchTaskBuilder<I, O> input(Input<I> input);

    BatchTaskBuilder<I, O> batching(Batching<I> batching);

    BatchTaskBuilder<I, O> handle(Handler<I, O> handler);

    BatchTask<O> build();
}
