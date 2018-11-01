package org.hswebframework.task

import org.hswebframework.task.job.JobDetail
import org.hswebframework.task.lock.LocalLockManager
import org.hswebframework.task.scheduler.DefaultSchedulerFactory
import org.hswebframework.task.scheduler.DefaultTaskFactory
import org.hswebframework.task.scheduler.DefaultTaskScheduler
import org.hswebframework.task.scheduler.SchedulerFactory
import org.hswebframework.task.scheduler.TaskScheduler
import org.hswebframework.task.scheduler.memory.InMemoryJobRepository
import org.hswebframework.task.scheduler.memory.InMemoryScheduleHistoryRepository
import org.hswebframework.task.scheduler.memory.InMemoryTaskRepository
import org.hswebframework.task.scheduler.supports.CronSchedulerProvider
import org.hswebframework.task.worker.DefaultTaskWorkerManager
import spock.lang.Specification

import java.util.concurrent.Executors

class LocalTaskClientTest extends Specification {


    LocalTaskClient client;

    def cleanup() {
        client.shutdown()
    }

    def setup() {
        client = new LocalTaskClient();
        SchedulerFactory schedulerFactory = new DefaultSchedulerFactory();
        schedulerFactory.register(new CronSchedulerProvider(Executors.newScheduledThreadPool(4)));

        TaskScheduler taskScheduler = new DefaultTaskScheduler();
        taskScheduler.setTaskFactory(new DefaultTaskFactory());
        taskScheduler.setTaskWorkerManager(new DefaultTaskWorkerManager());
        taskScheduler.setLockManager(new LocalLockManager());
        taskScheduler.setJobRepository(new InMemoryJobRepository())
        taskScheduler.setTaskRepository(new InMemoryTaskRepository())
        taskScheduler.setEventPublisher(new DefaultEventSubscriberPublisher())
        taskScheduler.setAutoShutdown(true)
        taskScheduler.setHistoryRepository(new InMemoryScheduleHistoryRepository())
        taskScheduler.setSchedulerId("test")

        client.setJobRepository(taskScheduler.getJobRepository());
        client.setSchedulerFactory(schedulerFactory);
        client.setTaskRepository(taskScheduler.getTaskRepository());
        client.setTaskFactory(taskScheduler.getTaskFactory())
        client.setTaskScheduler(taskScheduler)
        taskScheduler.startup();
        client.startup();
    }

    def "测试创建任务并指定taskId进行调度"() {
        given:
        JobDetail job = new JobDetail(id: "test", name: "test", jobType: "java-method", content: "org.hswebframework.task.scheduler.TestJob.execute")
        client.submitJob(job)
        when:
        client.jobRepository.findById("test") != null
        then:
        client.schedule("my-task", job.id, [type: "cron", cron: "0/2 * * * * ?"]);
        //多次提交相同任务
        client.schedule("my-task", job.id, [type: "cron", cron: "0/2 * * * * ?"]);
        expect:
        client.taskRepository.findById("my-task") != null
    }

    def "不指定taskId和jobId时报错"() {
        given:
        def error;
        try {
            client.schedule(null, null, [type: "cron", cron: "0/2 * * * * ?"])
        } catch (e) {
            error = e;
        }
        expect:
        error != null
    }

    def "指定taskId和不指定jobId时报错"() {
        given:
        def error
        try {
            client.schedule("test-task", null, [type: "cron", cron: "0/2 * * * * ?"])
        } catch (e) {
            error = e
        }
        expect:
        error != null
    }

    def "测试创建任务不指定taskId进行调度"() {
        given:
        JobDetail job = new JobDetail(id: "test", name: "test", jobType: "java-method", content: "org.hswebframework.task.scheduler.TestJob.execute")
        client.submitJob(job)
        when:
        client.jobRepository.findById("test") != null
        then:
        client.schedule(null, job.id, [type: "cron", cron: "0/2 * * * * ?"]);
        expect:
        !client.taskRepository.findAll().isEmpty()
    }
}
