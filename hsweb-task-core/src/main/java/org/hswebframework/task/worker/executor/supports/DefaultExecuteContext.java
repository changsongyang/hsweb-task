package org.hswebframework.task.worker.executor.supports;

import org.hswebframework.task.worker.executor.ExecuteContext;

import java.util.Map;
import java.util.Objects;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class DefaultExecuteContext implements ExecuteContext {

    private TypeConverter converter = TypeConverter.DEFAULT;

    private Map<String, Object> parameters;

    public DefaultExecuteContext(Map<String, Object> parameters) {
        Objects.requireNonNull(parameters);
        this.parameters = parameters;
    }

    public void setConverter(TypeConverter converter) {
        this.converter = converter;
    }

    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public <T> T getParameter(String key, Class<T> type) {
        return converter.convert(type, parameters.get(key));
    }
}
