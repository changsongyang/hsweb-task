package org.hswebframework.task.worker.executor;

import java.util.Map;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface ExecuteContext {

    Map<String, Object> getParameters();

    <T> T getParameter(String key, Class<T> type);

}
