package org.hswebframework.task.worker;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.task.utils.IdUtils;
import org.hswebframework.task.worker.executor.TaskExecutor;

import java.util.Objects;
import java.util.UUID;

/**
 * @author zhouhao
 * @since 1.0.0
 */

public class DefaultTaskWorker implements TaskWorker {

    @Getter
    @Setter
    private String id;

    @Getter
    @Setter
    private String registerId;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String[] groups;

    @Getter
    @Setter
    private String host;

    @Getter
    private long startupTime;

    @Getter
    private long shutdownTime;

    private WorkerStatus status = WorkerStatus.idle;

    private TaskExecutor executor;


    protected void setExecutor(TaskExecutor executor) {
        this.executor = executor;
    }

    public DefaultTaskWorker() {
    }

    public DefaultTaskWorker(TaskExecutor executor) {
        this.executor = executor;
    }

    @Override
    public void startup() {
        if (registerId == null) {
            registerId = IdUtils.newUUID();
        }
        validate();
        startupTime = System.currentTimeMillis();
    }

    protected void validate() {
        Objects.requireNonNull(getId(), "id can not be null");
        Objects.requireNonNull(getGroups(), "groups can not be null");
        Objects.requireNonNull(getRegisterId(), "registerId can not be null");
    }

    @Override
    public byte getHealth() {
        return getStatus().getHealthScore();
    }

    @Override
    public WorkerStatus getStatus() {
        if (status.getHealthScore() <= 0) {
            return status;
        }
        checkStatus();
        return status;
    }

    protected void checkStatus() {
        if (executor.getWaiting() > 0) {
            status = WorkerStatus.busy;
        } else {
            status = WorkerStatus.idle;
        }
    }

    @Override
    public TaskExecutor getExecutor() {
        return executor;
    }

    @Override
    public void shutdown(boolean force) {
        shutdownTime = System.currentTimeMillis();
        status = WorkerStatus.pause;
        executor.shutdown(force);
        status = WorkerStatus.shutdown;
    }

    @Override
    public void pause() {
        status = WorkerStatus.pause;
    }

    @Override
    public void resume() {
        checkStatus();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+"(id="+id+",name="+name+",groups="+(groups==null?null:String.join(",",groups))+")";
    }
}
