package org.hswebframework.task.cluster.scheduler;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class ReturnScheduleRequest implements Serializable {
    private String scheduleId;


    private Map<String, Object> schedulerConfiguration;

    public static ReturnScheduleRequest of(String scheduleId, Map<String, Object> schedulerConfiguration) {
        ReturnScheduleRequest returnScheduleRequest = new ReturnScheduleRequest();
        returnScheduleRequest.scheduleId = scheduleId;
        returnScheduleRequest.schedulerConfiguration = schedulerConfiguration;
        return returnScheduleRequest;
    }
}
