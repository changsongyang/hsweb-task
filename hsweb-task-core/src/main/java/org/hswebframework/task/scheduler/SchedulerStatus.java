package org.hswebframework.task.scheduler;

import lombok.Getter;

/**
 * @author zhouhao
 * @since 1.0
 */
@Getter
public enum SchedulerStatus {
    running(false),
    stop(true),
    pause(false),
    noWorker(true),
    cancel(true);

    //是否可竞争
    private boolean contestable;

    SchedulerStatus(boolean contestable) {
        this.contestable = contestable;
    }
}
