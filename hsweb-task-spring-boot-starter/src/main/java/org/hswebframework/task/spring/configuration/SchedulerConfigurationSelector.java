package org.hswebframework.task.spring.configuration;

import org.hswebframework.task.spring.annotation.EnableTaskScheduler;
import org.hswebframework.task.spring.annotation.EnableTaskWorker;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class SchedulerConfigurationSelector implements ImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        Map<String, Object> map = importingClassMetadata.getAnnotationAttributes(EnableTaskScheduler.class.getName());
        List<String> imports = new ArrayList<>();

        boolean isCluster;
        try {
            ClassUtils.forName("org.hswebframework.task.cluster.ClusterManager", this.getClass().getClassLoader());
            isCluster = true;
        } catch (ClassNotFoundException e) {
            isCluster = false;
        }
        if (isCluster) {
            imports.add("org.hswebframework.task.spring.configuration.ClusterManagerConfiguration");
            imports.add("org.hswebframework.task.spring.configuration.ClusterWorkerManagerConfiguration");
        }

        imports.add("org.hswebframework.task.spring.configuration.TaskConfiguration");
        imports.add("org.hswebframework.task.spring.configuration.SchedulerConfiguration");

        return imports.toArray(new String[0]);
    }
}
