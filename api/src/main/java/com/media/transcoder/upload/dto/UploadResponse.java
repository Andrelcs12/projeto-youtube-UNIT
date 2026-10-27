package com.media.transcoder.upload.dto;

import com.media.transcoder.jobs.dto.JobResponse;

public record UploadResponse(String message, JobResponse job) { }
