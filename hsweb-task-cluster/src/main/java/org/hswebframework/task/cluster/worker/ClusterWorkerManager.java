package org.hswebframework.task.cluster.worker;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.task.cluster.ClusterManager;
import org.hswebframework.task.cluster.Topic;
import org.hswebframework.task.worker.TaskWorker;
import org.hswebframework.task.worker.TaskWorkerManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
        worker.startup();
        localWorker.put(worker.getId(), worker);
        //如果不是调度器节点则推送通知
        if (!(worker instanceof SchedulerTaskWorker)) {
            workerJoinTopic.publish(WorkerInfo.of(worker));
        }
        return worker;
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
    public void startup() {
        //worker join
        workerJoinTopic.subscribe(workerInfo -> {
            log.debug("worker join: {}", workerInfo);
            SchedulerTaskWorker worker = new SchedulerTaskWorker(clusterManager, workerInfo.getId());
            register(worker);
        });
        //worker leave
        workerLeaveTopic.subscribe(workerInfo -> {
            log.debug("worker leave: {}", workerInfo);
            localWorker.remove(workerInfo.getId());
        });
    }
}
