package org.hswebframework.task

import spock.lang.Specification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * @author zhouhao
 * @since 1.0.0
 */
class ThreadPoolTimeoutOperationsTest extends Specification {
    def operations = new ThreadPoolTimeoutOperations(Executors.newFixedThreadPool(4));


    def doSync(response, long timeout, long sleep, timeoutResponse) {
        return operations.doTrySync({ Thread.sleep(sleep); response }, timeout, TimeUnit.MILLISECONDS, { err, t -> timeoutResponse })
    }

    def doAsync(response, long timeout, long sleep, timeoutResponse) {
        def countDownLatch = new CountDownLatch(1);
        def resp;

        operations.doTryAsync({ Thread.sleep(sleep); response },
                timeout,
                TimeUnit.MILLISECONDS,
                { err -> timeoutResponse },
                { res, isTimeout -> println res;resp = res; countDownLatch.countDown(); })
        countDownLatch.await();
        return resp
    }

    def "测试同步调用"() {

        expect:
        doSync(data, timeout, sleep, whenError) == response
        where:
        data          | timeout | sleep | whenError | response
        "testSuccess" | 100     | 0     | "error"   | "testSuccess"
        "testTimeout" | 100     | 101   | "error"   | "error"

    }

    def "测试异步调用"() {

        expect:
        doAsync(data, timeout, sleep, whenError) == response
        where:
        data          | timeout | sleep | whenError | response
        "testSuccess" | 100     | 0     | "error"   | "testSuccess"
        "testTimeout" | 100     | 101   | "error"   | "error"

    }


}
