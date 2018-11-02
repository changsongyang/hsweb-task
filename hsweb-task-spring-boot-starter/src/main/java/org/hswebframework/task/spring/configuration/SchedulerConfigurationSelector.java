package org.hswebframework.task.spring.configuration;

import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class SchedulerConfigurationSelector implements ImportSelector, EnvironmentAware {

    private Environment environment;

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
//        Map<String, Object> map = importingClassMetadata.getAnnotationAttributes(EnableTaskScheduler.class.getName());
        List<String> imports = new ArrayList<>();

        boolean isCluster;
        try {
            ClassUtils.forName("org.hswebframework.task.cluster.ClusterManager", this.getClass().getClassLoader());
            isCluster = true;
        } catch (ClassNotFoundException e) {
            isCluster = false;
        }

        if ("true".equals(environment.getProperty("hsweb.task.cluster.enabled", String.valueOf(isCluster)))) {
            imports.add("org.hswebframework.task.spring.configuration.ClusterManagerConfiguration");
            imports.add("org.hswebframework.task.spring.configuration.ClusterWorkerManagerConfiguration");
            imports.add("org.hswebframework.task.spring.configuration.ClusterSchedulerConfiguration");
        }

        imports.add("org.hswebframework.task.spring.configuration.RepositoryConfiguration");
        imports.add("org.hswebframework.task.spring.configuration.TaskConfiguration");
        imports.add("org.hswebframework.task.spring.configuration.SchedulerConfiguration");

        return imports.toArray(new String[0]);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
