package org.hswebframework.task;

/**
 * 任务执行计数器
 *
 * @author zhouhao
 * @since 1.0.0
 */
public interface ExecuteCounter {

    long getSubmitted();

    long getRunning();

    long getFail();

    long getSuccess();

    long getWaiting();

}
