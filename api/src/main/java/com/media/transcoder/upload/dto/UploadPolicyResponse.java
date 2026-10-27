package com.media.transcoder.upload.dto;

import java.util.List;

public record UploadPolicyResponse(long maxUploadBytes, int maxVideoSeconds, int maxInputHeight,
                                   int maxActiveVideos, int queueCapacity, List<String> allowedExtensions,
                                   List<String> outputProfiles, boolean externalUrlImportEnabled) { }
