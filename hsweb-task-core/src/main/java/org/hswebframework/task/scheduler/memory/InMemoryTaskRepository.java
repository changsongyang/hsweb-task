package org.hswebframework.task.scheduler.memory;

import org.hswebframework.task.Task;
import org.hswebframework.task.TaskRepository;
import org.hswebframework.task.TaskStatus;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class InMemoryTaskRepository implements TaskRepository {

    private Map<String, Task> taskMap = new ConcurrentHashMap<>();

    @Override
    public Task findById(String taskId) {
        return taskMap.get(taskId);
    }

    @Override
    public void changeStatus(String taskId, TaskStatus status) {
        Optional.ofNullable(findById(taskId))
                .ifPresent(task -> task.setStatus(status));
    }

    @Override
    public Task save(Task task) {
        if (task.getId() == null) {
            task.setId(UUID.randomUUID().toString());
        }
        taskMap.put(task.getId(), task);
        return task;
    }
}
