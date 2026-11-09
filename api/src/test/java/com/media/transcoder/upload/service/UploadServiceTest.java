package com.media.transcoder.upload.service;

import com.media.transcoder.integration.ffmpeg.FfmpegService;
import com.media.transcoder.jobs.repository.JobRepository;
import com.media.transcoder.jobs.service.JobQueue;
import com.media.transcoder.shared.ApiException;
import com.media.transcoder.shared.AppConfig;
import com.media.transcoder.upload.repository.UploadStorage;
import com.media.transcoder.upload.dto.UploadRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UploadServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void recognizesMp4FtypSignatureBeforeValidatingProfiles() {
        AppConfig config = new AppConfig(8080, tempDir, "ffmpeg", "ffprobe", 1024, 20, 2, 2160);
        JobRepository jobs = new JobRepository();
        try (JobQueue queue = new JobQueue(2, 1, new FfmpegService(config), jobs)) {
            UploadService service = new UploadService(config, jobs, queue, new UploadStorage(tempDir));
            UploadRequest request = new UploadRequest(
                    "sample.mp4", "local", "authorized", List.of(),
                    new byte[]{0, 0, 0, 16, 'f', 't', 'y', 'p', 'i', 's', 'o', 'm'});

            ApiException error = assertThrows(ApiException.class, () -> service.upload(request));

            assertEquals(400, error.status());
            assertEquals(0, jobs.findAll().size());
        }
    }

    @Test
    void rejectsMp4WithoutFtypSignature() {
        AppConfig config = new AppConfig(8080, tempDir, "ffmpeg", "ffprobe", 1024, 20, 2, 2160);
        JobRepository jobs = new JobRepository();
        try (JobQueue queue = new JobQueue(2, 1, new FfmpegService(config), jobs)) {
            UploadService service = new UploadService(config, jobs, queue, new UploadStorage(tempDir));
            UploadRequest request = new UploadRequest(
                    "sample.mp4", "local", "authorized", List.of("360p"),
                    new byte[]{0, 0, 0, 16, 'n', 'o', 'p', 'e'});

            ApiException error = assertThrows(ApiException.class, () -> service.upload(request));

            assertEquals(415, error.status());
            assertEquals(0, jobs.findAll().size());
        }
    }
}
