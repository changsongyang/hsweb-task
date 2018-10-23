package org.hswebframework.task.local;

import org.hswebframework.task.batch.BatchTaskBuilderFactory;
import org.hswebframework.task.batch.BatchTaskBuilder;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class LocalBatchTaskBuilderFactory implements BatchTaskBuilderFactory {
    @Override
    public <I, O> BatchTaskBuilder<I, O> create() {
        return new LocalFunctionTaskBuilder<>();
    }
}
