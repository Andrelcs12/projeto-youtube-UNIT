package com.media.transcoder.jobs.dto;

import com.media.transcoder.jobs.model.Job;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record JobResponse(UUID id, String originalName, String origin, String permission, String status,
                          String error, List<String> requestedProfiles, List<OutputResponse> outputs,
                          Instant createdAt, Instant updatedAt) {
    public static JobResponse from(Job job) {
        return new JobResponse(job.getId(),job.getOriginalName(),job.getOrigin(),job.getPermission(),job.getStatus().name(),job.getError(),job.getRequestedProfiles(),
                job.getOutputs().stream().map(file->new OutputResponse(file.id(),file.profile(),file.filename(),file.sizeBytes(),file.createdAt(),"/arquivos/"+file.id())).toList(),job.getCreatedAt(),job.getUpdatedAt());
    }
}
