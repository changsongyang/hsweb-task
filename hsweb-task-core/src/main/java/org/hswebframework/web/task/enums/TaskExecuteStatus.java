package org.hswebframework.web.task.enums;

public enum TaskExecuteStatus {
    //执行成功
    success,
    //执行失败
    failed,
    //执行中
    running,
    //挂起
    suspend,
    //阻断
    interrupt,
    //超时
    timeout,
    //准备中
    preparing;
}
