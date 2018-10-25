package org.hswebframework.task.worker.executor.supports;

import lombok.SneakyThrows;
import org.hswebframework.task.Task;
import org.hswebframework.task.TaskOperationResult;
import org.hswebframework.task.worker.executor.RunnableTask;
import org.hswebframework.task.worker.executor.RunnableTaskBuilder;
import org.hswebframework.task.worker.executor.TaskExecutor;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class ThreadPoolTaskExecutor implements TaskExecutor {

    private final AtomicLong submitted = new AtomicLong();

    private final AtomicLong running = new AtomicLong();

    private final AtomicLong fail = new AtomicLong();

    private final AtomicLong success = new AtomicLong();

    private final AtomicLong waiting = new AtomicLong();

    private ExecutorService executorService;

    private RunnableTaskBuilder taskBuilder;

    public ThreadPoolTaskExecutor(RunnableTaskBuilder taskBuilder) {
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        this.taskBuilder = taskBuilder;
    }

    public ThreadPoolTaskExecutor(ExecutorService executorService, RunnableTaskBuilder taskBuilder) {
        this.executorService = executorService;
        this.taskBuilder = taskBuilder;
    }

    @Override
    @SneakyThrows
    public String submitTask(Task task, Consumer<TaskOperationResult> resultConsumer) {
        RunnableTask runnableTask = taskBuilder.build(task);
        waiting.incrementAndGet();
        executorService.submit(() -> {
            waiting.decrementAndGet();
            running.incrementAndGet();
            TaskOperationResult result = runnableTask.run();
            resultConsumer.accept(result);
            if (result.isSuccess()) {
                success.incrementAndGet();
            } else {
                fail.incrementAndGet();
            }
        });
        submitted.incrementAndGet();
        return runnableTask.getId();
    }

    @Override
    public void shutdown(boolean force) {
        executorService.shutdown();
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
