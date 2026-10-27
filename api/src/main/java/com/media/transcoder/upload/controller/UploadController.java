package com.media.transcoder.upload.controller;

import com.media.transcoder.shared.ApiException;
import com.media.transcoder.shared.AppConfig;
import com.media.transcoder.shared.HttpSupport;
import com.media.transcoder.upload.service.MultipartUploadParser;
import com.media.transcoder.upload.service.UploadService;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

public class UploadController {
    private final MultipartUploadParser parser; private final UploadService service; private final AppConfig config;
    public UploadController(MultipartUploadParser parser, UploadService service, AppConfig config) { this.parser=parser; this.service=service; this.config=config; }
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("POST")) { exchange.getResponseHeaders().set("Allow", "POST"); HttpSupport.error(exchange,405,"Método não permitido."); return; }
        try { var request=parser.parse(exchange,config.maxUploadBytes()); HttpSupport.sendJson(exchange,202,service.upload(request)); }
        catch(ApiException e) { HttpSupport.error(exchange,e.status(),e.getMessage()); }
        catch(Exception e) { HttpSupport.error(exchange,500,"Erro ao receber upload."); }
    }
}
