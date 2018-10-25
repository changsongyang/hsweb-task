package org.hswebframework.task.scheduler;


/**
 * 任务调度器,用于进行任务调度,发起任务执行请求等操作
 *
 * @author zhouhao
 * @since 1.0.0
 */
public interface TaskScheduler {

    void schedule(String jobId, Scheduler scheduler);

    void cancel(String historyId, boolean force);

    boolean pause(String historyId);

    void shutdown(boolean force);

    default void shutdownNow() {
        shutdown(true);
    }

    void startup();

}
