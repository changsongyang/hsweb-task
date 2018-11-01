package org.hswebframework.task.spring.tests.scheduler;

import org.hswebframework.task.job.JobDetail;
import org.hswebframework.task.job.JobRepository;
import org.hswebframework.task.scheduler.Schedulers;
import org.hswebframework.task.scheduler.TaskScheduler;
import org.hswebframework.task.spring.annotation.EnableTaskScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@SpringBootApplication
@EnableTaskScheduler
public class TestSchedulerApplication implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(TestSchedulerApplication.class);
    }

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private TaskScheduler taskScheduler;

    @Override
    public void run(String... args) throws Exception {
        JobDetail jobDetail = new JobDetail();
        jobDetail.setId("test");
        jobDetail.setEnabled(true);
        jobDetail.setParallel(true);
        jobDetail.setTaskType("java-method");
        jobDetail.setContent("org.hswebframework.task.spring.tests.worker.TestJob.test");
        jobRepository.save(jobDetail);

        taskScheduler.schedule("test", Schedulers.cron(Executors.newSingleThreadScheduledExecutor(), "0/5 * * * * ?"));
    }
}
