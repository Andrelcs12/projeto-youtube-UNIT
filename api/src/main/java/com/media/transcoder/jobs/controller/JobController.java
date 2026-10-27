package com.media.transcoder.jobs.controller;

import com.media.transcoder.jobs.service.JobService;
import com.media.transcoder.shared.ApiException;
import com.media.transcoder.shared.HttpSupport;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

public class JobController {
    private final JobService service; public JobController(JobService service){this.service=service;}
    public void handle(HttpExchange exchange)throws IOException{
        if(!exchange.getRequestMethod().equals("GET")){exchange.getResponseHeaders().set("Allow","GET");HttpSupport.error(exchange,405,"Método não permitido.");return;}
        try {String id=HttpSupport.pathId(exchange,"/jobs/","");HttpSupport.sendJson(exchange,200,service.get(id));}
        catch(ApiException e){HttpSupport.error(exchange,e.status(),e.getMessage());}
        catch(Exception e){HttpSupport.error(exchange,500,"Não foi possível consultar o job.");}
    }
}
