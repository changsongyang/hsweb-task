package org.hswebframework.task.spring.annotation;


import lombok.RequiredArgsConstructor;

import java.lang.annotation.*;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Job {

    String id();

    String name();

    int version() default 1;

    boolean parallel() default false;

    int timeoutSeconds() default 60;

    Class<Throwable>[] retryWithout() default {};

    int errorRetryTimes() default 3;

    long retryInterval() default 0;

}
