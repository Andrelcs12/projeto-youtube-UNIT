package com.media.transcoder.jobs.repository;

import com.media.transcoder.jobs.model.Job;
import com.media.transcoder.jobs.model.JobStatus;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class JobRepository {
    private final ConcurrentHashMap<UUID,Job> jobs = new ConcurrentHashMap<>();
    public void save(Job job){jobs.put(job.getId(),job);} public Optional<Job> find(UUID id){return Optional.ofNullable(jobs.get(id));}
    public void delete(UUID id){jobs.remove(id);} public Collection<Job> findAll(){return jobs.values().stream().sorted(Comparator.comparing(Job::getCreatedAt).reversed()).toList();}
    public long countActive(){return jobs.values().stream().filter(j->j.getStatus()==JobStatus.QUEUED||j.getStatus()==JobStatus.PROCESSING).count();}
    public List<Job> findFailed(){return jobs.values().stream().filter(j->j.getStatus()==JobStatus.FAILED).toList();}
}
