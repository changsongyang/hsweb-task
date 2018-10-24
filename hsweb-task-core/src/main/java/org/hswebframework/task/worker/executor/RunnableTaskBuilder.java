package org.hswebframework.task.worker.executor;

import org.hswebframework.task.Task;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface RunnableTaskBuilder {
    RunnableTask build(Task task) throws Exception;
}
