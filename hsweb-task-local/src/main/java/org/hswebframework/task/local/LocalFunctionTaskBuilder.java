package org.hswebframework.task.local;

import org.hswebframework.task.batch.*;

import java.util.function.Consumer;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class LocalFunctionTaskBuilder<T, O> implements BatchTaskBuilder<T, O> {

    private Input<T> input;

    private Batching<T> batching;

    private Handler<T, O> handler;

    @Override
    public BatchTaskBuilder<T, O> input(Input<T> input) {
        this.input = input;
        return this;
    }

    @Override
    public BatchTaskBuilder<T, O> batching(Batching<T> batching) {
        this.batching = batching;
        return this;
    }

    @Override
    public BatchTaskBuilder<T, O> handle(Handler<T, O> handler) {
        this.handler = handler;
        return this;
    }

    @Override
    public BatchTask<O> build() {
        return new BatchTask<O>() {
            private volatile Consumer<O> output;

            @Override
            public void start() {
                batching.onBatch(list ->
                        handler.handle(list, out -> {
                            if (null != output) {
                                output.accept(out);
                            }
                        }));
                input.accept(batching::input);
            }

            @Override
            public BatchTask<O> output(Consumer<O> output) {
                this.output = output;
                return this;
            }
        };
    }
}
