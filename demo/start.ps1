param([int]$Port = 18082)
$ErrorActionPreference = 'Stop'
$mvn = "$env:USERPROFILE\.m2\wrapper\dists\apache-maven-3.9.12-bin\5nmfsn99br87k5d4ajlekdq10k\apache-maven-3.9.12\bin\mvn.cmd"
if (!(Test-Path $mvn)) { throw "Maven 3.9.12 portátil não encontrado no cache documentado." }
$env:PORT = "$Port"
$env:TRANSCODER_DATA_DIR = Join-Path $PSScriptRoot 'data'
& $mvn exec:java
