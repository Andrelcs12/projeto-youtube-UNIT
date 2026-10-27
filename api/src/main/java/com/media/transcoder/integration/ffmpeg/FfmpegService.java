package com.media.transcoder.integration.ffmpeg;

import com.media.transcoder.jobs.model.Job;
import com.media.transcoder.jobs.model.OutputFile;
import com.media.transcoder.shared.ApiException;
import com.media.transcoder.shared.AppConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class FfmpegService {
    private final AppConfig config;
    public FfmpegService(AppConfig config){this.config=config;}
    public void convert(Job job) throws Exception {
        MediaInfo media = probe(job.getSource());
        if (media.height() > config.maxInputHeight()) throw new ApiException(422,"Resolução de origem excede o limite de "+config.maxInputHeight()+"p.");
        Path outputDir=job.getSource().getParent();
        try {
            for(String profile:job.getRequestedProfiles()) {
                int height=Integer.parseInt(profile.substring(0,profile.length()-1));
                if(height>media.height()) throw new ApiException(422,"O perfil "+profile+" é maior que a resolução do vídeo de origem.");
                convertProfile(job,outputDir,profile,height);
            }
        } catch (Exception error) {
            for (OutputFile output : job.getOutputs()) Files.deleteIfExists(output.path());
            job.clearOutputs();
            throw error;
        }
    }
    private MediaInfo probe(Path source)throws Exception{
        Process process=start(config.ffprobe(),"-v","error","-select_streams","v:0","-show_entries","format=duration:stream=height","-of","default=noprint_wrappers=1:nokey=0",source.toString());
        CompletableFuture<String> output=CompletableFuture.supplyAsync(()->{try(var input=process.getInputStream()){return new String(input.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);}catch(IOException e){throw new java.util.concurrent.CompletionException(e);}});
        if(!process.waitFor(15,TimeUnit.SECONDS)){process.destroyForcibly();throw new ApiException(422,"Validação do vídeo excedeu o tempo limite.");}
        String result;try{result=output.get(2,TimeUnit.SECONDS);}catch(Exception e){throw new ApiException(422,"Não foi possível ler os metadados do vídeo.");}
        if(process.exitValue()!=0)throw new ApiException(422,"Vídeo inválido ou ffprobe indisponível.");
        double duration=-1;int height=-1;
        for(String line:result.split("\\R")){String[] pair=line.split("=",2);if(pair.length!=2)continue;if(pair[0].equals("duration"))try{duration=Double.parseDouble(pair[1]);}catch(NumberFormatException ignored){}if(pair[0].equals("height"))try{height=Integer.parseInt(pair[1]);}catch(NumberFormatException ignored){}}
        if(!Double.isFinite(duration)||duration<=0||height<=0)throw new ApiException(422,"Não foi possível identificar duração e resolução do vídeo.");
        if(duration>config.maxVideoSeconds())throw new ApiException(422,"Vídeo excede a duração máxima configurada de "+config.maxVideoSeconds()+" segundos.");
        return new MediaInfo(duration,height);
    }
    private void convertProfile(Job job,Path folder,String profile,int height)throws Exception{
        Path output=folder.resolve(profile+".mp4");
        Process process=start(config.ffmpeg(),"-nostdin","-hide_banner","-loglevel","error","-progress","pipe:1","-y","-i",job.getSource().toString(),"-vf","scale=-2:"+height,"-c:v","libx264","-preset","veryfast","-crf","24","-c:a","aac","-movflags","+faststart","-t",Integer.toString(config.maxVideoSeconds()),output.toString());
        CompletableFuture<Void> outputLog=CompletableFuture.runAsync(()->{try(var input=process.getInputStream()){input.transferTo(java.io.OutputStream.nullOutputStream());}catch(IOException ignored){}});
        boolean finished=process.waitFor(2,TimeUnit.MINUTES);
        if(!finished){process.destroyForcibly();Files.deleteIfExists(output);throw new ApiException(422,"Tempo limite da conversão excedido.");}
        outputLog.get(2,TimeUnit.SECONDS);
        if(process.exitValue()!=0||!Files.exists(output)||Files.size(output)==0)throw new ApiException(422,"FFmpeg falhou ao gerar o perfil "+profile+".");
        job.addOutput(new OutputFile(UUID.randomUUID(),job.getId(),profile,output.getFileName().toString(),output,Files.size(output),Instant.now()));
    }
    private record MediaInfo(double duration,int height){}
    private Process start(String executable,String...args)throws IOException{
        var command=new java.util.ArrayList<String>();command.add(executable);command.addAll(java.util.List.of(args));
        return new ProcessBuilder(command).redirectError(ProcessBuilder.Redirect.DISCARD).start();
    }
}
