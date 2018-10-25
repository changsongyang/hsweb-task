package org.hswebframework.task.spring;

import org.hswebframework.task.scheduler.DefaultSchedulerFactory;
import org.hswebframework.task.scheduler.SchedulerFactoryProvider;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class SchedulerFactoryBean extends DefaultSchedulerFactory implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof SchedulerFactoryProvider) {
            register(((SchedulerFactoryProvider) bean));
        }
        return bean;
    }
}
