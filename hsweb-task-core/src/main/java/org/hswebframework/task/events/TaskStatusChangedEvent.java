package org.hswebframework.task.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.task.Task;
import org.hswebframework.task.TaskExecuteStatus;

@AllArgsConstructor
@Getter
public class TaskStatusChangedEvent {

    private TaskExecuteStatus before;

    private TaskExecuteStatus after;

    private Task task;
}
