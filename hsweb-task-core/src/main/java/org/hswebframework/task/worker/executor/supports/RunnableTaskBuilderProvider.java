package org.hswebframework.task.worker.executor.supports;


/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface RunnableTaskBuilderProvider {

    String getSupportTaskType();

    TaskRunner build(String content) throws Exception;
}
