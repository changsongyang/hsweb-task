package org.hswebframework.task.spring.tests

import org.hswebframework.task.scheduler.TaskScheduler
import org.hswebframework.task.spring.tests.scheduler.TestSchedulerApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

/**
 * @author zhouhao
 * @since
 */
@ContextConfiguration
@SpringBootTest(classes = [TestSchedulerApplication.class], properties = ["classpath:application.yml"])
class InitSchedulerTest extends Specification {

    @Autowired
    private TaskScheduler scheduler;

    def "测试初始化"() {
        expect:
        scheduler != null
    }
}
