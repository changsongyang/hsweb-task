package org.hswebframework.task.worker.executor.supports;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.task.Task;
import org.hswebframework.task.worker.executor.ExecuteContext;
import org.hswebframework.task.worker.executor.RunnableTask;
import org.hswebframework.task.worker.executor.RunnableTaskBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
public class DefaultRunnableTaskBuilder implements RunnableTaskBuilder {

    private Map<String, RunnableTaskBuilderProvider> providers = new HashMap<>();

    private Map<String, TaskCache> cache = new ConcurrentHashMap<>();

    public DefaultRunnableTaskBuilder addProvider(RunnableTaskBuilderProvider provider) {
        providers.put(provider.getSupportTaskType(), provider);
        return this;
    }

    protected TaskRunner createTaskRunner(Task task) {
        String type = task.getJob().getJobType();
        log.debug("create {} RunnableTask[{}]", type, task.getId());
        RunnableTaskBuilderProvider provider = providers.get(type);
        if (provider == null) {
            return new AlwaysFailRunner(new UnsupportedOperationException("unsupported job type:" + task.getJob().getJobType()));
        }
        try {
            return provider.build(task.getJob().getContent());
        } catch (Exception e) {
            return new AlwaysFailRunner(e);
        }
    }

    static class AlwaysFailRunner implements TaskRunner {
        private Throwable error;

        AlwaysFailRunner(Throwable error) {
            this.error = error;
        }

        @Override
        public Object run(ExecuteContext context) throws Throwable {
            throw error;
        }

        @Override
        public void clear() {
            error = null;
        }
    }

    @Override
    public RunnableTask build(Task task) {
        TaskCache cached = cache.get(task.getId());
        if (cached != null && cached.taskHash == task.hashCode()) {
            return new DefaultRunnableTask(task, cached.taskRunner);
        } else {
            TaskCache old = cache.remove(task.getId());
            if (null != old) {
                //清除runner
                old.taskRunner.clear();
            }
        }
        TaskRunner runner = createTaskRunner(task);
        //如果是错误的runner,则不进行缓存,直接返回
        if (runner instanceof AlwaysFailRunner) {
            return new DefaultRunnableTask(task, runner);
        }
        TaskCache taskCache = new TaskCache();
        taskCache.taskRunner = runner;
        taskCache.taskHash = task.hashCode();
        cache.put(task.getId(), taskCache);
        return new DefaultRunnableTask(task, runner);
    }

    class TaskCache {
        TaskRunner taskRunner;

        long taskHash;
    }
}
