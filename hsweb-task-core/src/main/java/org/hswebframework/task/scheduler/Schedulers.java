package org.hswebframework.task.scheduler;


import org.hswebframework.task.scheduler.supports.PeriodScheduler;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public abstract class Schedulers {


    public static Scheduler now() {
        // TODO: 18-10-23
        throw new UnsupportedOperationException();
    }

    public static Scheduler cron(String cron) {
        // TODO: 18-10-23
        throw new UnsupportedOperationException();
    }

    public static Scheduler delay(long delay,
                                  TimeUnit unit) {
        // TODO: 18-10-23
        throw new UnsupportedOperationException();
    }

    public static Scheduler period(ScheduledExecutorService executorService,
                                   long initialDelay,
                                   long period,
                                   TimeUnit unit) {
        return new PeriodScheduler(executorService, initialDelay, period, unit);
    }
}
