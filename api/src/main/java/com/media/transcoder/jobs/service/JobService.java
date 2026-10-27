package com.media.transcoder.jobs.service;

import com.media.transcoder.jobs.model.Job;
import com.media.transcoder.jobs.repository.JobRepository;
import com.media.transcoder.shared.ApiException;
import java.util.List;
import java.util.UUID;

public class JobService {
    private final JobRepository jobs;
    public JobService(JobRepository jobs){this.jobs=jobs;}
    public Job get(String id){UUID uuid;try{uuid=UUID.fromString(id);}catch(IllegalArgumentException e){throw new ApiException(400,"ID do job inválido.");}return jobs.find(uuid).orElseThrow(()->new ApiException(404,"Job não encontrado."));}
    public List<Job> list(){return List.copyOf(jobs.findAll());}
}
