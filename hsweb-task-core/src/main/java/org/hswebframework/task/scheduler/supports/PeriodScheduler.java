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

    private ScheduledExecutorService executorService;

    private long initialDelay;
    private long period;

    private TimeUnit timeUnit;

    public ScheduledExecutorService getExecutorService() {
        return executorService;
    }

    public PeriodScheduler() {
    }

    public void setExecutorService(ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }

    public PeriodScheduler(ScheduledExecutorService executorService, long initialDelay, long period, TimeUnit timeUnit) {
        this.executorService = executorService;
        this.initialDelay = initialDelay;
        this.timeUnit = timeUnit;
        this.period = period;
    }

    @Override
    public String getType() {
        return "delay";
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
        config.put("initialDelay", initialDelay);
        config.put("period", period);

        config.put("timeUnit", timeUnit);

        return config;
    }

    public void initFromConfiguration(Map<String, Object> configuration) {
        initialDelay = (long) configuration.get("initialDelay");
        period = (long) configuration.get("period");

        timeUnit = (TimeUnit) configuration.get("timeUnit");
    }

    @Override
    protected void doStart() {
        AtomicReference<ScheduledFuture> futureAtomicReference = new AtomicReference<>();
        // 为什么不使用scheduleAtFixedRate?
        // 因为如果在执行间隔小于执行时间时,会导致任务队列堆积.
        futureAtomicReference.set(executorService.schedule(() ->
                fire(new ScheduleContext() {
                    @Override
                    public boolean isLastExecute() {
                        return false;
                    }

                    @Override
                    public long getNextExecuteTime() {
                        return PeriodScheduler.this.getNextExecuteTime(1).get(0);
                    }

                    @Override
                    public void cancel() {
                        futureAtomicReference.get().cancel(true);
                    }

                    @Override
                    public void next() {
                        doStart();
                        cancel();
                    }
                }), period, timeUnit));
    }
}
