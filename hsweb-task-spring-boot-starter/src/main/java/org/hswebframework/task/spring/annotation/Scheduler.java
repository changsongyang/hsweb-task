package org.hswebframework.task.spring.annotation;

import org.hswebframework.task.scheduler.SchedulerFactoryProvider;

import java.lang.annotation.*;

/**
 * 自定义调度器注解,在自定义的注解上,注解此注解,
 * 注解的属性为对应的{@link org.hswebframework.task.scheduler.Scheduler#getConfiguration()}的值
 * <p>
 * 可参照{@link CronScheduler}
 *
 * @author zhouhao
 * @see CronScheduler
 * @see org.hswebframework.task.scheduler.Scheduler#getConfiguration()
 * @see SchedulerFactoryProvider
 * @since 1.0.0
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Scheduler {
    /**
     * @return 调度器类型
     * @see SchedulerFactoryProvider#getSupportType()
     */
    String type();
}
