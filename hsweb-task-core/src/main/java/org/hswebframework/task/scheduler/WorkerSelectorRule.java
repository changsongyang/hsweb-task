package org.hswebframework.task.scheduler;

import org.hswebframework.task.worker.TaskWorker;

import java.util.List;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface WorkerSelectorRule {

    TaskWorker select(List<TaskWorker> workers);
}
