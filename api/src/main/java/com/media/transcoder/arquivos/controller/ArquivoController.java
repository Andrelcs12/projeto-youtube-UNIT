package com.media.transcoder.arquivos.controller;

import com.media.transcoder.arquivos.service.ArquivoService;
import com.media.transcoder.shared.ApiException;
import com.media.transcoder.shared.HttpSupport;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.nio.file.Files;

public class ArquivoController {
    private final ArquivoService service; public ArquivoController(ArquivoService service){this.service=service;}
    public void list(HttpExchange exchange)throws IOException{
        if(!exchange.getRequestMethod().equals("GET")){exchange.getResponseHeaders().set("Allow","GET");HttpSupport.error(exchange,405,"Método não permitido.");return;}
        try {String id=HttpSupport.pathId(exchange,"/jobs/","/arquivos");HttpSupport.sendJson(exchange,200,service.list(id));}
        catch(ApiException e){HttpSupport.error(exchange,e.status(),e.getMessage());}catch(Exception e){HttpSupport.error(exchange,500,"Não foi possível listar arquivos.");}
    }
    public void download(HttpExchange exchange)throws IOException{
        if(!exchange.getRequestMethod().equals("GET")){exchange.getResponseHeaders().set("Allow","GET");HttpSupport.error(exchange,405,"Método não permitido.");return;}
        try {
            var file=service.get(HttpSupport.pathId(exchange,"/arquivos/",""));
            if(!Files.isRegularFile(file.path()))throw new ApiException(404,"Arquivo indisponível.");
            String safeName=file.filename().replaceAll("[^A-Za-z0-9._-]","_");
            exchange.getResponseHeaders().set("Content-Type","video/mp4");exchange.getResponseHeaders().set("Content-Disposition","attachment; filename=\""+safeName+"\"");exchange.getResponseHeaders().set("X-Content-Type-Options","nosniff");
            exchange.sendResponseHeaders(200,Files.size(file.path()));try(var out=exchange.getResponseBody()){Files.copy(file.path(),out);}
        }catch(ApiException e){HttpSupport.error(exchange,e.status(),e.getMessage());}catch(Exception e){HttpSupport.error(exchange,500,"Não foi possível baixar o arquivo.");}
    }
}
