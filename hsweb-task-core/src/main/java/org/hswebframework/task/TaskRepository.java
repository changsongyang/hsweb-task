package org.hswebframework.task;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface TaskRepository {
    Task findById(String taskId);

    void changeStatus(String taskId, TaskStatus status);

    Task save(Task task);
}
