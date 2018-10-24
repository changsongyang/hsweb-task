package org.hswebframework.task.worker.executor.supports;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface TypeConverter {
    <T> T convert(Class<T> type, Object target);

    TypeConverter DEFAULT = Class::cast;
}
