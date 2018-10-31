package org.hswebframework.task.cluster.worker.history;

import lombok.Getter;
import lombok.Setter;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class ExecuteBefore {
    private String id;

    private String taskId;

    private String schedulerId;

    private String requestId;

}
