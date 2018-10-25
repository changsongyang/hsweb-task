package org.hswebframework.task.spring;

import org.hswebframework.task.DefaultEventSubscriberPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class SpringEventPublisher extends DefaultEventSubscriberPublisher {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public void publish(Object event) {
        eventPublisher.publishEvent(event);
    }
}
