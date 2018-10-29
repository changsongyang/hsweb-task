package org.hswebframework.task.cluster;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface ClusterManager {

    <T> T getObject(String name);

    <T> T setObject(String name, T value);

    <T> Map<String, T> getMap(String name);

    <T> List<T> getList(String name);

    <T> Set<T> getSet(String name);

    <T> Queue<T> getQueue(String name);

    <T> Topic<T> getTopic(String name);

    ClusterCountDownLatch getCountDownLatch(String name);

}
