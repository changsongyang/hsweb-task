package org.hswebframework.task.scheduler;


/**
 * 调度上下文
 *
 * @author zhouhao
 * @since 1.0.0
 */
public interface ScheduleContext {

    /**
     * @return 是否为最后一次执行
     */
    boolean isLastExecute();

    /**
     * @return 获取下一次执行当时间
     */
    long getNextExecuteTime();

    /**
     * 取消本次调度
     */
    void cancel();

    /**
     * 执行下一次调度,在一次调度执行完成之后,必须调用此方法才会开始下一次调度,否则调度将会停止
     *
     * @param currentSuccess 当前调度是否成功
     */
    void next(boolean currentSuccess);
}
