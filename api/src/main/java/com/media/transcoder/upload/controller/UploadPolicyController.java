package com.media.transcoder.upload.controller;

import com.media.transcoder.shared.HttpSupport;
import com.media.transcoder.upload.service.UploadService;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

public class UploadPolicyController {
    private final UploadService service;
    public UploadPolicyController(UploadService service){this.service=service;}
    public void handle(HttpExchange exchange)throws IOException{if(!exchange.getRequestMethod().equals("GET")){exchange.getResponseHeaders().set("Allow","GET");HttpSupport.error(exchange,405,"Método não permitido.");return;}HttpSupport.sendJson(exchange,200,service.policy());}
}
