package org.hswebframework.task.scheduler.history;

import org.hswebframework.task.scheduler.SchedulerStatus;

import java.util.List;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface ScheduleHistoryRepository {

    List<ScheduleHistory> findBySchedulerId(String schedulerId);

    ScheduleHistory save(ScheduleHistory history);

    void changeStatusById(String id, SchedulerStatus status);
}
