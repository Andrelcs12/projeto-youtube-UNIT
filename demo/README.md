# Demonstração rápida

1. Instale Java 17, Maven 3.9+ e uma distribuição FFmpeg completa.
2. Configure `FFMPEG_BIN` e `FFPROBE_BIN` apontando para os executáveis.
3. Execute `.demo\start.ps1`.
4. Verifique `http://127.0.0.1:18082/health` ou use `mvn exec:java "-Dexec.args=--swing"`.

Para a apresentação: mostrar upload de arquivo local autorizado, estado `QUEUED/PROCESSING`, conclusão e download de um perfil. Se o Swing não estiver disponível, use as rotas documentadas no README (`POST /videos`, `GET /jobs/{id}`, `GET /arquivos/{id}`).
