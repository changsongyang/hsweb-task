package org.hswebframework.task.worker.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.task.worker.TaskWorker;
import org.hswebframework.task.worker.WorkerStatus;

/**
 * worker状态变更事件
 *
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public class WorkerStatusChangedEvent {

    private TaskWorker   worker;
    private WorkerStatus changeBefore;
    private WorkerStatus changeAfter;

}
