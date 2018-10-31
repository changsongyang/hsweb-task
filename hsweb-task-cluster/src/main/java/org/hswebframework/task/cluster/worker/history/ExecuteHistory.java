package org.hswebframework.task.cluster.worker.history;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.task.TaskStatus;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class ExecuteHistory {
    private String id;

    private String taskId;

    private String schedulerId;

    private String requestId;

    private boolean success;

    private TaskStatus status;

    private Object result;

    private long startTime;

    private long endTime;
}
