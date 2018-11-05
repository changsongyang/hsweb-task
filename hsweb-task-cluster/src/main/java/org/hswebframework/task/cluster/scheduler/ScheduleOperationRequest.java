package org.hswebframework.task.cluster.scheduler;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hswebframework.task.scheduler.TaskScheduler;

import java.io.Serializable;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
@ToString
public class ScheduleOperationRequest implements Serializable {

    private Operation operation;

    private String scheduleId;

    public static ScheduleOperationRequest of(Operation operation, String scheduleId) {
        ScheduleOperationRequest request = new ScheduleOperationRequest();
        request.operation = operation;
        request.scheduleId = scheduleId;
        return request;
    }

    public enum Operation {
        start() {
            @Override
            void execute(TaskScheduler scheduler, String scheduleId) {
                scheduler.start(scheduleId);
            }
        }, cancel() {
            @Override
            void execute(TaskScheduler scheduler, String scheduleId) {
                scheduler.cancel(scheduleId, false);
            }
        }, pause() {
            @Override
            void execute(TaskScheduler scheduler, String scheduleId) {
                scheduler.pause(scheduleId);
            }
        };

        abstract void execute(TaskScheduler scheduler, String scheduleId);
    }
}
