package org.hswebframework.task;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.task.job.JobDetail;


/**
 * 任务执行信息,一个{@link JobDetail}会被创建为一个Task
 *
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class Task {

    private String id;

    private String schedulerId;

    private String jobId;

    private JobDetail job;

    private long createTime;

    private long lastExecuteTime;

    private long timeout;

    private TaskStatus status;

    @Override
    public String toString() {
        return "Task(id="+id+",jobId="+jobId+",jobName="+job.getName()+",status="+status+")";
    }
}
