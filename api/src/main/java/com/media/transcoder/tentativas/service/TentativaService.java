package com.media.transcoder.tentativas.service;

import com.media.transcoder.jobs.model.Job;
import com.media.transcoder.jobs.model.JobStatus;
import com.media.transcoder.jobs.repository.JobRepository;
import com.media.transcoder.jobs.service.JobQueue;
import com.media.transcoder.tentativas.dto.RetryResponse;
import com.media.transcoder.shared.ApiException;
import java.util.UUID;

public class TentativaService {
    private final JobRepository jobs; private final JobQueue queue;
    public TentativaService(JobRepository jobs,JobQueue queue){this.jobs=jobs;this.queue=queue;}
    public Job retry(String id){
        UUID uuid;try{uuid=UUID.fromString(id);}catch(IllegalArgumentException e){throw new ApiException(400,"ID do job inválido.");}
        Job job=jobs.find(uuid).orElseThrow(()->new ApiException(404,"Job não encontrado."));
        synchronized(job){if(job.getStatus()!=JobStatus.FAILED)throw new ApiException(409,"Somente jobs com falha podem ser reenfileirados.");if(jobs.countActive()>=3)throw new ApiException(429,"Limite de 3 vídeos ativos atingido.");job.clearOutputs();job.status(JobStatus.QUEUED);jobs.save(job);try{queue.submit(job);}catch(RuntimeException e){job.fail("Fila cheia; tente novamente mais tarde.");throw new ApiException(429,"Fila cheia; tente novamente mais tarde.");}}
        return job;
    }
    public RetryResponse retryResponse(String id){return new RetryResponse("Job reenfileirado para processamento.",com.media.transcoder.jobs.dto.JobResponse.from(retry(id)));}
}
