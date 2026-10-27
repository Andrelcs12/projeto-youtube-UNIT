package com.media.transcoder.jobs.model;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

public record OutputFile(UUID id, UUID jobId, String profile, String filename, Path path, long sizeBytes, Instant createdAt) { }
