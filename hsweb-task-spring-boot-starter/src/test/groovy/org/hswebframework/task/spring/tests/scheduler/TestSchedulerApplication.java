package org.hswebframework.task.spring.tests.scheduler;

import org.hswebframework.task.spring.annotation.EnableTaskScheduler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@SpringBootApplication
@EnableTaskScheduler
public class TestSchedulerApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestSchedulerApplication.class);
    }
}
