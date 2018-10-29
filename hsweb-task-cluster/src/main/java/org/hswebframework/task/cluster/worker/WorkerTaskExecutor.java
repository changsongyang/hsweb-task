package org.hswebframework.task.cluster.worker;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.task.Task;
import org.hswebframework.task.TaskOperationResult;
import org.hswebframework.task.cluster.ClusterManager;
import org.hswebframework.task.worker.executor.TaskExecutor;

import java.util.function.Consumer;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
public class WorkerTaskExecutor extends ClusterTaskExecutor {

    private TaskExecutor localExecutor;

    public WorkerTaskExecutor(ClusterManager clusterManager, String workerId, TaskExecutor localExecutor) {
        super(clusterManager, workerId);
        this.localExecutor = localExecutor;
    }

    @Override
    public String submitTask(Task task, Consumer<TaskOperationResult> resultConsumer) {
        return localExecutor.submitTask(task, resultConsumer);
    }

    public void startup() {
        getTaskQueue()
                .consume(clusterTask -> {//订阅任务
                    log.info("worker [{}] accept cluster task ,taskId={},requestId={}", workerId, clusterTask.getTask().getId(), clusterTask.getRequestId());
                    submitTask(clusterTask.getTask(), //提交到本地任务
                            result -> responseTaskResult(clusterTask.getRequestId(), result));
                });
    }


    @Override
    public void shutdown(boolean force) {
        localExecutor.shutdown(force);
    }
}
