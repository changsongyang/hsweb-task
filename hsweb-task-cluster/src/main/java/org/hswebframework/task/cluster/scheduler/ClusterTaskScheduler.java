package org.hswebframework.task.cluster.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.task.cluster.ClusterManager;
import org.hswebframework.task.cluster.Queue;
import org.hswebframework.task.cluster.Topic;
import org.hswebframework.task.lock.Lock;
import org.hswebframework.task.scheduler.DefaultTaskScheduler;
import org.hswebframework.task.scheduler.SchedulerStatus;
import org.hswebframework.task.scheduler.history.ScheduleHistory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 分布式,集群下的任务调度管理器,提供对多个任务调度器实例的支持以及管理.
 *
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
public class ClusterTaskScheduler extends DefaultTaskScheduler {

    private ClusterManager clusterManager;

    private Map<String, TaskSchedulerInfo> registry;

    private Topic<TaskSchedulerInfo> schedulerDownTopic;

    private Topic<TaskSchedulerInfo> schedulerUpTopic;

    private TaskSchedulerInfo schedulerInfo;

    public ClusterTaskScheduler(ClusterManager clusterManager) {
        this.clusterManager = clusterManager;
    }

    @Override
    public String getId() {
        return getSchedulerId();
    }

    @Override
    public void shutdown(boolean force) {
        super.shutdown(force);
        TaskSchedulerInfo thisScheduler = registry.remove(getSchedulerId());
        if (null != thisScheduler) {
            schedulerDownTopic.publish(thisScheduler);
        }
    }

    protected Queue<ScheduleOperationRequest> getOperationRequestQueue(String schedulerId) {
        return clusterManager.getQueue("cluster:schedule-operation:" + schedulerId);
    }

    @Override
    protected boolean tryCancelNotExistsScheduler(ScheduleHistory history) {
        return getOperationRequestQueue(history.getSchedulerId())
                .add(ScheduleOperationRequest.of(ScheduleOperationRequest.Operation.cancel, history.getId()));
    }

    @Override
    protected boolean tryPauseNotExistsScheduler(ScheduleHistory history) {
        return getOperationRequestQueue(history.getSchedulerId())
                .add(ScheduleOperationRequest.of(ScheduleOperationRequest.Operation.pause, history.getId()));
    }

    @Override
    protected boolean tryStartNotExistsScheduler(ScheduleHistory history) {
        return getOperationRequestQueue(history.getSchedulerId())
                .add(ScheduleOperationRequest.of(ScheduleOperationRequest.Operation.start, history.getId()));
    }

    @Override
    public void startup() {
        super.startup();
        registry = clusterManager.getMap("cluster:scheduler:registry");

        schedulerDownTopic = clusterManager.getTopic("cluster:scheduler:down");
        schedulerUpTopic = clusterManager.getTopic("cluster:scheduler:up");

        //接收来自其他scheduler发来的请求
        getOperationRequestQueue(getSchedulerId())
                .consume(request -> {
                    if (runningSchedulerMap.get(request.getScheduleId()) == null) {
                        log.warn("accept not running schedule operation request :{}", request);
                        return;
                    }
                    log.debug("accept schedule operation request :{}", request);
                    request.getOperation().execute(this, request.getScheduleId());
                });

        //其他调度节点上线了,将竞争到的任务归还到此节点
        schedulerUpTopic.subscribe(taskSchedulerInfo -> {
            if (taskSchedulerInfo.getId().equals(getSchedulerId())) {
                return;
            }
            log.debug("scheduler[{}] up", taskSchedulerInfo.getId());
            //尝试归还调度
            runningSchedulerMap.values()
                    .stream()
                    .filter(runningScheduler -> {
                        ScheduleHistory history = getHistoryRepository().findById(runningScheduler.getHistoryId());
                        return history.getSchedulerId().equals(taskSchedulerInfo.getId())
                                || history.getCreateSchedulerId().equals(taskSchedulerInfo.getId());
                    })
                    //取消本地任务
                    .peek(runningScheduler -> cancel(runningScheduler.getHistoryId(), true))
                    .map(runningScheduler -> ReturnScheduleRequest.of(runningScheduler.getHistoryId(), runningScheduler.getScheduler().getConfiguration()))
                    .forEach(request -> {
                        log.debug("return schedule[{}] to [{}]", request.getScheduleId(), taskSchedulerInfo.getId());
                        clusterManager.getQueue("cluster:schedule:return:" + taskSchedulerInfo.getId()).add(request);
                    });
        });
        //有其他的调度器节点下线,尝试竞争此节点上的调度任务
        schedulerDownTopic.subscribe(schedulerInfo -> {
            if (schedulerInfo.getId().equals(getSchedulerId())) {
                return;
            }
            log.debug("scheduler[{}] down", schedulerInfo.getId());
            for (; ; ) {
                Lock lock = getLockManager().tryGetLock("cluster:scheduler:compete-lock", 10, TimeUnit.SECONDS);
                try {
                    //尝试竞争挂掉了的调度节点的任务
                    List<ScheduleHistory> histories = getHistoryRepository()
                            .findBySchedulerId(schedulerInfo.getId(),
                                    SchedulerStatus.running,
                                    SchedulerStatus.cancel);
                    //一次竞争5个
                    long count = histories.stream()
                            .limit(5)
                            .filter(history -> !runningSchedulerMap.containsKey(history.getId()))
                            .peek(this::doStart)
                            .count();
                    if (count == 0) {
                        return;
                    }
                    log.debug("compete {} tasks", count);
                } finally {
                    lock.release();
                }
            }
        });
        //接收归还的调度
        clusterManager.<ReturnScheduleRequest>getQueue("cluster:schedule:return:" + getSchedulerId())
                .consume(o -> {
                    ScheduleHistory history = getHistoryRepository().findById(o.getScheduleId());
                    if (history != null) {
                        log.debug("accept return schedule[{}]", o.getScheduleId());
                        doStart(history);
                    }
                });
        //尝试注册自己到注册中心
        schedulerInfo = registry.get(getSchedulerId());
        if (null == schedulerInfo) {
            schedulerInfo = TaskSchedulerInfo.builder()
                    .id(getSchedulerId())
                    .uptime(System.currentTimeMillis())
                    .heartbeatTime(System.currentTimeMillis())
                    .build();
            registry.put(getSchedulerId(), schedulerInfo);
        }
        schedulerUpTopic.publish(schedulerInfo);

        //每2秒进行心跳以及集群内节点检测
        getExecutorService().scheduleAtFixedRate(() -> {
            //更新当前节点信息
            schedulerInfo.setHeartbeatTime(System.currentTimeMillis());
            registry.put(getSchedulerId(), schedulerInfo);

            //如果存在多个调度器,则进行检查
            if (registry.size() > 1) {
                Lock lock = getLockManager().tryGetLock("cluster:scheduler:registry:check-lock", 5, TimeUnit.SECONDS);
                try {
                    for (TaskSchedulerInfo value : registry.values()) {
                        //20秒内没有进行心跳则认为已经失效
                        if (System.currentTimeMillis() - value.getHeartbeatTime() > TimeUnit.SECONDS.toMillis(20)) {
                            registry.remove(value.getId());
                            //推送调度器失效事件
                            schedulerDownTopic.publish(value);
                        }
                    }
                } finally {
                    lock.release();
                }
            }

        }, 1, 10, TimeUnit.SECONDS);
    }
}
