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
