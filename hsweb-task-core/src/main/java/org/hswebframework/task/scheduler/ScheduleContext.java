package org.hswebframework.task.scheduler;


/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface ScheduleContext {
    boolean isLastExecute();

    long getNextExecuteTime();

    void cancel();

    void next(boolean currentSuccess);
}
