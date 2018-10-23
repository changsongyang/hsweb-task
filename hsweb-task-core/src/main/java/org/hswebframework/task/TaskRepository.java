package org.hswebframework.task;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface TaskRepository {
    Task findById(String taskId);

    void changeStatus(String taskId, TaskExecuteStatus status);

    Task save(Task task);
}
