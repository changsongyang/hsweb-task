package org.hswebframework.task;

import org.hswebframework.task.job.JobDetail;

import java.util.Map;

/**
 * task 客户端,用户提交任务和发起调度请求
 *
 * @author zhouhao
 * @since 1.0.0
 */
public interface TaskClient {

    /**
     * 提交任务
     *
     * @param jobDetail 任务详情
     */
    void submitJob(JobDetail jobDetail);

    /**
     * 请求任务调度,参数taskId和jobId不能同时为空.
     *
     * @param taskId                 taskId 可以为空,如果为空则每次开启新的task进调度
     * @param jobId                  jobId, {@link JobDetail#getId()}
     * @param schedulerConfiguration 调度配置 {@link org.hswebframework.task.scheduler.SchedulerFactory }
     */
    void schedule(String taskId, String jobId, Map<String, Object> schedulerConfiguration);
}
