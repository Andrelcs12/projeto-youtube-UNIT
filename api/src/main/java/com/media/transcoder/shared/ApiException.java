package com.media.transcoder.shared;

public class ApiException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final int status;
    public ApiException(int status, String message) { super(message); this.status = status; }
    public int status() { return status; }
}
