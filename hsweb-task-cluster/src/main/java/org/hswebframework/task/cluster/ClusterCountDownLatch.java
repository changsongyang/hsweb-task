package org.hswebframework.task.cluster;

import java.util.concurrent.TimeUnit;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface ClusterCountDownLatch {

    ClusterCountDownLatch setCount(long count);

    long getCount();

    void countdown();

    void await(long time, TimeUnit timeUnit);
}
