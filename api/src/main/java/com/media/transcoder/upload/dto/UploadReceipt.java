package com.media.transcoder.upload.dto;

import com.media.transcoder.jobs.dto.JobResponse;

public record UploadReceipt(String message, JobResponse job) { }
