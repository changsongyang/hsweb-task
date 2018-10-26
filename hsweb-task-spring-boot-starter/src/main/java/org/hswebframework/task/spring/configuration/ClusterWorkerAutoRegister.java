package org.hswebframework.task.spring.configuration;

import org.hswebframework.task.cluster.ClusterManager;
import org.hswebframework.task.cluster.worker.ClusterNodeTaskWorker;
import org.hswebframework.task.worker.DefaultTaskWorker;
import org.hswebframework.task.worker.TaskWorkerManager;
import org.hswebframework.task.worker.executor.TaskExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import java.util.UUID;

/**
 * 集群worker自动注册
 *
 * @author zhouhao
 * @since 1.0.0
 */
public class ClusterWorkerAutoRegister implements CommandLineRunner {

    @Autowired
    private TaskProperties taskProperties;

    @Autowired
    private TaskWorkerManager taskWorkerManager;

    @Autowired
    private TaskExecutor taskExecutor;

    @Autowired
    private ClusterManager clusterManager;

    @Override
    public void run(String... args) {
        TaskProperties.WorkerProperties workerProperties = taskProperties.getWorker();
        workerProperties.validate();

        ClusterNodeTaskWorker worker = new ClusterNodeTaskWorker(workerProperties.getId(), clusterManager, taskExecutor);
        worker.setId(workerProperties.getId());
        worker.setGroups(workerProperties.getGroups());
        worker.setHost(workerProperties.getHost());
        worker.setRegisterId(UUID.randomUUID().toString());
        worker.setName(workerProperties.getName());
        taskWorkerManager.register(worker);
    }
}
