package org.hswebframework.task.spring.annotation;

import org.hswebframework.task.spring.configuration.SchedulerConfigurationSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(SchedulerConfigurationSelector.class)
public @interface EnableTaskScheduler {
}
