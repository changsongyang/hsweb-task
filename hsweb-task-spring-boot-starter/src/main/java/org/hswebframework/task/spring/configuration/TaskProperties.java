package org.hswebframework.task.spring.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "hsweb.task")
@Getter
@Setter
public class TaskProperties {

    @Value("${spring.application.name}-#{T(java.net.InetAddress).localHost.hostName}")
    private String defaultId;

    @Value("#{T(java.net.InetAddress).localHost.hostName}")
    private String hostName;

    private SchedulerProperties scheduler = new SchedulerProperties();

    private WorkerProperties worker = new WorkerProperties();

    @Getter
    @Setter
    public class SchedulerProperties {
        private String id;

        public SchedulerProperties validate() {
            if (StringUtils.isEmpty(id)) {
                id = defaultId;
            }
            Assert.hasText(id, "please set property: hsweb.task.scheduler.id ");
            return this;
        }
    }

    @Getter
    @Setter
    public class WorkerProperties {

        private String id;

        private String[] groups;

        private String clientGroup;

        private String name;

        private String host;

        public WorkerProperties validate() {
            if (StringUtils.isEmpty(id)) {
                id = defaultId;
            }
            Assert.hasText(id, "please set property: hsweb.task.worker.id ");
            if (StringUtils.isEmpty(clientGroup) && groups.length == 1) {
                clientGroup = groups[0];
            }
            Assert.hasText(clientGroup, "please set property: hsweb.task.worker.client-group ");
            if (StringUtils.isEmpty(host)) {
                host = hostName;
            }
            return this;
        }
    }

}
