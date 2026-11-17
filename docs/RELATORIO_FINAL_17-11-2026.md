# Relatório final de homologação — 17/11/2026

## Resultado geral

**Parcialmente homologado.** O build Maven passa e a correção de admissão concorrente foi aplicada. A conversão/download por API havia sido comprovada no ciclo anterior. FFmpeg/ffprobe não estão no `PATH` desta sessão e o fluxo visual completo do Swing permanece sem confirmação real.

## Evidências executadas

| Cenário | Resultado |
|---|---|
| `mvn clean test` | PASSOU; 2 testes, 0 falhas |
| `mvn clean package` | PASSOU; JAR gerado |
| Java | Temurin 17.0.20.1 |
| Maven | 3.9.12 portátil via cache local |
| API upload/conversão/download | PASSOU no ciclo documentado em `VALIDACAO_MVP.md` |
| 1/3/5/10 clientes | NÃO EXECUTADO nesta sessão |
| Swing abrir | PASSOU no ciclo anterior |
| Clique Swing e download visual | NÃO COMPROVADO |

## Correção realizada

`JobRepository.saveActiveIfBelow` faz a reserva de vaga de forma sincronizada. Uploads concorrentes e retentativas deixam de depender de `countActive()` seguido de `save()` separado, evitando ultrapassar o limite de três jobs ativos sob corrida.

## Riscos remanescentes

- FFmpeg/ffprobe dependem de configuração local e não foram encontrados no `PATH`.
- O renderer de download do `Dashboard` não possui editor de célula adequado; o botão aparece, mas a interação real continua pendente.
- Jobs permanecem em memória e são perdidos no reinício.
- Não há autenticação ou isolamento por usuário, conforme escopo do MVP.

Não foram inventadas métricas, screenshots ou aprovações de cenários não executados.

## Atualização de 17/11/2026

FFmpeg 9.0.2 essentials foi baixado para `%TEMP%\\transcoder-ffmpeg-portable-qa`, sem instalação global. `libx264` foi confirmado. A API foi executada na porta 18083 com dados em `%TEMP%\\youtube-unit-qa-api-data`.

Jobs reais concluídos: `145be3d7-1486-4857-8ce7-da52a9b3ed54` (480p/3s), `e5f8bbba-cc3f-42e1-9553-d018b11ca64b` (720p/6s) e `7e7ebf99-9420-45c5-9eef-6a6650b10257` (1080p/10s). Os downloads e hashes estão em `%TEMP%\\youtube-unit-qa-evidence\\downloads.jsonl`.

O Dashboard foi corrigido com `TableCellEditor`, bloqueio de duplo envio e download via arquivo `.part`. `mvn clean test` e `mvn clean package` foram executados novamente com sucesso.

Carga: o primeiro cenário de 1 cliente foi executado e recebeu HTTP 429 porque a instância já tinha três jobs ativos. A execução automatizada dos cenários seguintes não concluiu dentro do comportamento esperado do script e não é declarada como aprovação; os cenários 3/5/10 permanecem pendentes de uma instância isolada por cenário.

## Encerramento da tentativa de matriz isolada

O harness foi refeito para criar uma API, diretório de dados, porta e barreira por cenário. Na execução de validação, uma instância Java deixada por tentativa anterior ocupou a porta `18101`; o log real registrou `java.net.BindException: Address already in use`. O processo foi identificado e encerrado, mas a rodada seguinte não produziu um `summary.json` completo dentro do timeout da sessão. Portanto, 1/3/5/10 continuam **não aprovados**; não há números inventados para a tabela de carga.

Nova tentativa: o executor passou a escolher porta aleatória, registrar PID, usar `taskkill /PID /T` apenas para a árvore iniciada e sempre gerar `summary.json`. O primeiro cenário isolado gerou `clients-1/summary.json`, mas falhou em `startup timeout` apesar do log registrar `API Transcoder disponível em http://127.0.0.1:37620`. Isso evidencia uma incompatibilidade no critério de leitura do health/jobs do próprio harness; a matriz não foi aprovada.

## Correção pontual do startup — cenário 1 aprovado

O defeito era a validação ambígua de `Invoke-RestMethod(.../jobs).Count`. O executor agora separa `/health` e `/jobs`, registra status/corpo/duração/exceção de cada tentativa e valida a lista vazia pelo corpo bruto `[]`. O timeout de startup foi ampliado para 90 segundos.

Execução real aprovada: `%TEMP%\\youtube-unit-concurrency-final4\\clients-1\\summary.json`. Porta escolhida: `30881`. Health: HTTP 200; jobs inicial: HTTP 200 com `[]`; upload: HTTP 202; job: COMPLETED; download: validado com ffprobe; processo encerrado pela árvore do PID criado. O summary registra `result: PASS` e `downloadValid: true`.
