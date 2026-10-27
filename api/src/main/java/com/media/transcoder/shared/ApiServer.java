package com.media.transcoder.shared;

import com.media.transcoder.arquivos.controller.ArquivoController;
import com.media.transcoder.jobs.controller.JobController;
import com.media.transcoder.jobs.controller.JobListController;
import com.media.transcoder.tentativas.controller.TentativaController;
import com.media.transcoder.upload.controller.UploadController;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class ApiServer implements AutoCloseable {
    private final HttpServer server;
    public ApiServer(AppConfig config,UploadController uploads,JobController jobs,JobListController jobList,ArquivoController files,TentativaController retries)throws IOException{
        server=HttpServer.create(new InetSocketAddress("127.0.0.1",config.port()),64);
        server.setExecutor(Executors.newFixedThreadPool(Math.max(4,Runtime.getRuntime().availableProcessors())));
        server.createContext("/videos",safe(uploads::handle));
        server.createContext("/jobs",safe(exchange->{if(exchange.getRequestURI().getPath().equals("/jobs"))jobList.handle(exchange);else{String path=exchange.getRequestURI().getPath();if(path.endsWith("/arquivos"))files.list(exchange);else if(path.endsWith("/tentativas"))retries.handle(exchange);else jobs.handle(exchange);}}));
        server.createContext("/arquivos/",safe(files::download));
        server.createContext("/health",safe(exchange->HttpSupport.sendJson(exchange,200,java.util.Map.of("status","UP"))));
    }
    private com.sun.net.httpserver.HttpHandler safe(com.sun.net.httpserver.HttpHandler handler){return exchange->{try{handler.handle(exchange);}catch(ApiException e){try{HttpSupport.error(exchange,e.status(),e.getMessage());}catch(IOException ignored){exchange.close();}}catch(Exception e){try{HttpSupport.error(exchange,500,"Erro interno da API.");}catch(IOException ignored){exchange.close();}}};}
    public void start(){server.start();System.out.println("API Transcoder disponível em http://127.0.0.1:"+server.getAddress().getPort());}
    @Override public void close(){server.stop(1);}
}
