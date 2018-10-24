package org.hswebframework.task;

import java.util.function.Consumer;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface EventSubscriber {
    <T> void subscribe(Class<T> type, Consumer<T> listener);
}
