package org.hswebframework.task.spring.tests.worker;

import org.hswebframework.task.spring.annotation.EnableTaskWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@SpringBootApplication
@EnableTaskWorker
public class TestWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestWorkerApplication.class);
    }
}
