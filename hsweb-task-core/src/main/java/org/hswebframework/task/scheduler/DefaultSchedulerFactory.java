package org.hswebframework.task.scheduler;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class DefaultSchedulerFactory implements SchedulerFactory {

    private Map<String, SchedulerFactoryProvider> providers = new HashMap<>();

    public DefaultSchedulerFactory register(SchedulerFactoryProvider provider) {
        providers.put(provider.getSupportType(), provider);
        return this;
    }

    @Override
    public Scheduler create(Map<String, Object> configuration) {
        String type = String.valueOf(configuration.get("type"));
        SchedulerFactoryProvider provider = providers.get(type);

        if (provider == null) {
            throw new UnsupportedOperationException("不支持的调度器类型:" + type);
        }

        return provider.create(configuration);
    }
}
