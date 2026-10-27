package com.media.transcoder.shared;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class HttpSupport {
    private static final ObjectMapper JSON = Json.MAPPER;
    private HttpSupport() { }
    public static void sendJson(HttpExchange exchange, int status, Object value) throws IOException {
        byte[] body = JSON.writeValueAsBytes(value);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.sendResponseHeaders(status, body.length);
        try (var out = exchange.getResponseBody()) { out.write(body); }
    }
    public static void error(HttpExchange exchange, int status, String message) throws IOException {
        sendJson(exchange, status, Map.of("erro", message));
    }
    public static String pathId(HttpExchange exchange, String prefix, String suffix) {
        String path = exchange.getRequestURI().getPath();
        if (!path.startsWith(prefix) || !path.endsWith(suffix)) throw new ApiException(400, "Rota inválida.");
        String id = path.substring(prefix.length(), path.length() - suffix.length());
        if (id.isBlank() || id.contains("/")) throw new ApiException(400, "Identificador inválido.");
        return id;
    }
    public static String query(HttpExchange exchange, String key) {
        String query = exchange.getRequestURI().getRawQuery();
        if (query == null) return "";
        for (String part : query.split("&")) {
            String[] pair = part.split("=", 2);
            if (pair.length == 2 && pair[0].equals(key)) return java.net.URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
        }
        return "";
    }
}
