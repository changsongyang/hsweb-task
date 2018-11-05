package org.hswebframework.task.scheduler.supports;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.task.scheduler.ScheduleContext;
import org.hswebframework.task.scheduler.Scheduler;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractScheduler implements Scheduler {

    public AbstractScheduler(ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }

    @Getter
    @Setter
    protected ScheduledExecutorService executorService;

    protected List<Consumer<ScheduleContext>> allTriggerListener = new ArrayList<>();

    protected List<Runnable> stopListener = new ArrayList<>();

    protected List<Runnable> cancelListener = new ArrayList<>();

    protected List<Runnable> pauseListener = new ArrayList<>();

    protected AtomicLong fireTimes = new AtomicLong();

    protected volatile boolean started = false;

    protected volatile boolean canceled = false;

    protected abstract void initFromConfiguration(Map<String, Object> configuration);

    protected void fire(ScheduleContext context) {
        if (canceled) {
            return;
        }
        fireTimes.incrementAndGet();
        for (Consumer<ScheduleContext> consumer : allTriggerListener) {
            consumer.accept(context);
        }
    }

    protected void callStopListener() {
        for (Runnable runnable : stopListener) {
            runnable.run();
        }
    }

    protected void callCancelListener() {
        for (Runnable runnable : cancelListener) {
            runnable.run();
        }
    }

    protected void callPauseListener() {
        for (Runnable runnable : pauseListener) {
            runnable.run();
        }
    }

    @Override
    public Scheduler onTriggered(Consumer<ScheduleContext> runnable) {
        allTriggerListener.add(runnable);
        return this;
    }

    @Override
    public Scheduler onStop(Runnable runnable) {
        stopListener.add(runnable);
        return this;
    }

    @Override
    public Scheduler onCancel(Runnable runnable) {
        cancelListener.add(runnable);
        return this;
    }

    @Override
    public Scheduler onPause(Runnable runnable) {
        pauseListener.add(runnable);
        return this;
    }

    @Override
    public Scheduler start() {
        if (!started) {
            started = true;
            doStart();
        }
        return this;
    }

    abstract protected long getNextFireTimestamp();

    protected void doStart() {
        long time = getNextFireTimestamp();
        if (time < 0) {
            return;
        }
        if (canceled) {
            return;
        }
        AtomicReference<ScheduledFuture> futureAtomicReference = new AtomicReference<>();
        Runnable runnable = () -> fire(new ScheduleContext() {

            @Override
            public boolean isLastExecute() {
                return getNextExecuteTime() >= 0;
            }

            @Override
            public long getNextExecuteTime() {
                return getNextFireTimestamp();
            }

            @Override
            public void cancel() {
                if (futureAtomicReference.get() != null) {
                    futureAtomicReference.get().cancel(true);
                }
                futureAtomicReference.set(null);
            }

            @Override
            public void next(boolean currentSuccess) {
                if (canceled) {
                    return;
                }
                doStart();
                cancel();
            }
        });

        long delay = time - System.currentTimeMillis();
        if (delay < 0) {
            log.warn("illegal delay time :{},scheduler:{}", delay, this.toString());
        }
        futureAtomicReference.set(executorService.schedule(
                runnable
                , Math.max(delay, 0)
                , TimeUnit.MILLISECONDS));
    }

    @Override
    public Scheduler stop(boolean force) {
        callStopListener();
        return this;
    }

    @Override
    public Scheduler cancel(boolean force) {
        callCancelListener();
        canceled = true;
        return this;
    }

    @Override
    public Scheduler pause() {
        callPauseListener();
        return this;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + getConfiguration().toString();
    }
}
