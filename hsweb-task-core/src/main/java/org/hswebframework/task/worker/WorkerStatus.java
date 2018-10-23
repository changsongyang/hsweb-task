package org.hswebframework.task.worker;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public enum WorkerStatus {
    /**
     * 空闲
     */
    idle,
    /**
     * 忙碌
     */
    busy,
    /**
     * 已暂停
     */
    pause,
    /**
     * 已注销
     */
    unregister
}
