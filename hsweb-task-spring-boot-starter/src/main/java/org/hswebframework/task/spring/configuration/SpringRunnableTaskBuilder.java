package org.hswebframework.task.spring.configuration;

import org.hswebframework.task.worker.executor.supports.DefaultRunnableTask;
import org.hswebframework.task.worker.executor.supports.DefaultRunnableTaskBuilder;
import org.hswebframework.task.worker.executor.supports.RunnableTaskBuilderProvider;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class SpringRunnableTaskBuilder extends DefaultRunnableTaskBuilder implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof RunnableTaskBuilderProvider) {
            addProvider(((RunnableTaskBuilderProvider) bean));
        }
        return bean;
    }
}
