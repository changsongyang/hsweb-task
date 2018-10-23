package org.hswebframework.task;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class TaskExecutionContext {
    private Map<String, Object> parameters;


}
