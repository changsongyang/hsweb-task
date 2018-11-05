package org.hswebframework.task.scheduler.history;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.task.Task;
import org.hswebframework.task.job.JobDetail;
import org.hswebframework.task.scheduler.SchedulerStatus;

import java.util.Map;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class ScheduleHistory {

    private String id;

    /**
     * 调度器ID,如果调度器存在多个,使用此ID进行区分
     */
    private String schedulerId;

    /**
     * 创建此调度的worker
     */
    private String createSchedulerId;

    /**
     * 作业ID
     *
     * @see JobDetail#id
     */
    private String jobId;

    /**
     * 任务名称
     *
     * @see JobDetail#name
     */
    private String jobName;

    /**
     * taskId
     *
     * @see Task#getId()
     */
    private String taskId;

    private long createTime;

    private long updateTime;

    private SchedulerStatus status;

    private Map<String, Object> schedulerConfiguration;

}
