package org.hswebframework.task.scheduler.supports;

import org.hswebframework.task.scheduler.Scheduler;
import org.hswebframework.task.scheduler.SchedulerFactoryProvider;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class CronSchedulerProvider implements SchedulerFactoryProvider {
    public static final String TYPE = "cron";

    public CronSchedulerProvider(ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }

    private ScheduledExecutorService executorService;

    @Override
    public String getSupportType() {
        return TYPE;
    }

    @Override
    public Scheduler create(Map<String, Object> configuration) {
        CronScheduler cronScheduler = new CronScheduler(executorService);
        cronScheduler.initFromConfiguration(configuration);
        return cronScheduler;
    }
}
