package org.hswebframework.web.task;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
public interface TaskRunnableFactory {
    /**
     * 新建任务
     * @param detail
     * @return
     */
    TaskRunnable create(JobDetail detail);

    /**
     * 暂停任务
     * @param detail
     * @return
     */
    TaskRunnable pause(JobDetail detail);

    /**
     * 停止任务
     * @param detail
     * @return
     */
    TaskRunnable stop(JobDetail detail);
}
