package org.hswebframework.task.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.task.Task;
import org.hswebframework.task.TaskOperationResult;

@AllArgsConstructor
@Getter
public class TaskExecuteAfterEvent {
    private Task task;

    private TaskOperationResult result;
}
