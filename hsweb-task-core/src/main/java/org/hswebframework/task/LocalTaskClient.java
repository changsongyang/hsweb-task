package org.hswebframework.task;

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
public class LocalTaskClient implements TaskClient {

    private JobRepository jobRepository;

    private TaskScheduler taskScheduler;

    private SchedulerFactory schedulerFactory;

    private TaskRepository taskRepository;

    @Override
    public void submitJob(JobDetail jobDetail) {
        jobRepository.save(jobDetail);
    }

    @Override
    public void schedule(String taskId, String jobId, Map<String, Object> schedulerConfiguration) {
        Scheduler scheduler = schedulerFactory.create(schedulerConfiguration);
        if (taskId == null) {
            taskScheduler.scheduleJob(jobId, scheduler);
        } else {
            Task task = taskRepository.findById(taskId);
            if(task==null){
                taskScheduler.scheduleJob(jobId, scheduler);
            }
        }

    }
}
