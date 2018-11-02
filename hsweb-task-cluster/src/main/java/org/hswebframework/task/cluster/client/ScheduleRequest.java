package org.hswebframework.task.cluster.client;

import lombok.*;

import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRequest implements Serializable {
    private String taskId;

    private String jobId;

    private Map<String, Object> configuration;
}