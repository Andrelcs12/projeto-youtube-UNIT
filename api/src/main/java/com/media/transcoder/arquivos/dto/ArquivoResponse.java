package com.media.transcoder.arquivos.dto;

import com.media.transcoder.jobs.model.OutputFile;
import java.time.Instant;
import java.util.UUID;

public record ArquivoResponse(UUID id,String profile,String filename,long sizeBytes,Instant createdAt,String downloadUrl) {
    public static ArquivoResponse from(OutputFile file){return new ArquivoResponse(file.id(),file.profile(),file.filename(),file.sizeBytes(),file.createdAt(),"/arquivos/"+file.id());}
}
