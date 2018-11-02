package org.hswebframework.task.spring.configuration;

import org.hswebframework.task.spring.AnnotationJobAutoRegister;
import org.hswebframework.task.worker.executor.RunnableTaskBuilder;
import org.hswebframework.task.worker.executor.supports.JavaMethodInvokeTaskProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Configuration
public class WorkerConfiguration {
    @Bean
    public AnnotationJobAutoRegister annotationJobAutoRegister() {
        return new AnnotationJobAutoRegister();
    }

    @Bean
    public JavaMethodInvokeTaskProvider javaMethodInvokeTaskProvider(ApplicationContext applicationContext) {
        JavaMethodInvokeTaskProvider provider = new JavaMethodInvokeTaskProvider(applicationContext.getClassLoader());
        provider.setInstanceGetter(type -> {
            try {
                return applicationContext.getBean(type);
            } catch (Exception e) {
                try {
                    return type.newInstance();
                } catch (Exception e1) {
                    throw new RuntimeException(e1);
                }
            }
        });
        return provider;
    }

    @Bean
    @ConditionalOnMissingBean(RunnableTaskBuilder.class)
    public SpringRunnableTaskBuilder springRunnableTaskBuilder() {
        return new SpringRunnableTaskBuilder();
    }
}
