package com.media.transcoder.upload.service;

import com.media.transcoder.shared.ApiException;
import com.media.transcoder.upload.model.UploadRequest;
import com.sun.net.httpserver.HttpExchange;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultipartUploadParser {
    private static final Pattern BOUNDARY = Pattern.compile("boundary=(?:\"([^\"]+)\"|([^;]+))", Pattern.CASE_INSENSITIVE);
    public UploadRequest parse(HttpExchange exchange, long maxBytes) throws IOException {
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.toLowerCase().startsWith("multipart/form-data")) throw new ApiException(415, "Envie multipart/form-data com o campo file.");
        Matcher matcher = BOUNDARY.matcher(contentType);
        if (!matcher.find()) throw new ApiException(400, "Boundary multipart ausente.");
        String boundary = matcher.group(1) != null ? matcher.group(1) : matcher.group(2).trim();
        long declared = parseLength(exchange.getRequestHeaders().getFirst("Content-Length"));
        if (declared > maxBytes + 64 * 1024) throw new ApiException(413, "Upload excede o limite de 20 MB.");
        byte[] body = readBounded(exchange.getRequestBody(), maxBytes + 64 * 1024);
        String wire = new String(body, StandardCharsets.ISO_8859_1);
        String delimiter = "--" + boundary;
        for (String part : wire.split(Pattern.quote(delimiter))) {
            int headerEnd = part.indexOf("\r\n\r\n");
            if (headerEnd < 0) continue;
            String headers = part.substring(0, headerEnd);
            Matcher disposition = Pattern.compile("filename=\"([^\"]*)\"", Pattern.CASE_INSENSITIVE).matcher(headers);
            if (headers.toLowerCase().contains("name=\"file\"") && disposition.find()) {
                String filename = disposition.group(1);
                String data = part.substring(headerEnd + 4);
                if (data.endsWith("\r\n")) data = data.substring(0, data.length() - 2);
                byte[] content = data.getBytes(StandardCharsets.ISO_8859_1);
                if (content.length == 0) throw new ApiException(400, "Arquivo vazio.");
                if (content.length > maxBytes) throw new ApiException(413, "Upload excede o limite de 20 MB.");
                String origin = field(wire, boundary, "origin");
                String permission = field(wire, boundary, "permission");
                if (origin.isBlank() || permission.isBlank()) throw new ApiException(400, "Informe a origem do vídeo e registre a permissão de uso.");
                return new UploadRequest(filename, origin, permission, content);
            }
        }
        throw new ApiException(400, "Campo de arquivo 'file' não encontrado.");
    }
    private String field(String wire, String boundary, String name) {
        for (String part : wire.split(Pattern.quote("--" + boundary))) {
            int end = part.indexOf("\r\n\r\n");
            if (end < 0 || !part.substring(0, end).toLowerCase().contains("name=\"" + name + "\"")) continue;
            String value = part.substring(end + 4);
            if (value.endsWith("\r\n")) value = value.substring(0, value.length() - 2);
            return value.trim();
        }
        return "";
    }
    private long parseLength(String header) { try { return header == null ? 0 : Long.parseLong(header); } catch (NumberFormatException ignored) { return 0; } }
    private byte[] readBounded(InputStream input, long max) throws IOException {
        try (input; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192]; int read;
            while ((read = input.read(buffer)) != -1) {
                if ((long) output.size() + read > max) throw new ApiException(413, "Upload excede o limite permitido.");
                output.write(buffer, 0, read);
            }
            return output.toByteArray();
        }
    }
}
