package org.hswebframework.task.cluster.redisson

import org.hswebframework.task.DefaultEventSubscriberPublisher
import org.hswebframework.task.cluster.ClusterManager
import org.hswebframework.task.cluster.worker.ClusterNodeTaskWorker
import org.hswebframework.task.cluster.worker.ClusterWorkerManager
import org.hswebframework.task.job.JobDetail
import org.hswebframework.task.job.JobRepository
import org.hswebframework.task.lock.LocalLockManager
import org.hswebframework.task.scheduler.DefaultSchedulerFactory
import org.hswebframework.task.scheduler.DefaultTaskFactory
import org.hswebframework.task.scheduler.DefaultTaskScheduler
import org.hswebframework.task.scheduler.Schedulers
import org.hswebframework.task.scheduler.TaskScheduler
import org.hswebframework.task.scheduler.memory.InMemoryJobRepository
import org.hswebframework.task.scheduler.memory.InMemoryScheduleHistoryRepository
import org.hswebframework.task.scheduler.memory.InMemoryTaskRepository
import org.hswebframework.task.worker.TaskWorkerManager
import org.hswebframework.task.worker.executor.RunnableTaskBuilder
import org.hswebframework.task.worker.executor.supports.DefaultRunnableTaskBuilder
import org.hswebframework.task.worker.executor.supports.JavaMethodInvokeTaskProvider
import org.hswebframework.task.worker.executor.supports.ThreadPoolTaskExecutor
import spock.lang.Specification

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * @author zhouhao
 * @since 1.0.0
 */
class FullFunctionTest extends Specification {

    //scheduler节点
    TaskWorkerManager schedulerWorkerManager;

    //worker节点
    TaskWorkerManager workerManager;

    ClusterManager clusterManager;

    RunnableTaskBuilder taskBuilder;

    TaskScheduler scheduler;

    JobRepository jobRepository;

    def setup() {
        def redisson = RedissonUtils.createRedissonClient()

        clusterManager = new RedissonClusterManager(redisson)
        schedulerWorkerManager = new ClusterWorkerManager(clusterManager)
        schedulerWorkerManager.startup()
        workerManager = new ClusterWorkerManager(clusterManager)
        workerManager.startup()
        taskBuilder = new DefaultRunnableTaskBuilder()
        taskBuilder.addProvider(new JavaMethodInvokeTaskProvider())

        jobRepository = new InMemoryJobRepository()
        //初始化调度器

        scheduler = new DefaultTaskScheduler()
        scheduler.setEventPublisher(new DefaultEventSubscriberPublisher())
        scheduler.setHistoryRepository(new InMemoryScheduleHistoryRepository())
        scheduler.setJobRepository(jobRepository)
        scheduler.setSchedulerFactory(new DefaultSchedulerFactory())
        scheduler.setSchedulerId("test")
        scheduler.setTaskRepository(new InMemoryTaskRepository())
        scheduler.setLockManager(new LocalLockManager())
        scheduler.setTaskFactory(new DefaultTaskFactory())
        scheduler.setTaskWorkerManager(schedulerWorkerManager)
        scheduler.startup()
    }

    def "测试注册注销"() {
        given: "在worker节点注册"
        def worker = new ClusterNodeTaskWorker("test", clusterManager as ClusterManager, new ThreadPoolTaskExecutor(taskBuilder));
        worker.setGroups(["default"] as String[])
        worker.setName("测试")
        worker.setRegisterId(UUID.randomUUID().toString())
        //在worker节点注册一个worker
        workerManager.register(worker)
        Thread.sleep(100)
        when: "scheduler节点已成功注册worker"
        !schedulerWorkerManager.getAllWorker().isEmpty()
        then: "在scheduler节点注销worker"
        schedulerWorkerManager.unregister("test", true)
        Thread.sleep(100)
        expect: "worker节点的worker已被注销"
        workerManager.getAllWorker().isEmpty()
    }

    def "测试任务调度"() {
        given: "注册worker"
        def worker = new ClusterNodeTaskWorker("worker-0001", clusterManager as ClusterManager, new ThreadPoolTaskExecutor(taskBuilder));
        worker.setGroups(["worker"] as String[])
        worker.setName("调度测试")
        worker.setRegisterId(UUID.randomUUID().toString())
        workerManager.register(worker)
        Thread.sleep(200)
        when: "scheduler节点已成功注册worker"
        !schedulerWorkerManager.getAllWorker().isEmpty()
        then: "创建任务并启动调度"
        jobRepository.save(new JobDetail(
                id: "testJob",
                taskType: "java-method",
                content: "org.hswebframework.task.cluster.redisson.TestJob.execute",
                executeTimeOut: 100000,
                parallel: true
        ))
        scheduler.schedule("testJob", Schedulers.period(Executors.newSingleThreadScheduledExecutor(), 100, 100, TimeUnit.MILLISECONDS))
        Thread.sleep(5000)
        expect: "任务已执行"
        TestJob.atomicLong.get() != 0

    }


}
