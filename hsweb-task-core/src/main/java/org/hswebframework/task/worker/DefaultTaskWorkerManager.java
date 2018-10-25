package org.hswebframework.task.worker;

import java.util.*;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class DefaultTaskWorkerManager implements TaskWorkerManager {

    private Map<String, TaskWorker> workerRepository = new HashMap<>();

    @Override
    public TaskWorker getWorkerById(String id) {
        return workerRepository.get(id);
    }

    @Override
    public List<TaskWorker> getAllWorker() {
        return new ArrayList<>(workerRepository.values());
    }

    @Override
    public TaskWorker select(String group) {
        return getAllWorker()
                .stream()
                .filter(worker -> {
                    if (group == null || group.length() == 0) {
                        return true;
                    }
                    return worker.getHealth() > 0 && Arrays.asList(worker.getGroups()).contains(group);
                })
                .max(Comparator.comparingInt(TaskWorker::getHealth))
                .orElse(null);
    }

    @Override
    public TaskWorker register(TaskWorker worker) {
        workerRepository.put(worker.getId(), worker);
        worker.startup();
        return worker;
    }

    @Override
    public TaskWorker unregister(String id, boolean force) {
        TaskWorker worker = workerRepository.get(id);
        if (worker == null) {
            return null;
        }
        worker.shutdown(force);
        return worker;
    }

    @Override
    public void startup() {

    }
}
