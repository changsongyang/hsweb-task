package org.hswebframework.task.cluster.worker;

import org.hswebframework.task.cluster.ClusterManager;
import org.hswebframework.task.worker.TaskWorker;
import org.hswebframework.task.worker.WorkerStatus;
import org.hswebframework.task.worker.executor.TaskExecutor;

import java.util.Map;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class SchedulerTaskWorker implements TaskWorker {

    private ClusterManager clusterManager;

    private TaskExecutor taskExecutor;

    private Map<String, WorkerInfo> taskInfoMap;

    private String id;

    private volatile WorkerInfo cache;

    private volatile long lastCacheTime;

    public SchedulerTaskWorker(ClusterManager clusterManager, String id) {
        this.clusterManager = clusterManager;
        this.id = id;
    }

    protected WorkerInfo getWorkerInfo() {
        if (System.currentTimeMillis() - lastCacheTime > 100) {
            WorkerInfo info = taskInfoMap.get(id);
            if (null != info) {
                cache = info;
            }
            lastCacheTime = System.currentTimeMillis();
        }
        return cache;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getRegisterId() {
        return getWorkerInfo().getRegisterId();
    }

    @Override
    public String getName() {
        return getWorkerInfo().getName();
    }

    @Override
    public String[] getGroups() {
        return getWorkerInfo().getGroups();
    }

    @Override
    public String getHost() {
        return getWorkerInfo().getHost();
    }

    @Override
    public long getStartupTime() {
        return getWorkerInfo().getStartupTime();
    }

    @Override
    public long getShutdownTime() {
        return getWorkerInfo().getShutdownTime();
    }

    @Override
    public byte getHealth() {
        return getWorkerInfo().getHealth();
    }

    @Override
    public WorkerStatus getStatus() {
        return getWorkerInfo().getStatus();
    }

    @Override
    public TaskExecutor getExecutor() {
        return taskExecutor;
    }

    @Override
    public void shutdown(boolean force) {
        taskExecutor.shutdown(force);
        taskInfoMap.remove(getId());
    }

    @Override
    public void startup() {
        if (taskExecutor == null) {
            taskExecutor = new SchedulerTaskExecutor(clusterManager, getId());
        }
        if (taskInfoMap == null) {
            taskInfoMap = clusterManager.getMap("cluster:workers");
        }
        // init cache
        getWorkerInfo();
    }

    @Override
    public void pause() {
        WorkerInfo workerInfo = getWorkerInfo();
        workerInfo.setStatus(WorkerStatus.pause);
        taskInfoMap.put(getId(), workerInfo);
    }

    @Override
    public void resume() {
        WorkerInfo workerInfo = getWorkerInfo();
        workerInfo.setStatus(WorkerStatus.idle);
        taskInfoMap.put(getId(), workerInfo);
    }
}
