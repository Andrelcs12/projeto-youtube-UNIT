# Transcoder Video

MVP acadêmico de conversão local e assíncrona de vídeos, em Java 17. A interface Swing envia vídeos próprios ou licenciados à API, acompanha os jobs e lista os resultados. O worker FFmpeg recebe arquivos até 20 MB e 20 segundos, aceita origem até 4K (limite padrão 2160p) e gera os perfis escolhidos entre 360p, 480p e 720p. O projeto usa armazenamento em disco e fila limitada; não requer banco de dados ou broker.

## Executar

Requisitos: JDK 17, Maven 3.9+ e FFmpeg instalado e disponível em `PATH` (`ffmpeg -version`).

```powershell
mvn clean package
mvn exec:java
```

API padrão: `http://localhost:8080`. Painel Swing opcional: `mvn exec:java -Dexec.args="--swing"`.
Para abrir API e interface: `mvn exec:java -Dexec.args="--swing"`. Configure `PORT`, `TRANSCODER_DATA_DIR`, `FFMPEG_BIN`, `FFPROBE_BIN`, `MAX_UPLOAD_BYTES` (padrão 20 MiB), `MAX_VIDEO_SECONDS` (padrão 20 s), `MAX_QUEUE_SIZE` (padrão 10) e `MAX_INPUT_HEIGHT` (padrão 2160; máximo 4320) pelo ambiente.

## Rotas

| Método | Rota | Resultado |
|---|---|---|
| POST | `/videos` | Recebe multipart `file` e retorna HTTP 202 com o job |
| GET | `/upload-config` | Retorna os limites e formatos aceitos pela interface |
| GET | `/jobs/{id}` | Consulta status e perfis gerados |
| GET | `/jobs` | Lista jobs para o painel |
| GET | `/jobs/{id}/arquivos` | Lista arquivos disponíveis |
| GET | `/arquivos/{id}` | Baixa arquivo pelo ID retornado na listagem |
| POST | `/jobs/{id}/tentativas` | Enfileira novamente um job em falha |
| GET | `/posts` | Lista os vídeos prontos e seus links locais |
| GET | `/health` | Verifica disponibilidade |

Envio multipart exige `file`, `origin`, `permission` e `profiles` (por exemplo, `360p,480p,720p`). A resolução da fonte não é a resolução de saída: é possível enviar uma fonte 4K dentro dos limites e escolher perfil de saída inferior. Envie apenas conteúdo autorizado; o app não baixa URLs de plataformas.

## Organização por feature

`api/src/main/java/com/media/transcoder/` contém as features `upload`, `jobs`, `arquivos` e `tentativas`, separadas em `controller`, `service` e `dto`. `jobs/model` e `jobs/repository` encapsulam o domínio interno e seu armazenamento em memória. `shared` fornece configuração, erros e infraestrutura HTTP. `integration/ffmpeg` integra o worker com FFmpeg/ffprobe; `desktop` contém a interface Swing.

## Limites do MVP

Origem e permissão são registradas no cadastro do upload e confirmadas pelo operador; a aplicação não verifica direitos autorais. O worker valida duração/resolução via `ffprobe`, limita tempo da conversão e usa concorrência de dois processos. Codecs incompatíveis ou binários ausentes geram falha no job. Os metadados e a fila ficam em memória e os arquivos no diretório local; reiniciar perde o estado dos jobs. A interface constrói o multipart em memória, portanto arquivos muito próximos do limite podem causar pressão adicional de memória.
