package org.hswebframework.task.spring.tests.worker;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class TestJob {
    public void test() throws InterruptedException {
        Thread.sleep(5000);
        System.out.println(11111);
    }
}
