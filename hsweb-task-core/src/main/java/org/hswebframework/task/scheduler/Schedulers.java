package org.hswebframework.task.scheduler;


import org.hswebframework.task.scheduler.supports.CronScheduler;
import org.hswebframework.task.scheduler.supports.PeriodScheduler;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public abstract class Schedulers {


    public static Scheduler cron(ScheduledExecutorService executorService, String cron) {
        return new CronScheduler(cron, executorService);
    }

    public static Scheduler period(ScheduledExecutorService executorService,
                                   long initialDelay,
                                   long period,
                                   TimeUnit unit) {
        return new PeriodScheduler(executorService, initialDelay, period, unit);
    }
}
