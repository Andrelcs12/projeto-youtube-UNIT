package com.media.transcoder.upload.model;

public record UploadRequest(String originalName, String origin, String permission, byte[] content) { }
