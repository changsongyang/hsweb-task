package org.hswebframework.task.worker.executor;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface TaskExecuteCounter {

    long getSuccess();

    long getFail();

}
