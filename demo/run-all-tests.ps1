param([int]$Port=18083)
$ErrorActionPreference='Stop';$root=Join-Path $env:TEMP 'youtube-unit-qa-evidence';New-Item -ItemType Directory -Force $root|Out-Null; if((Invoke-RestMethod "http://127.0.0.1:$Port/health").status -ne 'UP'){throw 'API indisponível'}; & "$PSScriptRoot\test-concorrencia.ps1" -Port $Port; if($LASTEXITCODE -ne 0){exit $LASTEXITCODE}; Write-Output "Evidências em $root"; exit 0
