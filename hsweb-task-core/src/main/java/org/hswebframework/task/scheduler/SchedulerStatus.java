package org.hswebframework.task.scheduler;

import lombok.Getter;

/**
 * @author zhouhao
 * @since 1.0
 */
@Getter
public enum SchedulerStatus {
    /**
     * 执行中
     */
    running(false),
    /**
     * 已停止
     */
    stop(true),
    /**
     * 已暂停
     */
    pause(false),
    /**
     * 任务没有worker执行
     */
    noWorker(true),
    /**
     * 已取消
     */
    cancel(true),
    /**
     * 调度器已经被禁用
     */
    disabled(true);

    //是否可竞争
    private boolean contestable;

    SchedulerStatus(boolean contestable) {
        this.contestable = contestable;
    }
}
