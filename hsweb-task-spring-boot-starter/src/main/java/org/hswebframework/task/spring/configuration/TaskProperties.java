package org.hswebframework.task.spring.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "hsweb.task")
@Getter
@Setter
public class TaskProperties {

    private SchedulerProperties scheduler = new SchedulerProperties();

    private WorkerProperties worker = new WorkerProperties();


    @Getter
    @Setter
    public static class SchedulerProperties {
        private String id;

        public SchedulerProperties validate() {
            Assert.hasText(id, "please set property: hsweb.task.scheduler.id ");
            return this;
        }
    }

    @Getter
    @Setter
    public static class WorkerProperties {
        private String id;

        private String[] groups;

        private String name;

        private String host;

        public WorkerProperties validate() {
            Assert.hasText(id, "please set property: hsweb.task.worker.id ");
            return this;
        }
    }

}
