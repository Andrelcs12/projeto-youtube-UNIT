package com.media.transcoder.jobs.dto;

import java.time.Instant;
import java.util.UUID;

public record PostResponse(UUID id,String title,String description,String status,Instant createdAt,String jobId,String downloadUrl) { }
