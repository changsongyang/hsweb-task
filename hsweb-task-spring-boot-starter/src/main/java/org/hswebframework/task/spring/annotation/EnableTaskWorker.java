package org.hswebframework.task.spring.annotation;

import org.hswebframework.task.spring.configuration.WorkerConfigurationSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(WorkerConfigurationSelector.class)
public @interface EnableTaskWorker {
}
