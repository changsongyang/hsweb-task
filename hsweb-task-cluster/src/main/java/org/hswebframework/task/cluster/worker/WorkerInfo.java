package org.hswebframework.task.cluster.worker;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hswebframework.task.worker.TaskWorker;
import org.hswebframework.task.worker.WorkerStatus;

import java.io.Serializable;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
@ToString
public class WorkerInfo implements Serializable {

    private String id;

    private String registerId;

    private String name;

    private String[] groups;

    private String host;

    private long startupTime;

    private long shutdownTime;

    private byte health;

    private WorkerStatus status;

    private long lastHeartbeatTime;

    public static WorkerInfo of(TaskWorker worker) {
        WorkerInfo info = new WorkerInfo();
        info.setId(worker.getId());
        info.setRegisterId(worker.getRegisterId());
        info.setGroups(worker.getGroups());
        info.setName(worker.getName());
        info.setStartupTime(worker.getStartupTime());
        info.setShutdownTime(worker.getShutdownTime());
        info.setStatus(worker.getStatus());
        info.setLastHeartbeatTime(worker.getStartupTime());
        info.setHealth(worker.getHealth());
        info.setHost(worker.getHost());
        return info;
    }
}
