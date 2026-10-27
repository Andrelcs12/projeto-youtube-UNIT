package com.media.transcoder;

import com.media.transcoder.arquivos.controller.ArquivoController;
import com.media.transcoder.arquivos.service.ArquivoService;
import com.media.transcoder.desktop.Dashboard;
import com.media.transcoder.integration.ffmpeg.FfmpegService;
import com.media.transcoder.jobs.controller.JobController;
import com.media.transcoder.jobs.controller.JobListController;
import com.media.transcoder.posts.controller.PostController;
import com.media.transcoder.jobs.repository.JobRepository;
import com.media.transcoder.jobs.service.JobQueue;
import com.media.transcoder.jobs.service.JobService;
import com.media.transcoder.posts.service.PostService;
import com.media.transcoder.shared.ApiServer;
import com.media.transcoder.shared.AppConfig;
import com.media.transcoder.tentativas.controller.TentativaController;
import com.media.transcoder.tentativas.service.TentativaService;
import com.media.transcoder.upload.controller.UploadController;
import com.media.transcoder.upload.repository.UploadStorage;
import com.media.transcoder.upload.service.MultipartUploadParser;
import com.media.transcoder.upload.service.UploadService;

public class TranscoderApplication {
    public static void main(String[] args)throws Exception{
        AppConfig config=AppConfig.fromEnvironment();
        if(config.port()<1||config.port()>65535||config.maxQueueSize()<1||config.maxVideoSeconds()<1||config.maxUploadBytes()<1||config.maxInputHeight()<360||config.maxInputHeight()>4320)throw new IllegalArgumentException("Configuração inválida: confira PORT, limites de upload, fila e resolução de origem (360p a 4320p).");
        var jobs=new JobRepository();var ffmpeg=new FfmpegService(config);
        var queue=new JobQueue(config.maxQueueSize(),2,ffmpeg,jobs);
        var uploadStorage=new UploadStorage(config.dataDir().resolve("uploads"));
        var uploadService=new UploadService(config,jobs,queue,uploadStorage);
        var jobService=new JobService(jobs);var arquivoService=new ArquivoService(jobs);var tentativaService=new TentativaService(jobs,queue);
        var server=new ApiServer(config,new UploadController(new MultipartUploadParser(),uploadService,config),new com.media.transcoder.upload.controller.UploadPolicyController(uploadService),new JobController(jobService),new JobListController(jobService),new PostController(new PostService(jobService)),new ArquivoController(arquivoService),new TentativaController(tentativaService));
        Runtime.getRuntime().addShutdownHook(new Thread(()->{server.close();queue.close();}));
        server.start();
        if(java.util.Arrays.asList(args).contains("--swing"))new Dashboard("http://127.0.0.1:"+config.port()).show();
        else new java.util.concurrent.CountDownLatch(1).await();
    }
}
