package org.hswebframework.task;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.task.job.JobDetail;
import org.hswebframework.task.job.JobRepository;
import org.hswebframework.task.lock.Lock;
import org.hswebframework.task.lock.LockManager;
import org.hswebframework.task.scheduler.Scheduler;
import org.hswebframework.task.scheduler.SchedulerFactory;
import org.hswebframework.task.scheduler.TaskScheduler;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
@Slf4j
public class LocalTaskClient implements TaskClient {

    private JobRepository jobRepository;

    private TaskScheduler taskScheduler;

    private SchedulerFactory schedulerFactory;

    private TaskRepository taskRepository;

    private TaskFactory taskFactory;

    private LockManager lockManager;

    @Override
    public void submitJob(JobDetail jobDetail) {
        jobRepository.save(jobDetail);
    }

    @Override
    public void schedule(String taskId, String jobId, Map<String, Object> schedulerConfiguration) {
        Scheduler scheduler = schedulerFactory.create(schedulerConfiguration);

        if (taskId != null && jobId != null) {
            Lock lock = lockManager.tryGetLock("create-task-lock:" + taskId, 30, TimeUnit.SECONDS);
            try {
                Task task = taskRepository.findById(taskId);
                JobDetail job = jobRepository.findById(jobId);
                if (job == null) {
                    log.error("schedule error,job[{}] not fount", jobId);
                    return;
                }
                if (task == null) {
                    log.debug("create new task [{}] for job [{}]", taskId, jobId);
                    //创建新当task
                    task = taskFactory.create(job);
                    task.setId(taskId);
                    task.setJobId(jobId);
                    task.setStatus(TaskStatus.preparing);
                    taskRepository.save(task);
                }
                taskScheduler.scheduleTask(taskId, scheduler);
            } finally {
                lock.release();
            }
        } else if (jobId != null) {
            taskScheduler.scheduleJob(jobId, scheduler);
        } else {
            throw new UnsupportedOperationException("taskId and jobId can not be null");
        }
    }

    @Override
    public void startup() {

    }

    @Override
    public void shutdown() {

    }
}
