package com.media.transcoder.upload.service;

import com.media.transcoder.jobs.model.Job;
import com.media.transcoder.jobs.repository.JobRepository;
import com.media.transcoder.jobs.service.JobQueue;
import com.media.transcoder.shared.ApiException;
import com.media.transcoder.shared.AppConfig;
import com.media.transcoder.upload.model.UploadReceipt;
import com.media.transcoder.upload.model.UploadRequest;
import com.media.transcoder.upload.repository.UploadStorage;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

public class UploadService {
    private final AppConfig config; private final JobRepository jobs; private final JobQueue queue; private final UploadStorage storage;
    public UploadService(AppConfig config, JobRepository jobs, JobQueue queue, UploadStorage storage) { this.config=config; this.jobs=jobs; this.queue=queue; this.storage=storage; }
    public UploadReceipt upload(UploadRequest request) {
        validateFile(request);
        if (jobs.countActive() >= 3) throw new ApiException(429, "Limite de 3 vídeos ativos atingido.");
        UUID id = UUID.randomUUID();
        Path source = storage.save(id, request.originalName(), request.content());
        Job job = new Job(id, request.originalName(), request.origin().trim(), request.permission().trim(), source, Instant.now());
        jobs.save(job);
        try { queue.submit(job); }
        catch (RuntimeException error) { jobs.delete(id); storage.delete(id); throw new ApiException(429, "Fila cheia; tente novamente mais tarde."); }
        return new UploadReceipt("Vídeo recebido para processamento.", job);
    }
    private void validateFile(UploadRequest request) {
        String name = request.originalName() == null ? "" : request.originalName().toLowerCase();
        if (!(name.endsWith(".mp4") || name.endsWith(".mov") || name.endsWith(".mkv") || name.endsWith(".webm") || name.endsWith(".avi") || name.endsWith(".m4v"))) throw new ApiException(415, "Formato não suportado. Use MP4, MOV, MKV, WEBM, AVI ou M4V.");
        if (request.content().length > config.maxUploadBytes()) throw new ApiException(413, "Upload excede o limite configurado.");
        if (!signatureLooksLikeVideo(request.content(), name)) throw new ApiException(415, "O conteúdo enviado não corresponde a um vídeo válido.");
    }
    private boolean signatureLooksLikeVideo(byte[] b, String name) {
        if (b.length < 12) return false;
        if (name.endsWith(".mp4") || name.endsWith(".m4v") || name.endsWith(".mov")) return ascii(b, 4, 8).equals("ftyp");
        if (name.endsWith(".mkv")) return (b[0]&255)==0x1a && (b[1]&255)==0x45 && (b[2]&255)==0xdf && (b[3]&255)==0xa3;
        if (name.endsWith(".webm")) return (b[0]&255)==0x1a && (b[1]&255)==0x45 && (b[2]&255)==0xdf && (b[3]&255)==0xa3;
        if (name.endsWith(".avi")) return ascii(b,0,4).equals("RIFF") && ascii(b,8,4).equals("AVI ");
        return false;
    }
    private String ascii(byte[] b, int offset, int length) { return new String(b, offset, length, java.nio.charset.StandardCharsets.US_ASCII); }
}
