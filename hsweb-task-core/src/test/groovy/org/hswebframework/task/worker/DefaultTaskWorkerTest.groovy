package org.hswebframework.task.worker

import org.hswebframework.task.Task
import org.hswebframework.task.job.JobDetail
import org.hswebframework.task.worker.executor.RunnableTaskBuilder
import org.hswebframework.task.worker.executor.TaskExecutor
import org.hswebframework.task.worker.executor.supports.DefaultRunnableTaskBuilder
import org.hswebframework.task.worker.executor.supports.JavaMethodInvokeTaskProvider
import org.hswebframework.task.worker.executor.supports.ThreadPoolTaskExecutor
import spock.lang.Specification

import java.util.concurrent.CountDownLatch

/**
 * @author zhouhao
 * @since 1.0.0
 */
class DefaultTaskWorkerTest extends Specification {


    TaskWorkerManager manager = new DefaultTaskWorkerManager()

    def setup() {
        RunnableTaskBuilder builder = new DefaultRunnableTaskBuilder()
        builder.addProvider(new JavaMethodInvokeTaskProvider())

        TaskExecutor executor = new ThreadPoolTaskExecutor(builder)

        DefaultTaskWorker worker = new DefaultTaskWorker(executor)
        worker.setId("worker-001")
        worker.setHost("localhost")
        worker.setGroups(["default"] as String[])
        worker.setName("测试worker")

        manager.register(worker)
    }


    def "测试简单的任务执行"() {
        JobDetail detail = new JobDetail(
                name: "test", id: "test",
                jobType: "java-method",
                content: "org.hswebframework.task.worker.executor.supports.TestClass.parameterMethod",
                parameters: ["0": "param1", "1": "param2"]
        )
        Task task = new Task(
                id: "test-task",
                jobId: detail.getId(),
                job: detail
        )
        def latch = new CountDownLatch(1)
        given:
        def worker = manager.select("default")
        when:
        worker != null
        then:
        def res
        worker.getExecutor().submitTask(task, { result ->
            res = result
            latch.countDown()
        })
        latch.await()
        expect:
        res != null
        res.success
        res.result == "param1" + "param2"
    }
}
