package com.media.transcoder.shared;

import java.nio.file.Path;

public record AppConfig(int port, Path dataDir, String ffmpeg, String ffprobe,
                        long maxUploadBytes, int maxVideoSeconds, int maxQueueSize) {
    public static AppConfig fromEnvironment() {
        return new AppConfig(integer("PORT", 8080), Path.of(value("TRANSCODER_DATA_DIR", "data")),
                value("FFMPEG_BIN", "ffmpeg"), value("FFPROBE_BIN", "ffprobe"),
                longValue("MAX_UPLOAD_BYTES", 20L * 1024 * 1024), integer("MAX_VIDEO_SECONDS", 20),
                integer("MAX_QUEUE_SIZE", 10));
    }
    private static String value(String key, String fallback) { return System.getenv().getOrDefault(key, fallback); }
    private static int integer(String key, int fallback) { return Integer.parseInt(value(key, Integer.toString(fallback))); }
    private static long longValue(String key, long fallback) { return Long.parseLong(value(key, Long.toString(fallback))); }
}
