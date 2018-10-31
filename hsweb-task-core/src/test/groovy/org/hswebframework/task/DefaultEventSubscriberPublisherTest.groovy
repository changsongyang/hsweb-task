package org.hswebframework.task

import org.hswebframework.task.events.TaskCreatedEvent
import spock.lang.Specification

/**
 * @author zhouhao
 * @since 1.0.0
 */
class DefaultEventSubscriberPublisherTest extends Specification {


    EventPublisher publisher;
    def subscriber = publisher = new DefaultEventSubscriberPublisher();

    def "测试发布订阅"() {
        given:
        def event = new TaskCreatedEvent(null)
        def subEvent
        subscriber.subscribe(TaskCreatedEvent.class, { e -> subEvent = e })
        publisher.publish(event)
        expect:
        subEvent != null
    }
}
