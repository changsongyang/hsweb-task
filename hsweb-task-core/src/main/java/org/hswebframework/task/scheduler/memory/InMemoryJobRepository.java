package org.hswebframework.task.scheduler.memory;

import org.hswebframework.task.job.JobDetail;
import org.hswebframework.task.job.JobRepository;
import org.hswebframework.task.utils.IdUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class InMemoryJobRepository implements JobRepository {

    private Map<String, JobDetail> jobs = new ConcurrentHashMap<>();

    @Override
    public List<JobDetail> findAll() {
        return new ArrayList<>(jobs.values());
    }

    @Override
    public JobDetail findById(String id) {
        return jobs.get(id);
    }

    @Override
    public JobDetail save(JobDetail detail) {

        if (detail.getId() == null) {
            detail.setId(IdUtils.newUUID());
        }
        jobs.put(detail.getId(), detail);
        return detail;
    }

    @Override
    public JobDetail delete(String id) {
        return jobs.remove(id);
    }
}
