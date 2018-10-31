package org.hswebframework.task.cluster.worker;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.task.Task;
import org.hswebframework.task.TaskOperationResult;
import org.hswebframework.task.TaskStatus;
import org.hswebframework.task.TimeoutOperations;
import org.hswebframework.task.cluster.ClusterManager;
import org.hswebframework.task.cluster.ClusterTask;
import org.hswebframework.task.utils.IdUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
public class SchedulerTaskExecutor extends ClusterTaskExecutor {

    public SchedulerTaskExecutor(TimeoutOperations timeoutOperations, ClusterManager clusterManager, String workerId) {
        super(timeoutOperations,clusterManager, workerId);
    }

    @Override
    @SneakyThrows
    public String submitTask(Task task, Consumer<TaskOperationResult> resultConsumer) {
        ClusterTask clusterTask = new ClusterTask();
        clusterTask.setRequestId(IdUtils.newUUID());
        clusterTask.setTask(task);
        try {
            log.info("wait worker[{}] response task result,requestId={}", workerId, clusterTask.getRequestId());
            consumeTaskResult(clusterTask.getRequestId(), resultConsumer,task);
            log.info("task published to worker[{}]", workerId);
            boolean success = getTaskQueue().add(clusterTask);
            log.info("task published to worker[{}]:{}", workerId, success ? "success" : "fail");
            if (!success) {
                TaskOperationResult error = new TaskOperationResult();
                error.setTaskId(task.getId());
                error.setJobId(task.getJobId());
                error.setStatus(TaskStatus.cancel);
                error.setMessage("未能正确选择worker");
                resultConsumer.accept(error);
            }
        } catch (Exception e) {
            TaskOperationResult error = new TaskOperationResult();
            error.setMessage(e.getMessage());
            error.setErrorName(e.getClass().getName());
            error.setExecutionId(clusterTask.getRequestId());
            error.setStatus(TaskStatus.failed);
            error.setTaskId(task.getId());
            error.setJobId(task.getJobId());
            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            error.setErrorStack(writer.toString());
        }
        return clusterTask.getRequestId();
    }

    @Override
    public void shutdown(boolean force) {

    }
}
