package org.hswebframework.task;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.task.job.JobDetail;
import org.hswebframework.task.job.JobRepository;
import org.hswebframework.task.scheduler.Scheduler;
import org.hswebframework.task.scheduler.SchedulerFactory;
import org.hswebframework.task.scheduler.TaskScheduler;

import java.util.Map;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class LocalTaskClient implements TaskClient {

    private JobRepository jobRepository;

    private TaskScheduler taskScheduler;

    private SchedulerFactory schedulerFactory;

    private TaskRepository taskRepository;

    private TaskFactory taskFactory;

    @Override
    public void submitJob(JobDetail jobDetail) {
        jobRepository.save(jobDetail);
    }

    @Override
    public void schedule(String taskId, String jobId, Map<String, Object> schedulerConfiguration) {
        Scheduler scheduler = schedulerFactory.create(schedulerConfiguration);

        if (taskId != null && jobId != null) {
            Task task = taskRepository.findById(taskId);
            JobDetail job = jobRepository.findById(jobId);
            if (task == null) {
                //创建新当task
                task = taskFactory.create(job);
                task.setId(taskId);
                task.setStatus(TaskStatus.preparing);
                taskRepository.save(task);
            }
            taskScheduler.scheduleTask(taskId, scheduler);
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
