package com.media.transcoder.jobs.model;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Job {
    private final UUID id; private final String originalName; private final String origin; private final String permission;
    private final Path source; private final Instant createdAt; private volatile Instant updatedAt;
    private final List<String> requestedProfiles;
    private volatile JobStatus status = JobStatus.QUEUED; private volatile String error;
    private final List<OutputFile> outputs = new ArrayList<>();
    public Job(UUID id,String originalName,String origin,String permission,Path source,Instant createdAt,List<String> requestedProfiles) {
        this.id=id;this.originalName=originalName;this.origin=origin;this.permission=permission;this.source=source;this.createdAt=createdAt;this.updatedAt=createdAt;this.requestedProfiles=List.copyOf(requestedProfiles);
    }
    public UUID getId(){return id;} public String getOriginalName(){return originalName;} public String getOrigin(){return origin;}
    public String getPermission(){return permission;} public Path getSource(){return source;} public Instant getCreatedAt(){return createdAt;}
    public List<String> getRequestedProfiles(){return requestedProfiles;}
    public Instant getUpdatedAt(){return updatedAt;} public JobStatus getStatus(){return status;} public String getError(){return error;}
    public synchronized List<OutputFile> getOutputs(){return List.copyOf(outputs);}
    public synchronized void status(JobStatus value){status=value;if(value==JobStatus.QUEUED)error=null;updatedAt=Instant.now();}
    public synchronized void fail(String message){status=JobStatus.FAILED;error=message;updatedAt=Instant.now();}
    public synchronized void addOutput(OutputFile file){outputs.add(file);updatedAt=Instant.now();}
    public synchronized void clearOutputs(){outputs.clear();}
}
