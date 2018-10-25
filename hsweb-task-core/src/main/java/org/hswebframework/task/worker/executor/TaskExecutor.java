package org.hswebframework.task.worker.executor;

import org.hswebframework.task.ExecuteCounter;
import org.hswebframework.task.Task;
import org.hswebframework.task.TaskOperationResult;

import java.util.function.Consumer;

/**
 * 任务执行器,用于执行具体任务
 *
 * @author zhouhao
 * @since 1.0.0
 */
public interface TaskExecutor extends ExecuteCounter {
    /**
     * 提交一个任务,任务将异步执行,执行完成后会调用回调通知执行结果
     *
     * @param task           任务信息
     * @param resultConsumer 任务执行后回调执行结果
     * @return 返回本次任务的执行id
     */
    String submitTask(Task task, Consumer<TaskOperationResult> resultConsumer);

    void shutdown(boolean force);

    void startup();
}
