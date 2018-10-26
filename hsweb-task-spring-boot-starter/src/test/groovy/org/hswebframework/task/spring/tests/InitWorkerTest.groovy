package org.hswebframework.task.spring.tests

import org.hswebframework.task.spring.tests.worker.TestWorkerApplication
import org.hswebframework.task.worker.TaskWorkerManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

/**
 * @author zhouhao
 * @since
 */
@ContextConfiguration
@SpringBootTest(classes = [TestWorkerApplication.class], properties = ["classpath:application.yml"])
class InitWorkerTest extends Specification {

    @Autowired
    private TaskWorkerManager taskWorkerManager;

    def "测试初始化"() {
        expect:
        taskWorkerManager != null
        !taskWorkerManager.getAllWorker().isEmpty()
    }
}
