package org.hswebframework.task.worker.executor;

import org.hswebframework.task.Task;
import org.hswebframework.task.TaskOperationResult;
import org.hswebframework.task.TaskStatus;

/**
 * 可执行的task,用于最终执行task
 *
 * @author zhouhao
 * @since 1.0.0
 */
public interface RunnableTask {
    /**
     * @return 执行id
     */
    String getId();

    /**
     * task信息
     *
     * @return task信息
     */
    Task getTask();

    /**
     * @return 当前任务状态
     */
    TaskStatus getStatus();

    /**
     * 执行task,此方法不运行抛出异常,一次信息都记录到返回值中
     *
     * @return 执行结果
     */
    TaskOperationResult run();
}
