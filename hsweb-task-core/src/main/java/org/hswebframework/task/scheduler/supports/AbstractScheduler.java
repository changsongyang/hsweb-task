package org.hswebframework.task.scheduler.supports;

import lombok.Getter;
import org.hswebframework.task.scheduler.ScheduleContext;
import org.hswebframework.task.scheduler.Scheduler;
import org.hswebframework.task.scheduler.SchedulerFactoryProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public abstract class AbstractScheduler implements Scheduler {

    protected List<Consumer<ScheduleContext>> allTriggerListener = new ArrayList<>();

    protected List<Runnable> stopListener = new ArrayList<>();

    protected List<Runnable> cancelListener = new ArrayList<>();

    protected List<Runnable> pauseListener = new ArrayList<>();

    protected boolean started = false;

    protected abstract void initFromConfiguration(Map<String, Object> configuration);

    protected void fire(Function<Consumer<ScheduleContext>, ScheduleContext> contextGetter) {
        for (Consumer<ScheduleContext> consumer : allTriggerListener) {
            consumer.accept(contextGetter.apply(consumer));
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
        started = true;
        doStart();
        return this;
    }

    protected abstract void doStart();

    @Override
    public Scheduler stop(boolean force) {
        callStopListener();
        return this;
    }

    @Override
    public Scheduler cancel(boolean force) {
        callCancelListener();
        return this;
    }

    @Override
    public Scheduler pause() {
        callPauseListener();
        return this;
    }


}
