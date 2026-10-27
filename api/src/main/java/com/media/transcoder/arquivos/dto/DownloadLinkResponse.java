package com.media.transcoder.arquivos.dto;

import java.util.UUID;

public record DownloadLinkResponse(UUID id,String filename,String downloadUrl) { }
