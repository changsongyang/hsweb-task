package org.hswebframework.task.cluster;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.task.Task;

import java.io.Serializable;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class ClusterTask implements Serializable {
    private String requestId;
    private Task   task;
}
