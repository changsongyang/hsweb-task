package org.hswebframework.task.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.task.Task;
import org.hswebframework.task.TaskStatus;

@AllArgsConstructor
@Getter
public class TaskStatusChangedEvent {

    private TaskStatus before;

    private TaskStatus after;

    private Task task;
}
