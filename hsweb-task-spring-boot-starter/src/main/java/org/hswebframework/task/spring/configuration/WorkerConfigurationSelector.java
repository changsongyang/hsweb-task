package org.hswebframework.task.spring.configuration;

import org.hswebframework.task.spring.annotation.EnableTaskWorker;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class WorkerConfigurationSelector implements ImportSelector, EnvironmentAware {

    private Environment environment;

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        List<String> imports = new ArrayList<>();
        boolean isCluster;
        try {
            ClassUtils.forName("org.hswebframework.task.cluster.ClusterManager", this.getClass().getClassLoader());
            isCluster = true;
        } catch (ClassNotFoundException e) {
            isCluster = false;
        }
        imports.add("org.hswebframework.task.spring.configuration.TaskConfiguration");
        imports.add("org.hswebframework.task.spring.configuration.WorkerConfiguration");
        if (!"true".equals(environment.getProperty("hsweb.task.cluster.enabled", String.valueOf(isCluster)))) {
            imports.add("org.hswebframework.task.spring.configuration.LocalWorkerConfiguration");
        } else {
            imports.add("org.hswebframework.task.spring.configuration.ClusterManagerConfiguration");
            imports.add("org.hswebframework.task.spring.configuration.ClusterWorkerConfiguration");
            imports.add("org.hswebframework.task.spring.configuration.ClusterWorkerManagerConfiguration");
        }

        return imports.toArray(new String[0]);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
