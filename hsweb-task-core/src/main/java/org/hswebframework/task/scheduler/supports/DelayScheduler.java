package org.hswebframework.task.scheduler.supports;

import org.hswebframework.task.scheduler.ScheduleContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class DelayScheduler extends AbstractScheduler {

    private ScheduledExecutorService executorService;

    private long delay;

    private TimeUnit timeUnit;

    public ScheduledExecutorService getExecutorService() {
        return executorService;
    }

    public DelayScheduler() {
    }

    public void setExecutorService(ScheduledExecutorService executorService, long delay, TimeUnit timeUnit) {
        this.executorService = executorService;
        this.delay = delay;
        this.timeUnit = timeUnit;
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
            timeList.add(currentTime += timeUnit.toMillis(delay));
        }
        return timeList;
    }

    @Override
    public Map<String, Object> getConfiguration() {
        Map<String, Object> config = new HashMap<>();
        config.put("delay", delay);
        config.put("timeUnit", timeUnit);

        return config;
    }

    public void initFromConfiguration(Map<String, Object> configuration) {
        delay = (long) configuration.get("delay");
        timeUnit = (TimeUnit) configuration.get("timeUnit");
    }

    @Override
    protected void doStart() {
        AtomicReference<ScheduledFuture> futureAtomicReference = new AtomicReference<>();
        futureAtomicReference.set(executorService.schedule(() ->
                fire(consumer -> new ScheduleContext() {
                    @Override
                    public boolean isLastExecute() {
                        return false;
                    }

                    @Override
                    public long getNextExecuteTime() {
                        return DelayScheduler.this.getNextExecuteTime(1).get(0);
                    }

                    @Override
                    public void cancel() {
                        futureAtomicReference.get().cancel(true);
                    }

                    @Override
                    public void lock() {
//                        synchronized (consumer) {
//                            try {
//                                consumer.wait();
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
                    }

                    @Override
                    public void release() {
//                        synchronized (consumer) {
//                            consumer.notify();
//                        }
                    }
                }), delay, timeUnit));
    }
}
