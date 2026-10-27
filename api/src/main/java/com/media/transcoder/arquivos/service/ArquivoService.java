package com.media.transcoder.arquivos.service;

import com.media.transcoder.jobs.model.Job;
import com.media.transcoder.jobs.model.OutputFile;
import com.media.transcoder.jobs.repository.JobRepository;
import com.media.transcoder.shared.ApiException;
import java.util.List;
import java.util.UUID;

public class ArquivoService {
    private final JobRepository jobs;
    public ArquivoService(JobRepository jobs){this.jobs=jobs;}
    public List<OutputFile> list(String jobId){Job job=findJob(jobId);return job.getOutputs();}
    public OutputFile get(String fileId){UUID id;try{id=UUID.fromString(fileId);}catch(IllegalArgumentException e){throw new ApiException(400,"ID do arquivo inválido.");}return jobs.findAll().stream().flatMap(j->j.getOutputs().stream()).filter(f->f.id().equals(id)).findFirst().orElseThrow(()->new ApiException(404,"Arquivo não encontrado."));}
    private Job findJob(String id){UUID uuid;try{uuid=UUID.fromString(id);}catch(IllegalArgumentException e){throw new ApiException(400,"ID do job inválido.");}return jobs.find(uuid).orElseThrow(()->new ApiException(404,"Job não encontrado."));}
}
