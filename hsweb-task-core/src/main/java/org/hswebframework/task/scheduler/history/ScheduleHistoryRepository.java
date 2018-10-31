package org.hswebframework.task.scheduler.history;

import org.hswebframework.task.scheduler.SchedulerStatus;

import java.util.List;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface ScheduleHistoryRepository {

    List<ScheduleHistory> findBySchedulerId(String schedulerId, SchedulerStatus... statuses);

    ScheduleHistory save(ScheduleHistory history);

    ScheduleHistory findById(String id);

    List<ScheduleHistory> findByTaskId(String taskId);

    void changeStatus(String id, SchedulerStatus status);
}
