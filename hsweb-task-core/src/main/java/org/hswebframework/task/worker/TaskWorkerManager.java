package org.hswebframework.task.worker;

import java.util.List;

/**
 * job worker管理器,用于管理worker信息
 *
 * @author zhouhao
 * @since 1.0.0
 */
public interface TaskWorkerManager {

    /**
     * 获取全部worker
     *
     * @return 任务列表, 如果没有worker则返回<code>new ArrayList()</code>
     */
    List<TaskWorker> getAllWorker();

    /**
     * 指定group选取一个worker,将会返回合适的worker.
     *
     * @param group 分组,可以为null,为null时,将从所有worker中选取.
     * @return 返回合适的worker, 如果不存在返回null
     */
    TaskWorker select(String group);

    /**
     * 注册一个worker
     *
     * @param worker 要注册的worker
     * @return 最终注册的worker
     */
    TaskWorker register(TaskWorker worker);

    /**
     * 注销一个worker
     *
     * @param id    worker id
     * @param force 是否强制注销,如果为false,则会等待任务执行完成后再注销.
     *              强制注销将会中断任务执行,并且停止该worker未执行的任务.
     * @return 被注销的worker, 如果worker不存在则返回<code>null</code>
     */
    TaskWorker unregister(String id, boolean force);

}
