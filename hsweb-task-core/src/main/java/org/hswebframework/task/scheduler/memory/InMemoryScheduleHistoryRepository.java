package org.hswebframework.task.scheduler.memory;

import org.hswebframework.task.scheduler.SchedulerStatus;
import org.hswebframework.task.scheduler.history.ScheduleHistory;
import org.hswebframework.task.scheduler.history.ScheduleHistoryRepository;
import org.hswebframework.task.utils.IdUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class InMemoryScheduleHistoryRepository implements ScheduleHistoryRepository {

    private Map<String, ScheduleHistory> histories = new ConcurrentHashMap<>();

    @Override
    public List<ScheduleHistory> findBySchedulerId(String schedulerId, SchedulerStatus... statuses) {
        List<SchedulerStatus> statusList = Arrays.asList(statuses);
        return histories.values().stream()
                .filter(history -> history.getSchedulerId().equals(schedulerId))
                .filter(history -> statuses.length == 0 || statusList.contains(history.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public ScheduleHistory save(ScheduleHistory history) {
        if (history.getId() == null) {
            history.setId(IdUtils.newUUID());
        }
        histories.put(history.getId(), history);
        return history;
    }

    @Override
    public ScheduleHistory findById(String id) {
        return histories.get(id);
    }

    @Override
    public List<ScheduleHistory> findByTaskId(String taskId) {
        return histories.values().stream()
                .filter(history -> history.getTaskId().equals(taskId))
                .collect(Collectors.toList());
    }

    @Override
    public void changeStatus(String id, SchedulerStatus status) {
        Optional.ofNullable(histories.get(id))
                .ifPresent(history -> {
                    history.setStatus(status);
                    history.setUpdateTime(System.currentTimeMillis());
                });
    }
}
