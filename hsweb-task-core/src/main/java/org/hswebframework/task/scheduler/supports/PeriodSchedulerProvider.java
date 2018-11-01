package org.hswebframework.task.scheduler.supports;

import org.hswebframework.task.scheduler.Scheduler;
import org.hswebframework.task.scheduler.SchedulerFactoryProvider;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class PeriodSchedulerProvider implements SchedulerFactoryProvider {

    public static final String TYPE = "period";

    private ScheduledExecutorService executorService;

    public PeriodSchedulerProvider(ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public String getSupportType() {
        return TYPE;
    }

    @Override
    public Scheduler create(Map<String, Object> configuration) {

        PeriodScheduler scheduler = new PeriodScheduler(executorService);
        scheduler.initFromConfiguration(configuration);

        return scheduler;
    }
}
