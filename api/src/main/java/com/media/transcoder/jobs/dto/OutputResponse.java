package com.media.transcoder.jobs.dto;

import java.time.Instant;
import java.util.UUID;

public record OutputResponse(UUID id, String profile, String filename, long sizeBytes, Instant createdAt, String downloadUrl) { }
