package org.hswebframework.task.cluster.client;

import org.hswebframework.task.cluster.scheduler.TaskSchedulerInfo;
import org.hswebframework.task.scheduler.WorkerSelectorRule;
import org.hswebframework.task.worker.TaskWorker;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class RoundTaskSchedulerSelectorRule implements TaskSchedulerSelectorRule {

    public static final TaskSchedulerSelectorRule instance = new RoundTaskSchedulerSelectorRule();

    private volatile AtomicInteger lastWorkerIndex = new AtomicInteger(0);

    @Override
    public TaskSchedulerInfo select(List<TaskSchedulerInfo> workers) {
        if (workers == null || workers.isEmpty()) {
            return null;
        }
        if (workers.size() == 1) {
            return workers.get(0);
        }
        TaskSchedulerInfo worker = workers.get(Math.min(workers.size() - 1, lastWorkerIndex.getAndIncrement()));
        if (lastWorkerIndex.get() >= workers.size()) {
            lastWorkerIndex.set(0);
        }
        return worker;
    }
}
