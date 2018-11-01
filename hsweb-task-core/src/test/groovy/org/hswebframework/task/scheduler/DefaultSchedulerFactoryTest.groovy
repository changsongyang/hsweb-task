package org.hswebframework.task.scheduler

import org.hswebframework.task.scheduler.supports.PeriodScheduler
import org.hswebframework.task.scheduler.supports.PeriodSchedulerProvider
import spock.lang.Specification

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * @author zhouhao
 * @since 1.0.0
 */
class DefaultSchedulerFactoryTest extends Specification {

    DefaultSchedulerFactory factory = new DefaultSchedulerFactory()

    def executor = Executors.newScheduledThreadPool(10)

    def setup() {
        factory.register(new PeriodSchedulerProvider(executor))
    }

    def "测试初始化"() {
        given:
        def config = new PeriodScheduler(executor, 100, 1000, TimeUnit.MILLISECONDS).getConfiguration()
        when:
        config != null
        then:
        def parsed = factory.create(config)
        expect:
        parsed != null
        parsed instanceof PeriodScheduler
        parsed.getConfiguration() == config
    }
}
