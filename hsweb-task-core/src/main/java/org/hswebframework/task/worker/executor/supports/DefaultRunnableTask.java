package org.hswebframework.task.worker.executor.supports;

import org.hswebframework.task.Task;
import org.hswebframework.task.TaskOperationResult;
import org.hswebframework.task.TaskStatus;
import org.hswebframework.task.utils.IdUtils;
import org.hswebframework.task.worker.executor.ExecuteContext;
import org.hswebframework.task.worker.executor.RunnableTask;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class DefaultRunnableTask implements RunnableTask {

    private final    String     id;
    private final    Task       task;
    private final    TaskRunner runner;
    private volatile TaskStatus status;

    public DefaultRunnableTask(Task task, TaskRunner runner) {
        this.task = task;
        this.runner = runner;
        this.id = IdUtils.newUUID();
    }

    @Override
    public TaskOperationResult run() {
        Map<String, Object> parameters = task.getJob().getParameters();
        if (null == parameters) {
            parameters = new HashMap<>();
        }
        ExecuteContext context = new DefaultExecuteContext(parameters);
        status = TaskStatus.running;
        TaskOperationResult result = new TaskOperationResult();
        result.setExecutionId(getId());
        result.setJobId(this.getTask().getJobId());
        result.setTaskId(this.getTask().getId());
        result.setStartTime(System.currentTimeMillis());
        try {
            Object runResult = runner.run(context);
            result.setSuccess(true);
            result.setStatus(TaskStatus.success);
            result.setResult(runResult);
        } catch (Throwable e) {
            result.setSuccess(false);
            result.setErrorName(e.getClass().getName());
            result.setMessage(e.getMessage());
            result.setStatus(TaskStatus.failed);
            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            result.setErrorStack(writer.toString());
        }
        result.setEndTime(System.currentTimeMillis());
        return result;
    }


    @Override
    public String getId() {
        return id;
    }

    @Override
    public Task getTask() {
        return task;
    }

    @Override
    public TaskStatus getStatus() {
        return status;
    }

}
