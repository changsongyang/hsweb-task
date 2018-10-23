package org.hswebframework.task;

import org.hswebframework.task.job.JobDetail;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface TaskFactory {
    Task create(JobDetail job);
}
