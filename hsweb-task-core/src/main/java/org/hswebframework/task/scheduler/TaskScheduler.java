package org.hswebframework.task.scheduler;


/**
 * 任务调度器,用于进行任务调度,发起任务执行请求等操作
 *
 * @author zhouhao
 * @since 1.0.0
 */
public interface TaskScheduler {

    String getId();

    /**
     * 启动一个作业调度,每次调用都将创建新的{@link org.hswebframework.task.Task}.
     * 如果多次调用,并传入相同的jobId,此job将会被多次执行.
     *
     * @param jobId     作业id,对应当任务必须存在于{@link org.hswebframework.task.job.JobRepository#findById(String)}
     * @param scheduler 调度器
     * @return 调度id, 使用此id可进行暂停, 取消, 重启操作. {@link org.hswebframework.task.scheduler.history.ScheduleHistory#id}
     * @see org.hswebframework.task.scheduler.history.ScheduleHistory#id
     * @see Scheduler
     * @see Schedulers
     * @see org.hswebframework.task.job.JobDetail
     * @see org.hswebframework.task.job.JobRepository
     */
    String scheduleJob(String jobId, Scheduler scheduler);

    /**
     * 启动一个已有的任务调度,将已有的任务使用新的调度调度器进行调度.
     *
     * @param taskId    任务ID
     * @param scheduler 调度器
     * @return 调度id
     * @see org.hswebframework.task.TaskRepository
     * @see Scheduler
     * @see Schedulers
     * @see org.hswebframework.task.Task
     */
    String scheduleTask(String taskId, Scheduler scheduler);

    /**
     * 取消一个调度
     *
     * @param scheduleId {@link org.hswebframework.task.scheduler.history.ScheduleHistory#id}
     * @param force      是否强制取消
     */
    void cancel(String scheduleId, boolean force);

    /**
     * 暂停调度
     *
     * @param scheduleId {@link org.hswebframework.task.scheduler.history.ScheduleHistory#id}
     * @return 是否成功
     */
    boolean pause(String scheduleId);

    /**
     * 启动一个调度
     *
     * @param scheduleId {@link org.hswebframework.task.scheduler.history.ScheduleHistory#id}
     */
    void start(String scheduleId);

    /**
     * 关闭此调度器,管理调度器将停止正在进行的调度任务
     *
     * @param force 是否强制关闭
     */
    void shutdown(boolean force);

    /**
     * 强制关闭
     *
     * @see this#shutdown(boolean)
     */
    default void shutdownNow() {
        shutdown(true);
    }

    /**
     * 启动调度器
     */
    void startup();

}
