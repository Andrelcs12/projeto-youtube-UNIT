package com.media.transcoder.upload.dto;

import java.util.List;

public record UploadRequest(String originalName, String origin, String permission, List<String> profiles, byte[] content) { }
