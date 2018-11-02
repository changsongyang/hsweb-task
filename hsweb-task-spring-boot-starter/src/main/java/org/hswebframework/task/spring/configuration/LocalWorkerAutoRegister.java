package org.hswebframework.task.spring.configuration;

import org.hswebframework.task.utils.IdUtils;
import org.hswebframework.task.worker.DefaultTaskWorker;
import org.hswebframework.task.worker.TaskWorkerManager;
import org.hswebframework.task.worker.executor.TaskExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import java.util.Arrays;
import java.util.UUID;

/**
 * 单点worker自动注册
 *
 * @author zhouhao
 * @since 1.0.0
 */
public class LocalWorkerAutoRegister implements CommandLineRunner {

    @Autowired
    private TaskProperties taskProperties;

    @Autowired
    private TaskWorkerManager taskWorkerManager;

    @Autowired
    private TaskExecutor taskExecutor;

    @Override
    public void run(String... args) {
        DefaultTaskWorker worker = new DefaultTaskWorker(taskExecutor);

        TaskProperties.WorkerProperties workerProperties = taskProperties.getWorker();
        workerProperties.validate();
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
