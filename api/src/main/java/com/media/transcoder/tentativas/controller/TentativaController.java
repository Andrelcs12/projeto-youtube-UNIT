package com.media.transcoder.tentativas.controller;

import com.media.transcoder.shared.ApiException;
import com.media.transcoder.shared.HttpSupport;
import com.media.transcoder.tentativas.service.TentativaService;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

public class TentativaController {
    private final TentativaService service; public TentativaController(TentativaService service){this.service=service;}
    public void handle(HttpExchange exchange)throws IOException{
        if(!exchange.getRequestMethod().equals("POST")){exchange.getResponseHeaders().set("Allow","POST");HttpSupport.error(exchange,405,"Método não permitido.");return;}
        try{String id=HttpSupport.pathId(exchange,"/jobs/","/tentativas");HttpSupport.sendJson(exchange,202,service.retryResponse(id));}
        catch(ApiException e){HttpSupport.error(exchange,e.status(),e.getMessage());}catch(Exception e){HttpSupport.error(exchange,500,"Não foi possível reenfileirar o job.");}
    }
}
