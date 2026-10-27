package com.media.transcoder.upload.repository;

import com.media.transcoder.shared.ApiException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class UploadStorage {
    private final Path root;
    public UploadStorage(Path root) { this.root = root.toAbsolutePath().normalize(); }
    public Path save(UUID id, String filename, byte[] bytes) {
        try {
            Path folder = root.resolve(id.toString()).normalize();
            if (!folder.startsWith(root)) throw new ApiException(400, "Caminho inválido.");
            Files.createDirectories(folder);
            String ext = extension(filename);
            Path target = folder.resolve("original" + ext);
            Files.write(target, bytes);
            return target;
        } catch (IOException e) { throw new ApiException(500, "Não foi possível armazenar o vídeo."); }
    }
    private String extension(String filename) {
        if (filename == null) return ".mp4";
        String ext = filename.contains(".") ? filename.substring(filename.lastIndexOf('.')).toLowerCase() : ".mp4";
        return ext.matches("\\.(mp4|mov|mkv|webm|avi|m4v)") ? ext : ".mp4";
    }
    public void delete(UUID id) {
        Path folder = root.resolve(id.toString()).normalize();
        if (!folder.startsWith(root)) return;
        try (var paths = Files.walk(folder)) { paths.sorted(java.util.Comparator.reverseOrder()).forEach(path -> { try { Files.deleteIfExists(path); } catch (IOException ignored) { } }); }
        catch (IOException ignored) { }
    }
}
