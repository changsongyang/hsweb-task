package org.hswebframework.task.cluster.worker;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.task.TimeoutOperations;
import org.hswebframework.task.cluster.ClusterManager;
import org.hswebframework.task.worker.DefaultTaskWorker;
import org.hswebframework.task.worker.executor.TaskExecutor;

import java.util.Map;

import static org.hswebframework.task.worker.WorkerStatus.busy;
import static org.hswebframework.task.worker.WorkerStatus.shutdown;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
public class ClusterNodeTaskWorker extends DefaultTaskWorker {

    private Map<String, WorkerInfo> workerInfoMap;

    private WorkerTaskExecutor workerTaskExecutor;

    private ClusterManager clusterManager;

    public ClusterNodeTaskWorker(String id, TimeoutOperations timeoutOperations, ClusterManager clusterManager, TaskExecutor executor) {
        workerTaskExecutor = new WorkerTaskExecutor(timeoutOperations, clusterManager, id, executor);
        this.clusterManager = clusterManager;
        super.setExecutor(workerTaskExecutor);
        super.setId(id);
        this.workerInfoMap = clusterManager.getMap("cluster:workers");
    }

    @Override
    public void startup() {
        workerTaskExecutor.startup();
        super.startup();
        Thread heartbeatThread = new Thread(() -> {
            boolean first = true;
            for (; getStatus() != shutdown; ) {
                try {
                    WorkerInfo workerInfo = WorkerInfo.of(this);
                    workerInfo.setLastHeartbeatTime(System.currentTimeMillis());
                    WorkerInfo old = workerInfoMap.put(workerInfo.getId(), workerInfo);
                    if (old == null && !first) {
                        clusterManager
                                .getTopic(WorkerInfo.class,"cluster:worker:join")
                                .publish(workerInfo);
                    }
                    Thread.sleep(1000);
                    first = false;
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
        heartbeatThread.setName("cluster-worker-node-heartbeat-thread");
        heartbeatThread.setDaemon(false);
        heartbeatThread.start();
    }
}
