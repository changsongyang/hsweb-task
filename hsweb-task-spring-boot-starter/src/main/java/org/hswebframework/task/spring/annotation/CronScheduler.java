package org.hswebframework.task.spring.annotation;

import org.hswebframework.task.scheduler.supports.CronSchedulerProvider;

import java.lang.annotation.*;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Scheduler(type = CronSchedulerProvider.TYPE)
public @interface CronScheduler {

    String cron();

    CronType cronType() default CronType.QUARTZ;

    enum CronType {
        QUARTZ,
        SPRING
    }
}
