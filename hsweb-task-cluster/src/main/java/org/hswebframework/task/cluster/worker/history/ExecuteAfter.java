package org.hswebframework.task.cluster.worker.history;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.task.TaskStatus;

import java.io.Serializable;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class ExecuteAfter implements Serializable {

    private String id;

    private boolean success;

    private TaskStatus status;

    private Object result;

    private long startTime;

    private long endTime;
}
