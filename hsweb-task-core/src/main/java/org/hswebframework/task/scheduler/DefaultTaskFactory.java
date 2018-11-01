package org.hswebframework.task.scheduler;

import org.hswebframework.task.Task;
import org.hswebframework.task.TaskFactory;
import org.hswebframework.task.job.JobDetail;
import org.hswebframework.task.utils.IdUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class DefaultTaskFactory implements TaskFactory {
    @Override
    public Task create(JobDetail job) {
        Task task = new Task();
        task.setId(IdUtils.newUUID());
        task.setJob(job);
        task.setJobId(job.getId());
        task.setCreateTime(System.currentTimeMillis());
        task.setTimeout(job.getExecuteTimeOut() <= 0 ? Integer.MAX_VALUE : job.getExecuteTimeOut());
        return task;
    }
}
