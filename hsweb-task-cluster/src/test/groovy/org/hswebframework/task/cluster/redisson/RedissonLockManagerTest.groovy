package org.hswebframework.task.cluster.redisson

import spock.lang.Specification

import java.util.concurrent.TimeUnit

/**
 * @author zhouhao
 * @since 1.0.0
 */
class RedissonLockManagerTest extends Specification {

    def lockManager = new RedissonLockManager(RedissonUtils.createRedissonClient())

    def "测试锁,解锁"() {
        given: "获取锁"
        def data
        new Thread({
            def lock2 = lockManager.tryGetLock("test-lock", 10000, TimeUnit.MILLISECONDS)
            data = "1234"
            new Thread({
                Thread.sleep(1000)
                lock2.release()
            }).start()
        }).start()
        Thread.sleep(200)
        def lock = lockManager.tryGetLock("test-lock", 10000, TimeUnit.MILLISECONDS)
        lock.release()
        expect:
        data == "1234"
    }
}
