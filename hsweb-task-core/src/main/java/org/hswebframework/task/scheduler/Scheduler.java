package org.hswebframework.task.scheduler;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 调度器,用于执行实际的任务
 *
 * @author zhouhao
 * @see Schedulers
 * @see org.hswebframework.task.scheduler.supports.AbstractScheduler
 * @see org.hswebframework.task.scheduler.supports.CronScheduler
 * @see org.hswebframework.task.scheduler.supports.PeriodScheduler
 * @see SchedulerFactory
 * @see SchedulerFactoryProvider
 * @since 1.0.0
 */
public interface Scheduler {
    /**
     * @return 调度器类型
     * @see SchedulerFactoryProvider#getSupportType()
     */
    String getType();

    /**
     * 获取未来n次的执行时间戳,此时间并不一定是实际的时间.
     * 具体时间以任务实际执行的时间为准.
     *
     * @param times 要获取的次数
     * @return 每一次执行的具体时间
     * @see Date#getTime()
     * @see Instant#toEpochMilli()
     */
    List<Long> getNextExecuteTime(int times);

    /**
     * 注册一个调度触发监听器,在执行时间到达时,将会调用注册的事件
     *
     * @param runnable 事件执行器
     * @return 调度器自身
     * @see ScheduleContext
     * @see Consumer#accept(Object)
     */
    Scheduler onTriggered(Consumer<ScheduleContext> runnable);

    /**
     * 注册一个调度器停止监听器
     *
     * @param runnable 当调度器停止时执行
     * @return 调度器自身
     * @see this#stop(boolean)
     */
    Scheduler onStop(Runnable runnable);

    /**
     * 注册一个调度器取消监听器
     *
     * @param runnable 当调度器取消时执行
     * @return 调度器自身
     * @see this#cancel(boolean)
     */
    Scheduler onCancel(Runnable runnable);

    /**
     * 注册一个调度器暂停监听器
     *
     * @param runnable 当调度器暂停时执行
     * @return 调度器自身
     * @see this#pause()
     */
    Scheduler onPause(Runnable runnable);

    /**
     * 启动调度器
     *
     * @return 调度器自身
     */
    Scheduler start();

    /**
     * 停止调度
     *
     * @param force 是否强制停止
     * @return 调度器自身
     */
    Scheduler stop(boolean force);

    /**
     * 取消调度
     *
     * @param force 是否强制取消
     * @return 调度器自身
     */
    Scheduler cancel(boolean force);

    /**
     * 暂停调度
     *
     * @return 调度器自身
     */
    Scheduler pause();

    /**
     * 返回调度器配置信息,可使用此配置,并通过{@link SchedulerFactory#create(Map)}进行创建相同当配置.
     * 例如:
     * <pre>
     *     schedulerFactory.create({"type":"cron","cron":"0/10 * * * * ?"})
     * </pre>
     *
     * @return 配置内容, 不会为空
     * @see SchedulerFactory#create(Map)
     * @see SchedulerFactoryProvider#create(Map)
     * @see org.hswebframework.task.scheduler.supports.AbstractScheduler#initFromConfiguration(Map)
     * @see org.hswebframework.task.scheduler.supports.CronScheduler
     */
    Map<String, Object> getConfiguration();
}
