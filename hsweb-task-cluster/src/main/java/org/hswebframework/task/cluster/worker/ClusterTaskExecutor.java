package org.hswebframework.task.cluster.worker;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.task.TaskOperationResult;
import org.hswebframework.task.cluster.ClusterManager;
import org.hswebframework.task.cluster.ClusterTask;
import org.hswebframework.task.cluster.Topic;
import org.hswebframework.task.worker.executor.TaskExecutor;

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

    protected String workerId;

    public ClusterTaskExecutor(ClusterManager clusterManager, String workerId) {
        this.clusterManager = clusterManager;
        this.workerId = workerId;
    }

    public Topic<ClusterTask> getTaskTopic() {
        return clusterManager.getTopic("task:accept:" + workerId);
    }

    public void consumeTaskResult(String requestId, Consumer<TaskOperationResult> consumer) {
        Topic<TaskOperationResult> resultTopic = clusterManager.getTopic("task:result:" + requestId);
        resultTopic.subscribe(result -> {
            consumer.accept(result);
            resultTopic.close();
            log.info("worker[{}] response task result [status={}],requestId={}",workerId, result.getStatus(), requestId);
        });
    }

    public void responseTaskResult(String requestId, TaskOperationResult result) {
        Topic<TaskOperationResult> resultTopic = clusterManager.getTopic("task:result:" + requestId);
        resultTopic.publish(result);
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
