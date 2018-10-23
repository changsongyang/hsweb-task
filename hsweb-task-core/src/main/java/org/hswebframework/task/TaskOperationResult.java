package org.hswebframework.task;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TaskOperationResult {
    private String taskId;

    private String jobId;

    private String executionId;

    private TaskExecuteStatus status;

    private boolean success;

    private String errorStack;

}
