# Resumo para apresentação

- O sistema é um conversor local assíncrono em Java 17, com API HTTP nativa, Swing e FFmpeg.
- Foram gerados e processados vídeos sintéticos de 480p/3s, 720p/6s e 1080p/10s.
- Os arquivos foram convertidos para perfis 360p, 480p e 720p quando aplicável.
- As saídas foram baixadas pela API e validadas com ffprobe: codec H.264, dimensões e durações corretas.
- O limite de três jobs ativos é protegido por reserva sincronizada.
- O Swing agora usa editor de célula para os botões de download, evita envio duplicado e grava downloads em arquivo temporário antes de substituir o destino.
- A validação visual por clique depende de uma sessão gráfica; a execução desta homologação não produziu screenshot nem declarou clique real.
- Os testes automatizados atuais têm 2 testes JUnit; os demais resultados estão nos arquivos temporários indicados no relatório completo.
