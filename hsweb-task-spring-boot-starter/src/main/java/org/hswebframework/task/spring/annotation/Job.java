package org.hswebframework.task.spring.annotation;


import java.lang.annotation.*;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Job {

    String id() default "";

    String name() default "";

    int version() default 1;

}
