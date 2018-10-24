package org.hswebframework.task.worker.executor;

import org.hswebframework.task.Task;
import org.hswebframework.task.TaskOperationResult;
import org.hswebframework.task.TaskStatus;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface RunnableTask extends TaskExecuteCounter {
    String getId();

    Task getTask();

    TaskStatus getStatus();

    TaskOperationResult getLastResult();

    TaskOperationResult run();
}
