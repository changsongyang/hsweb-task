package org.hswebframework.task.scheduler;


/**
 * 任务调度器,用于进行任务调度,发起任务执行请求等操作
 *
 * @author zhouhao
 * @since 1.0.0
 */
public interface TaskScheduler {

    String schedule(String jobId, Scheduler scheduler);

    void cancel(String scheduleId, boolean force);

    boolean pause(String scheduleId);

    void start(String scheduleId);

    void shutdown(boolean force);

    default void shutdownNow() {
        shutdown(true);
    }

    void startup();

}
