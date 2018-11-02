package org.hswebframework.task.spring.tests.single;

import org.hswebframework.task.spring.annotation.EnableTaskScheduler;
import org.hswebframework.task.spring.annotation.EnableTaskWorker;
import org.hswebframework.task.spring.annotation.Job;
import org.hswebframework.task.spring.tests.scheduler.TestSchedulerApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@SpringBootApplication
@EnableTaskWorker
@EnableTaskScheduler
@Component
public class SingleNodeApplication {
    public static void main(String[] args) {
        SpringApplication.run(SingleNodeApplication.class,"--hsweb.task.cluster.enabled=false");
    }

    @Job(id = "test", name = "测试")
    @Scheduled(fixedRate = 1000,initialDelay = 10)
    public void test() {
        System.out.println(1234);
    }
}
