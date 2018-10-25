package org.hswebframework.task.cluster.worker;

import lombok.SneakyThrows;
import org.hswebframework.task.Task;
import org.hswebframework.task.TaskOperationResult;
import org.hswebframework.task.cluster.ClusterManager;
import org.hswebframework.task.cluster.ClusterTask;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class SchedulerTaskExecutor extends ClusterTaskExecutor {

    public SchedulerTaskExecutor(ClusterManager clusterManager, String workerId) {
        super(clusterManager, workerId);
    }

    @Override
    @SneakyThrows
    public String submitTask(Task task, Consumer<TaskOperationResult> resultConsumer) {
        ClusterTask clusterTask = new ClusterTask();
        clusterTask.setRequestId(UUID.randomUUID().toString());
        clusterTask.setTask(task);
        getTaskTopic().publish(clusterTask);
        consumeTaskResult(clusterTask.getRequestId(), resultConsumer);
        return clusterTask.getRequestId();
    }

    @Override
    public void shutdown(boolean force) {

    }
}
