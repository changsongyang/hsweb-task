package org.hswebframework.task.batch;


import java.util.function.Consumer;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface BatchTask<O> {

    void start();

    /**
     * 绑定输出结果
     * @param output
     * @return
     */
    BatchTask<O> output(Consumer<O> output);
}
