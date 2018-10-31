package org.hswebframework.task.cluster.worker.history;

import java.util.List;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface ExecuteHistoryRepository {
    void saveExecuteBefore(ExecuteBefore before);

    void saveExecuteAfter(ExecuteAfter after);

    List<ExecuteBefore> queryRunningBySchedulerId(String schedulerId);
}
