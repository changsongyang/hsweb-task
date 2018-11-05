package org.hswebframework.task.cluster.redisson.repository;

import org.hswebframework.task.job.JobDetail;
import org.hswebframework.task.job.JobRepository;
import org.redisson.api.RMap;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class RedissonJobRepository implements JobRepository {
    private RMap<String, JobDetail> repository;

    public RedissonJobRepository(RMap<String, JobDetail> repository) {
        this.repository = repository;
    }

    @Override
    public List<JobDetail> findAll() {
        return new ArrayList<>(repository.values());
    }

    @Override
    public JobDetail findById(String id) {
        return repository.get(id);
    }

    @Override
    public JobDetail save(JobDetail detail) {
        repository.put(detail.getId(), detail);
        return detail;
    }

    @Override
    public JobDetail delete(String id) {
        return repository.remove(id);
    }

}
