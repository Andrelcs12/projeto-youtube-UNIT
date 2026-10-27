package com.media.transcoder.tentativas.dto;

import com.media.transcoder.jobs.dto.JobResponse;

public record RetryResponse(String message,JobResponse job) { }
