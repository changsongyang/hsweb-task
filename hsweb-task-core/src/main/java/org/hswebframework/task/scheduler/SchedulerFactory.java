package org.hswebframework.task.scheduler;

import java.util.Map;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface SchedulerFactory {
    Scheduler create(Map<String, Object> configuration);
}
