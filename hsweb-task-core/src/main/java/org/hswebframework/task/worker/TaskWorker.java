package org.hswebframework.task.worker;

import org.hswebframework.task.TaskExecutor;
import org.hswebframework.task.worker.event.WorkerStatusChangedEvent;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface TaskWorker {
    /**
     * worker id,全局唯一
     *
     * @return 全局唯一的id
     */
    String getId();

    /**
     * 注册ID,每次注册的ID不同,用于区分同一个worker在不同时间注册的信息
     *
     * @return 注册ID
     */
    String getRegisterId();

    /**
     * @return worker name
     */
    String getName();

    /**
     * 分组,同一个任务,在同一个分组上的同一时间只会执行一次.
     *
     * @return 分组, 可能为<code>null</code>
     */
    String[] getGroups();

    /**
     * 当前worker的主机名,在集群的时候用于区分是哪一个节点
     *
     * @return 主机名称
     */
    String getHost();

    /**
     * worker启动的时间
     *
     * @return 时间戳毫秒
     * @see System#currentTimeMillis()
     */
    long getRegisterTime();

    /**
     * 注销时间,worker被标记为已注销时,该方法生效. 否则返回-1.
     *
     * @return 时间戳毫秒
     * @see System#currentTimeMillis()
     * @see WorkerStatus#unregister
     * @see TaskWorkerManager#unregister(String, boolean)
     */
    long getUnRegisterTime();

    /**
     * 当前worker可同时执行的最大任务数量
     *
     * @return 最大任务数量,-1为不限制数量
     */
    int getMaximum();

    /**
     * 当前work的健康度,满分100.
     *
     * @return 值越低健康度越低, 此worker执行任务的概率越低
     */
    byte getHealth();

    /**
     * worker的当前状态
     *
     * @return 状态枚举
     * @see WorkerStatus
     * @see WorkerStatusChangedEvent
     */
    WorkerStatus getStatus();

    /**
     * 此worker的任务执行器,用于执行任务
     *
     * @return 任务执行器
     * @see TaskExecutor
     */
    TaskExecutor getExecutor();
}
