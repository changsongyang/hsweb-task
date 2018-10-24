package org.hswebframework.task.worker;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public enum WorkerStatus {
    /**
     * 空闲
     */
    idle((byte) 100),
    /**
     * 忙碌
     */
    busy((byte) 50),
    /**
     * 已暂停
     */
    pause((byte) 0),

    /**
     * 离线
     */
    offline((byte) 0),
    /**
     * 已关闭
     */
    shutdown((byte) 0);

    private byte healthScore;

    WorkerStatus(byte healthScore) {
        this.healthScore = healthScore;
    }

    public byte getHealthScore() {
        return healthScore;
    }
}
