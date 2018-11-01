package org.hswebframework.task.worker;

import lombok.Setter;
import org.hswebframework.task.scheduler.WorkerSelectorRule;
import org.hswebframework.task.scheduler.rules.RoundWorkerSelectorRule;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class DefaultTaskWorkerManager implements TaskWorkerManager {

    private Map<String, TaskWorker> workerRepository = new HashMap<>();

    private Map<Integer, Consumer<TaskWorker>> workerJoinListeners  = new ConcurrentHashMap<>();
    private Map<Integer, Consumer<TaskWorker>> workerLeaveListeners = new ConcurrentHashMap<>();

    @Setter
    private WorkerSelectorRule selectorRule = RoundWorkerSelectorRule.instance;

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
        return selectorRule.select(getAllWorker()
                .stream()
                .filter(worker -> {
                    if (group == null || group.length() == 0) {
                        return true;
                    }
                    return worker.getHealth() > 0 && Arrays.asList(worker.getGroups()).contains(group);
                }).collect(Collectors.toList()));
    }

    @Override
    public TaskWorker register(TaskWorker worker) {
        workerRepository.put(worker.getId(), worker);
        worker.startup();
        workerJoinListeners.forEach((integer, workerConsumer) -> workerConsumer.accept(worker));
        return worker;
    }

    @Override
    public TaskWorker unregister(String id, boolean force) {
        TaskWorker worker = workerRepository.get(id);
        if (worker == null) {
            return null;
        }
        workerLeaveListeners.forEach((integer, workerConsumer) -> workerConsumer.accept(worker));
        worker.shutdown(force);
        return worker;
    }

    @Override
    public long onWorkerJoin(Consumer<TaskWorker> workerConsumer) {
        int hash = System.identityHashCode(workerConsumer);
        workerJoinListeners.put(hash, workerConsumer);
        return hash;
    }

    @Override
    public long onWorkerLeave(Consumer<TaskWorker> workerConsumer) {
        int hash = System.identityHashCode(workerConsumer);
        workerLeaveListeners.put(hash, workerConsumer);
        return hash;
    }

    @Override
    public void startup() {

    }

    @Override
    public void shutdown() {

    }
}
