package org.hswebframework.task.cluster.redisson.repository;

import org.hswebframework.task.scheduler.SchedulerStatus;
import org.hswebframework.task.scheduler.history.ScheduleHistory;
import org.hswebframework.task.scheduler.history.ScheduleHistoryRepository;
import org.hswebframework.task.utils.IdUtils;
import org.redisson.api.RMap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class RedissonScheduleHistoryRepository implements ScheduleHistoryRepository {
    private RMap<String, ScheduleHistory> histories;

    public RedissonScheduleHistoryRepository(RMap<String, ScheduleHistory> histories) {
        this.histories = histories;
    }

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
                    histories.put(id, history);
                });
    }
}
