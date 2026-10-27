# Transcoder Video

MVP acadêmico de processamento local e assíncrono de vídeos, em Java 17. A API recebe vídeos de até 20 MB e 20 segundos, mantém até 3 originais, cria versões 360p e 480p com FFmpeg e disponibiliza consulta e download HTTP local. O projeto usa armazenamento em disco e fila limitada; não requer banco de dados ou broker.

## Executar

Requisitos: JDK 17, Maven 3.9+ e FFmpeg instalado e disponível em `PATH` (`ffmpeg -version`).

```powershell
mvn clean package
mvn exec:java
```

API padrão: `http://localhost:8080`. Painel Swing opcional: `mvn exec:java -Dexec.args="--swing"`.
Configure `PORT`, `TRANSCODER_DATA_DIR`, `FFMPEG_BIN`, `MAX_UPLOAD_BYTES`, `MAX_VIDEO_SECONDS` e `MAX_QUEUE_SIZE` pelo ambiente.

## Rotas

| Método | Rota | Resultado |
|---|---|---|
| POST | `/videos` | Recebe multipart `file` e retorna HTTP 202 com o job |
| GET | `/jobs/{id}` | Consulta status e perfis gerados |
| GET | `/jobs/{id}/arquivos` | Lista arquivos disponíveis |
| GET | `/arquivos/{id}` | Baixa arquivo pelo ID retornado na listagem |
| POST | `/jobs/{id}/tentativas` | Enfileira novamente um job em falha |
| GET | `/health` | Verifica disponibilidade |

Para uma validação local rápida: `curl.exe -F "file=@video.mp4" http://localhost:8080/videos`.

## Organização por feature

`api/src/main/java/com/media/transcoder/` contém `upload`, `jobs`, `arquivos` e `tentativas`; cada feature divide `controller`, `service`, `model` e `repository`. `shared` fornece configuração, erros e utilitários. Conversão fica em `integration/ffmpeg`; persistência e fila em `files/`. `desktop` mantém o painel Swing separado.

## Limites do MVP

Origem e permissão são registradas no cadastro do upload e devem ser informadas pelo operador; a aplicação não verifica direitos autorais. O tempo é validado via `ffprobe`; codecs incompatíveis ou binários ausentes geram falha de processamento. Os metadados e a fila ficam em memória e os arquivos no diretório local: reiniciar perde o estado dos jobs, embora os arquivos permaneçam. Use somente vídeos próprios ou licenciados.
