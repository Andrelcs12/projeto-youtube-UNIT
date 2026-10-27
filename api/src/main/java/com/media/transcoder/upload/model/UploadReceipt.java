package com.media.transcoder.upload.model;

import com.media.transcoder.jobs.model.Job;

public record UploadReceipt(String message, Job job) { }
