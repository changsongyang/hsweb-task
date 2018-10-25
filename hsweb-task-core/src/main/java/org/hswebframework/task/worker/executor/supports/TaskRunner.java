package org.hswebframework.task.worker.executor.supports;

import org.hswebframework.task.worker.executor.ExecuteContext;


/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface TaskRunner {
    Object run(ExecuteContext context) throws Throwable;
}
