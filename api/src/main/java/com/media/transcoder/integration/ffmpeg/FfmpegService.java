package com.media.transcoder.integration.ffmpeg;

import com.media.transcoder.jobs.model.Job;
import com.media.transcoder.jobs.model.OutputFile;
import com.media.transcoder.shared.ApiException;
import com.media.transcoder.shared.AppConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class FfmpegService {
    private final AppConfig config;
    public FfmpegService(AppConfig config){this.config=config;}
    public void convert(Job job) throws Exception {
        validateDuration(job.getSource());
        Path outputDir=job.getSource().getParent();
        try {
            convertProfile(job,outputDir,"360p",360);
            convertProfile(job,outputDir,"480p",480);
        } catch (Exception error) {
            for (OutputFile output : job.getOutputs()) Files.deleteIfExists(output.path());
            job.clearOutputs();
            throw error;
        }
    }
    private void validateDuration(Path source)throws Exception{
        Process process=start(config.ffprobe(),"-v","error","-show_entries","format=duration","-of","default=noprint_wrappers=1:nokey=1",source.toString());
        String output=new String(process.getInputStream().readAllBytes(),java.nio.charset.StandardCharsets.UTF_8).trim();
        if(!process.waitFor(15,TimeUnit.SECONDS)){process.destroyForcibly();throw new ApiException(422,"Validação de duração expirou.");}
        if(process.exitValue()!=0)throw new ApiException(422,"Não foi possível validar o vídeo. Confirme se ffprobe está instalado e se o arquivo é válido.");
        try{double seconds=Double.parseDouble(output);if(!Double.isFinite(seconds)||seconds<=0)throw new NumberFormatException();if(seconds>config.maxVideoSeconds())throw new ApiException(422,"Vídeo excede a duração máxima de 20 segundos.");}
        catch(NumberFormatException e){throw new ApiException(422,"Duração do vídeo inválida.");}
    }
    private void convertProfile(Job job,Path folder,String profile,int height)throws Exception{
        Path output=folder.resolve(profile+".mp4");
        Process process=start(config.ffmpeg(),"-nostdin","-hide_banner","-loglevel","error","-y","-i",job.getSource().toString(),"-vf","scale=-2:"+height,"-c:v","libx264","-preset","veryfast","-crf","24","-c:a","aac","-movflags","+faststart","-t",Integer.toString(config.maxVideoSeconds()),output.toString());
        boolean finished=process.waitFor(2,TimeUnit.MINUTES);
        if(!finished){process.destroyForcibly();throw new ApiException(422,"Tempo limite da conversão excedido.");}
        if(process.exitValue()!=0||!Files.exists(output)||Files.size(output)==0)throw new ApiException(422,"FFmpeg falhou ao gerar o perfil "+profile+".");
        job.addOutput(new OutputFile(UUID.randomUUID(),job.getId(),profile,output.getFileName().toString(),output,Files.size(output),Instant.now()));
    }
    private Process start(String executable,String...args)throws IOException{
        var command=new java.util.ArrayList<String>();command.add(executable);command.addAll(java.util.List.of(args));
        return new ProcessBuilder(command).redirectError(ProcessBuilder.Redirect.DISCARD).start();
    }
}
