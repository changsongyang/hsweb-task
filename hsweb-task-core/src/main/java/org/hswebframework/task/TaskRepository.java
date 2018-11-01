package org.hswebframework.task;

import java.util.List;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface TaskRepository {
    List<Task> findAll();

    Task findById(String taskId);

    void changeStatus(String taskId, TaskStatus status);

    Task save(Task task);
}
