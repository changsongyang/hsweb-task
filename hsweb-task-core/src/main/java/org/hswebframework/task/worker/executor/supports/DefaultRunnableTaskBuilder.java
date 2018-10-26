package org.hswebframework.task.worker.executor.supports;

import org.hswebframework.task.Task;
import org.hswebframework.task.worker.executor.RunnableTask;
import org.hswebframework.task.worker.executor.RunnableTaskBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class DefaultRunnableTaskBuilder implements RunnableTaskBuilder {

    private Map<String, RunnableTaskBuilderProvider> providers = new HashMap<>();

    public DefaultRunnableTaskBuilder addProvider(RunnableTaskBuilderProvider provider) {
        providers.put(provider.getSupportTaskType(), provider);
        return this;
    }

    @Override
    public RunnableTask build(Task task) throws Exception {
        String type = task.getJob().getTaskType();
        RunnableTaskBuilderProvider provider = providers.get(type);
        if (provider == null) {
            return new DefaultRunnableTask(task, (context) -> {
                throw new UnsupportedOperationException("un support task type:" + task.getJob().getTaskType());
            });
        }

        TaskRunner runner = provider.build(task.getJob().getContent());

        return new DefaultRunnableTask(task, runner);
    }
}
