package org.hswebframework.task.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.task.Task;

@AllArgsConstructor
@Getter
public class TaskCreatedEvent {
    private Task task;

}
