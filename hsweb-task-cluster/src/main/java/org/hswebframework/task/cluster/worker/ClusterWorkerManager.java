package org.hswebframework.task.cluster.worker;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.task.cluster.ClusterManager;
import org.hswebframework.task.cluster.Topic;
import org.hswebframework.task.worker.TaskWorker;
import org.hswebframework.task.worker.TaskWorkerManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
public class ClusterWorkerManager implements TaskWorkerManager {

    private ClusterManager          clusterManager;
    private Map<String, WorkerInfo> clusterWorkerInfoList;
    private Topic<WorkerInfo>       workerJoinTopic;
    private Topic<WorkerInfo>       workerLeaveTopic;

    private Map<String, TaskWorker> localWorker = new ConcurrentHashMap<>();

    public ClusterWorkerManager(ClusterManager clusterManager) {
        this.clusterManager = clusterManager;
        clusterWorkerInfoList = clusterManager.getMap("cluster:workers");
        workerJoinTopic = clusterManager.getTopic("cluster:worker:join");
        workerLeaveTopic = clusterManager.getTopic("cluster:worker:leave");
    }

    @Override
    public TaskWorker getWorkerById(String id) {
        return localWorker.get(id);
    }

    @Override
    public List<TaskWorker> getAllWorker() {
        return new ArrayList<>(localWorker.values());
    }

    @Override
    public TaskWorker select(String group) {
        return getAllWorker()
                .stream()
                .filter(worker -> {
                    if (group == null || group.length() == 0) {
                        return true;
                    }
                    return worker.getHealth() > 0 && Arrays.asList(worker.getGroups()).contains(group);
                })
                .max(Comparator.comparingInt(TaskWorker::getHealth))
                .orElse(null);
    }

    @Override
    public TaskWorker register(TaskWorker worker) {
        doRegister(worker);
        //如果不是调度器节点则推送通知
        if (!(worker instanceof SchedulerTaskWorker)) {
            workerJoinTopic.publish(WorkerInfo.of(worker));
        }
        return worker;
    }

    public void doRegister(TaskWorker worker) {
        worker.startup();
        localWorker.put(worker.getId(), worker);
    }

    @Override
    public TaskWorker unregister(String id, boolean force) {
        TaskWorker worker = localWorker.get(id);

        if (null != worker) {
            worker.shutdown(force);
            WorkerInfo workerInfo = clusterWorkerInfoList.getOrDefault(id, WorkerInfo.of(worker));
            workerInfo.setShutdownTime(System.currentTimeMillis());
            workerLeaveTopic.publish(workerInfo);
            clusterWorkerInfoList.remove(id);
            localWorker.remove(id);

        }
        return worker;
    }

    @Override
    public void shutdown() {
        getAllWorker()
                .stream()
                .filter(worker -> !(worker instanceof SchedulerTaskWorker))
                .map(TaskWorker::getId)
                .forEach(id -> this.unregister(id, false));
    }

    @Override
    public void startup() {

        Consumer<WorkerInfo> joinWorker = workerInfo -> {
            if (System.currentTimeMillis() - workerInfo.getLastHeartbeatTime() > TimeUnit.SECONDS.toMillis(30)) {
                clusterWorkerInfoList.remove(workerInfo.getId());
                log.debug("worker[{}] is dead ", workerInfo.getId());
                return;
            }
            TaskWorker oldWorker = localWorker.get(workerInfo.getId());
            if (oldWorker != null && !(oldWorker instanceof SchedulerTaskWorker)) {
                return;
            }
            log.debug("worker join: {}", workerInfo);
            SchedulerTaskWorker worker = new SchedulerTaskWorker(clusterManager, workerInfo.getId());
            doRegister(worker);
        };
        //worker join
        workerJoinTopic.subscribe(joinWorker);
        //worker leave
        workerLeaveTopic.subscribe(workerInfo -> {
            log.debug("worker leave: {}", workerInfo);
            localWorker.remove(workerInfo.getId());
        });
        clusterWorkerInfoList
                .values()
                .forEach(joinWorker);
    }
}
