package org.hswebframework.task.cluster.client;

import org.hswebframework.task.cluster.scheduler.TaskSchedulerInfo;

import java.util.List;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface TaskSchedulerSelectorRule {

    TaskSchedulerInfo select(List<TaskSchedulerInfo> allScheduler);
}
