package org.hswebframework.task.cluster.scheduler;

import lombok.*;

import java.io.Serializable;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskSchedulerInfo implements Serializable {

    private String id;

    private long uptime;

    private long heartbeatTime;

    private long scheduled;

    private long running;

}
