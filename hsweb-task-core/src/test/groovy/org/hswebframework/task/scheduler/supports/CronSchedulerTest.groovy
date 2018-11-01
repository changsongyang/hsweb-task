package org.hswebframework.task.scheduler.supports

import spock.lang.Specification

import java.util.concurrent.Executors

/**
 * @author zhouhao
 * @since 1.0.0
 */
class CronSchedulerTest extends Specification {

    def scheduler = new CronScheduler("0/2 * * * * ?", Executors.newScheduledThreadPool(4))

    def "测试cron调度"() {
        given:
        def now = System.currentTimeMillis();
        long nextTime = scheduler.getNextFireTimestamp();
        def times = scheduler.getNextExecuteTime(3);
        expect:
        nextTime - now > 0 && nextTime - now <= 2000
        times.size() == 3
    }
}
