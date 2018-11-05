package org.hswebframework.task.job;

import java.util.List;

public interface JobRepository {

    List<JobDetail> findAll();

    JobDetail findById(String id);

    JobDetail save(JobDetail detail);

    JobDetail delete(String id);

}
