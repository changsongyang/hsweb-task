package org.hswebframework.task.spring.tests.worker;

import org.hswebframework.task.spring.annotation.Job;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Component
public class TestJob {

    @Job(id = "test.job", name = "测试任务")
    @Scheduled(cron = "0/5 * * * * ?")
    public void test() throws InterruptedException {
        Thread.sleep(5000);
        System.out.println(11111);
    }
}
