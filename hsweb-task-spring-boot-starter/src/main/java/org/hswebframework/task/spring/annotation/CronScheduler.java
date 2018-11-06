package org.hswebframework.task.spring.annotation;

import com.cronutils.model.CronType;
import org.hswebframework.task.scheduler.supports.CronSchedulerProvider;

import java.lang.annotation.*;

/**
 * 使用cron进行调度
 *
 * @author zhouhao
 * @see org.hswebframework.task.scheduler.supports.CronScheduler
 * @see CronSchedulerProvider
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
//注解Scheduler,并定义类型
@Scheduler(type = CronSchedulerProvider.TYPE)
public @interface CronScheduler {

    /**
     * @return cron表达式, 如: 0/10 * * * * ?
     * @see org.hswebframework.task.scheduler.supports.CronScheduler#cron
     */
    String cron();

    /**
     * @return cron 类型
     * @see CronType
     * @see org.hswebframework.task.scheduler.supports.CronScheduler#cronType
     */
    CronType cronType() default CronType.QUARTZ;

}
