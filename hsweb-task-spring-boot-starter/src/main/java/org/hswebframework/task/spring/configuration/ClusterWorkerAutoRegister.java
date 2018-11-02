package org.hswebframework.task.spring.configuration;

import org.hswebframework.task.TimeoutOperations;
import org.hswebframework.task.cluster.ClusterManager;
import org.hswebframework.task.cluster.worker.ClusterNodeTaskWorker;
import org.hswebframework.task.utils.IdUtils;
import org.hswebframework.task.worker.DefaultTaskWorker;
import org.hswebframework.task.worker.TaskWorkerManager;
import org.hswebframework.task.worker.executor.TaskExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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

    @Autowired
    private TimeoutOperations timeoutOperations;

    @Override
    public void run(String... args) {
        TaskProperties.WorkerProperties workerProperties = taskProperties.getWorker();
        workerProperties.validate();

        ClusterNodeTaskWorker worker = new ClusterNodeTaskWorker(workerProperties.getId(), timeoutOperations, clusterManager, taskExecutor);
        worker.setId(workerProperties.getId());
        String[] newGroup = Arrays.copyOf(workerProperties.getGroups(), workerProperties.getGroups().length + 1);
        newGroup[newGroup.length - 1] = worker.getId();

        worker.setGroups(newGroup);
        worker.setHost(workerProperties.getHost());
        worker.setRegisterId(IdUtils.newUUID());
        worker.setName(workerProperties.getName());
        taskWorkerManager.register(worker);
    }
}
