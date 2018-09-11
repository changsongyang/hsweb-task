package org.hswebframework.web.task.local;

import org.hswebframework.web.task.batch.BatchTaskBuilder;
import org.hswebframework.web.task.batch.BatchTaskBuilderFactory;

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
