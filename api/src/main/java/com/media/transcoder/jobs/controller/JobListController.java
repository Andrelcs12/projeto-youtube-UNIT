package com.media.transcoder.jobs.controller;

import com.media.transcoder.jobs.service.JobService;
import com.media.transcoder.shared.HttpSupport;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

public class JobListController {
    private final JobService service; public JobListController(JobService service){this.service=service;}
    public void handle(HttpExchange exchange)throws IOException{if(!exchange.getRequestMethod().equals("GET")){exchange.getResponseHeaders().set("Allow","GET");HttpSupport.error(exchange,405,"Método não permitido.");return;}HttpSupport.sendJson(exchange,200,service.list());}
}
