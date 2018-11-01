package org.hswebframework.task.scheduler.memory;

import org.hswebframework.task.Task;
import org.hswebframework.task.TaskRepository;
import org.hswebframework.task.TaskStatus;
import org.hswebframework.task.utils.IdUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class InMemoryTaskRepository implements TaskRepository {

    private Map<String, Task> taskMap = new ConcurrentHashMap<>();

    @Override
    public List<Task> findAll() {
        return new ArrayList<>(taskMap.values());
    }

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
            task.setId(IdUtils.newUUID());
        }
        taskMap.put(task.getId(), task);
        return task;
    }
}
