# Validacao do MVP

Data: 09/11/2026. Ambiente: Windows 11 x64 build 26200; Temurin Java/Javac 17.0.20.1; Apache Maven 3.9.12 localizado no cache local; FFmpeg e ffprobe 9.0.2 full_build, baixados pelo winget em ZIP portatil, hash do pacote verificado pelo winget. Nenhuma instalacao global foi feita. A pasta temporaria e os binarios nao fazem parte do repositorio.

## Comandos principais

```powershell
$mvn = "$env:USERPROFILE\.m2\wrapper\dists\apache-maven-3.9.12-bin\5nmfsn99br87k5d4ajlekdq10k\apache-maven-3.9.12\bin\mvn.cmd"
$env:MAVEN_OPTS = '-Xms32m -Xmx256m -XX:MaxMetaspaceSize=128m'
& $mvn clean test
& $mvn clean package

$bin = "$env:TEMP\transcoder-ffmpeg-portable\extracted\ffmpeg-9.0.2-full_build\bin"
$env:FFMPEG_BIN = "$bin\ffmpeg.exe"
$env:FFPROBE_BIN = "$bin\ffprobe.exe"
$env:TRANSCODER_DATA_DIR = "$env:TEMP\transcoder-ffmpeg-portable\app-data-final"
$env:PORT = '18082'
& $mvn exec:java
```

Video de teste gerado localmente, sem download externo:

```powershell
& $env:FFMPEG_BIN -hide_banner -loglevel error -y -f lavfi -i 'testsrc2=size=1280x720:rate=24' -t 6 -c:v libx264 -pix_fmt yuv420p "$env:TEMP\transcoder-ffmpeg-portable\transcoder-smoke.mp4"
curl.exe -F "file=@$env:TEMP\transcoder-ffmpeg-portable\transcoder-smoke.mp4;type=video/mp4" -F 'origin=sintetico local' -F 'permission=gerado localmente' -F 'profiles=360p,480p,720p' http://127.0.0.1:18082/videos
```

O Swing foi iniciado com `& $mvn exec:java '-Dexec.args=--swing'`. A janela `Transcoder Video — Conversor local` foi capturada e inspecionada. A janela abriu, mas o clique automatizado nao atingiu o seletor; o envio pela interface visual fica sem confirmacao. O mesmo multipart e o fluxo completo foram confirmados diretamente pela API.

## Matriz de verificacao

| Verificacao | Status | Evidencia |
|---|---|---|
| `mvn clean test` | PASSOU | 2 testes, 0 falhas/erros |
| `mvn clean package` | PASSOU | BUILD SUCCESS e JAR gerado |
| API `/health`, `/upload-config` | PASSOU | HTTP 200; UP; limite 20 MiB, duracao 20 s, origem 2160p, tres perfis |
| `GET /jobs`, `/posts`, `/jobs/{id}/arquivos` | PASSOU | Listagens retornaram job, post e 3 arquivos |
| Upload multipart do MP4 sintético | PASSOU | HTTP 202; job `1df32747-b317-42e6-97aa-5c44e56f5410` |
| Estados do job | PASSOU | Resposta inicial observada em PROCESSING; terminal COMPLETED |
| 360p | PASSOU | HTTP 200, 238.227 bytes, 640x360, 6 s, decodificacao FFmpeg exit 0 |
| 480p | PASSOU | HTTP 200, 476.735 bytes, 854x480, 6 s, decodificacao FFmpeg exit 0 |
| 720p | PASSOU | HTTP 200, 1.607.636 bytes, 1280x720, 6 s, decodificacao FFmpeg exit 0 |
| Falhas de entrada | PASSOU | Perfil inválido HTTP 400; origem ausente HTTP 400; assinatura inválida HTTP 415 |
| Falha do ffprobe e retentativa | PASSOU | Job FAILED; `POST /tentativas` HTTP 202 e QUEUED; voltou a FAILED com erro genérico sem caminho local |
| Swing abre | PASSOU | Janela visível, captura de tela real e health ativo |
| Upload acionado por clique no Swing | NÃO EXECUTADO | Clique automatizado não atingiu o seletor; upload API foi validado integralmente |
| Limite de corpo, duração >20 s e fila lotada | NÃO EXECUTADO | Não exercitados neste ciclo |

## Bugs corrigidos

- `pom.xml` usava diretórios Maven padrão da raiz e reportava sucesso sem compilar os 38 fontes existentes em `api/src/main/java`. Configurados os diretórios de fonte e teste correspondentes à estrutura do repositório.
- A assinatura MP4 era comparada incorretamente: lia oito bytes (`ftypisom`) e comparava com `ftyp`. Agora verifica os quatro bytes de assinatura. Dois testes JUnit cobrem assinatura presente/ausente.
- Upload aceito falhava ao serializar `Instant`, pois faltava `jackson-datatype-jsr310`. Dependência 2.18.3 adicionada junto ao Databind existente.
- Exceções de execução de FFmpeg/ffprobe expunham caminho local no erro do job. A fila agora mantém mensagens controladas de `ApiException`, registra somente a classe da exceção interna e devolve mensagem genérica para falhas externas.
- README recebeu exemplo PowerShell com o argumento Swing preservado como um único argumento.
- No modo `--swing`, `main` retornava após abrir a janela. Agora mantém a aplicação ativa até encerramento, assim como no modo API.
- Os componentes do perfil e da origem esticavam verticalmente no `BoxLayout`; as alturas agora ficam limitadas ao tamanho de controles de formulário.

## Limites e observacoes

Os jobs e a fila continuam em memoria; reiniciar perde o estado dos jobs, embora os arquivos persistam no diretório configurado. O smoke completo usou dados e binários em `%TEMP%`. O Swing foi aberto e inspecionado; após a correção do ciclo de vida, o processo permaneceu ativo. A automação de clique foi roteada ao Explorador em vez do Swing, então a seleção/envio visual continua sem confirmação. O upload e a conversão foram validados via API com o mesmo arquivo sintético.

As variáveis `FFMPEG_BIN`, `FFPROBE_BIN`, `TRANSCODER_DATA_DIR` e `PORT` são configuradas por processo. Para reprodução local, aponte `FFMPEG_BIN` e `FFPROBE_BIN` para uma distribuição completa; não é necessário adicioná-la ao PATH nem instalar globalmente.
