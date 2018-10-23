package org.hswebframework.task.scheduler;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 调度器
 *
 * @author zhouhao
 * @since 1.0.0
 */
public interface Scheduler {
    String getType();

    List<Long> getNextExecuteTime(int times);

    Scheduler onTriggered(Consumer<ScheduleContext> runnable);

    Scheduler onStop(Runnable runnable);

    Scheduler onCancel(Runnable runnable);

    Scheduler onPause(Runnable runnable);

    Scheduler start();

    Scheduler stop(boolean force);

    Scheduler cancel(boolean force);

    Scheduler pause();

    Map<String, Object> getConfiguration();
}
