package org.hswebframework.task;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface ExecutableTask extends Task {
    TaskOperationResult execute(TaskExecutionContext context);
}
