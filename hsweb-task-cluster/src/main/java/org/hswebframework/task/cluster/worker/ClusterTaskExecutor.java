package org.hswebframework.task.cluster.worker;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.task.Task;
import org.hswebframework.task.TaskOperationResult;
import org.hswebframework.task.TaskStatus;
import org.hswebframework.task.TimeoutOperations;
import org.hswebframework.task.cluster.ClusterManager;
import org.hswebframework.task.cluster.ClusterTask;
import org.hswebframework.task.cluster.Queue;
import org.hswebframework.task.worker.executor.TaskExecutor;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
public abstract class ClusterTaskExecutor implements TaskExecutor {
    private final AtomicLong submitted = new AtomicLong();

    private final AtomicLong running = new AtomicLong();

    private final AtomicLong fail = new AtomicLong();

    private final AtomicLong success = new AtomicLong();

    private final AtomicLong waiting = new AtomicLong();

    protected ClusterManager clusterManager;

    private TimeoutOperations timeoutOperations;

    protected String workerId;

    public ClusterTaskExecutor(TimeoutOperations timeoutOperations, ClusterManager clusterManager, String workerId) {
        this.clusterManager = clusterManager;
        this.workerId = workerId;
        this.timeoutOperations = timeoutOperations;
    }

    public Queue<ClusterTask> getTaskQueue() {
        return clusterManager.getQueue("task:accept:" + workerId);
    }

    public void consumeTaskResult(String requestId, Consumer<TaskOperationResult> consumer, Task task) {
        Queue<TaskOperationResult> requestQueue = clusterManager.getQueue("task:result:" + requestId);
        long startTime = System.currentTimeMillis();

        timeoutOperations.doTryAsync(() -> requestQueue.poll(task.getTimeout(), TimeUnit.MILLISECONDS),
                task.getTimeout(),
                TimeUnit.MILLISECONDS,
                (error) -> {
                    TaskOperationResult result = new TaskOperationResult();
                    result.setExecutionId(requestId);
                    result.setMessage(error.getClass().getName() + ":" + error.getMessage());
                    result.setTaskId(task.getId());
                    result.setJobId(task.getJobId());
                    result.setStatus(TaskStatus.timeout);
                    result.setStartTime(startTime);
                    result.setEndTime(System.currentTimeMillis());
                    result.setErrorName(error.getClass().getName());
                    if (error instanceof TimeoutException) {
                        log.debug("wait task[{}] execute response timeout");
                    } else {
                        log.warn("wait task[{}] execute response error", task.getId(), error);
                    }
                    return result;
                }, (result, isTimeout) -> {
                    consumer.accept(result);
                    if (!isTimeout) {
                        requestQueue.close();
                    }
                    log.info("worker[{}] response task result [status={}],requestId={}", workerId, result.getStatus(), requestId);
                });
    }

    public void responseTaskResult(String requestId, TaskOperationResult result) {
        Queue<TaskOperationResult> requestQueue = clusterManager.getQueue("task:result:" + requestId);
        result.setExecutionId(requestId);
        requestQueue.add(result);
    }

    @Override
    public long getSubmitted() {
        return submitted.get();
    }

    @Override
    public long getRunning() {
        return running.get();
    }

    @Override
    public long getFail() {
        return fail.get();
    }

    @Override
    public long getSuccess() {
        return success.get();
    }

    @Override
    public long getWaiting() {
        return waiting.get();
    }

    @Override
    public void startup() {

    }
}
