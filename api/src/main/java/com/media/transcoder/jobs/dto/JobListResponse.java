package com.media.transcoder.jobs.dto;

import java.util.List;

public record JobListResponse(List<JobResponse> jobs,long count) { }
