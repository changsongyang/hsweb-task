package org.hswebframework.task.cluster.redisson

import spock.lang.Specification

import java.util.concurrent.TimeUnit

/**
 * @author zhouhao
 * @since 1.0.0
 */
class RedissonClusterManagerTest extends Specification {

    def clusterManager = new RedissonClusterManager(RedissonUtils.createRedissonClient());

    def "测试List"() {
        given:
        List<String> list = clusterManager.getList("test-list");
        list.clear()
        when:
        list != null
        then:
        list.add("1")
        expect:
        list.contains("1")
    }

    def "测试普通对象"() {
        given:
        clusterManager.setObject("test", 1)
        expect:
        clusterManager.getObject("test") == 1
    }

    def "测试Set"() {
        given:
        Set<String> set = clusterManager.getSet("test-set");
        set.clear()
        when:
        set != null
        then:
        set.add("1")
        expect:
        !set.add("1")
        set.size() == 1
        set.contains("1")
    }

    def "测试Queue"() {
        given: "测试poll"
        def queue = clusterManager.getQueue("test-queue");
        queue.close()
        new Thread({
            Thread.sleep(1000)
            queue.add("1234")
        }).start()
        def val = queue.poll(2000, TimeUnit.MILLISECONDS);
        when: "成功消费"
        val == "1234"
        then: "测试consume"
        def consumeObj
        queue.consume({ obj -> consumeObj = obj })
        new Thread({
            Thread.sleep(1000)
            queue.add("2345")
        }).start()
        Thread.sleep(2000)
        expect: "成功消费"
        consumeObj == "2345"
    }

    def "测试Topic和CountDownLatch"() {
        given:
        def topic = clusterManager.getTopic("test-topic");
        def res
        def countdownLatch = clusterManager.getCountDownLatch("test-countdown");
        countdownLatch.setCount(1)
        topic.subscribe({ str ->
            res = str
            countdownLatch.countdown()
        })
        topic.publish("1234");
        countdownLatch.await(2, TimeUnit.SECONDS);
        topic.close();
        expect:
        res == "1234"
    }
}
