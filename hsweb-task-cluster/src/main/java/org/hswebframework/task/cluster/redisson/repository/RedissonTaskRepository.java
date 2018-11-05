package org.hswebframework.task.cluster.redisson.repository;

import org.hswebframework.task.Task;
import org.hswebframework.task.TaskRepository;
import org.hswebframework.task.TaskStatus;
import org.redisson.api.RMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class RedissonTaskRepository implements TaskRepository {

    private RMap<String, Task> repository;

    public RedissonTaskRepository(RMap<String, Task> repository) {
        this.repository = repository;
    }

    @Override
    public List<Task> findAll() {
        return new ArrayList<>(repository.values());
    }

    @Override
    public Task findById(String taskId) {
        return repository.get(taskId);
    }

    @Override
    public void changeStatus(String taskId, TaskStatus status) {
        Optional.ofNullable(repository.get(taskId))
                .ifPresent(task -> {
                    task.setStatus(status);
                    repository.put(taskId, task);
                });
    }

    @Override
    public Task save(Task task) {
        repository.put(task.getId(), task);
        return task;
    }
}
