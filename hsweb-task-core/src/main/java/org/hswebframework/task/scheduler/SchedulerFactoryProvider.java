package org.hswebframework.task.scheduler;

import java.util.Map;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface SchedulerFactoryProvider {
    /**
     * @return 支持的调度器类型
     * @see Scheduler#getType()
     */
    String getSupportType();

    Scheduler create(Map<String, Object> configuration);

}
