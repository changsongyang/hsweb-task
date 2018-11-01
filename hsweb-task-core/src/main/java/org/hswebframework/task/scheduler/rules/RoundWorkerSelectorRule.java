package org.hswebframework.task.scheduler.rules;

import org.hswebframework.task.scheduler.WorkerSelectorRule;
import org.hswebframework.task.worker.TaskWorker;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class RoundWorkerSelectorRule implements WorkerSelectorRule {


    public static final WorkerSelectorRule instance = new RoundWorkerSelectorRule();

    private volatile AtomicInteger lastWorkerIndex = new AtomicInteger(0);

    @Override
    public TaskWorker select(List<TaskWorker> workers) {
        if (workers == null || workers.isEmpty()) {
            return null;
        }
        if (workers.size() == 1) {
            return workers.get(0);
        }
        TaskWorker worker = workers.get(Math.min(workers.size() - 1, lastWorkerIndex.getAndIncrement()));
        if (lastWorkerIndex.get() >= workers.size()) {
            lastWorkerIndex.set(0);
        }
        return worker;
    }
}
