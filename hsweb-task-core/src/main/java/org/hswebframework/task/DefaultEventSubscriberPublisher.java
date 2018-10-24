package org.hswebframework.task;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@SuppressWarnings("all")
public class DefaultEventSubscriberPublisher implements EventPublisher, EventSubscriber {

    private Map<Class, List<Consumer>> consumers = new ConcurrentHashMap<>();

    @Override
    public void publish(Object event) {
        getConsumers(event.getClass())
                .forEach(consumer -> consumer.accept(event));
    }

    protected List<Consumer> getConsumers(Class type) {
        return consumers.computeIfAbsent(type, t -> new LinkedList<>());
    }

    public <T> void subscribe(Class<T> type, Consumer<T> consumer) {
        getConsumers(type).add(consumer);
    }
}
