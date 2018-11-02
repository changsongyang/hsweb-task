package org.hswebframework.task.scheduler.supports;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.task.scheduler.ScheduleContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 周期性调度器,使用{@link ScheduledExecutorService}实现
 *
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
public class PeriodScheduler extends AbstractScheduler {

    private long     initialDelay;
    private long     period;
    private TimeUnit timeUnit;

    public PeriodScheduler(ScheduledExecutorService executorService) {
        super(executorService);
    }

    public void setExecutorService(ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }

    public PeriodScheduler(ScheduledExecutorService executorService, long initialDelay, long period, TimeUnit timeUnit) {
        super(executorService);
        this.executorService = executorService;
        this.initialDelay = initialDelay;
        this.timeUnit = timeUnit;
        this.period = period;
    }

    @Override
    public String getType() {
        return PeriodSchedulerProvider.TYPE;
    }

    @Override
    public List<Long> getNextExecuteTime(int times) {
        List<Long> timeList = new ArrayList<>(times);
        long currentTime = System.currentTimeMillis();
        for (int i = 0; i < times; i++) {
            timeList.add(currentTime += timeUnit.toMillis(period));
        }
        return timeList;
    }

    @Override
    public Map<String, Object> getConfiguration() {
        Map<String, Object> config = new HashMap<>();
        config.put("type", getType());
        config.put("initialDelay", initialDelay);
        config.put("period", period);
        config.put("timeUnit", timeUnit);

        return config;
    }

    public void initFromConfiguration(Map<String, Object> configuration) {
        initialDelay = (long) configuration.get("initialDelay");
        period = (long) configuration.get("period");
        timeUnit = (TimeUnit) configuration.getOrDefault("timeUnit", TimeUnit.MILLISECONDS);
    }

    @Override
    protected long getNextFireTimestamp() {
        if (fireTimes.get() == 0) {
            return System.currentTimeMillis() + timeUnit.toMillis(initialDelay);
        }
        return System.currentTimeMillis() + timeUnit.toMillis(period);
    }

}
