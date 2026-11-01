param(
    [string]$Task = 'Read tools/offline-harness/TASKS.md. Inspect the first incomplete task and implement only that task. Verify the change and update its status with evidence. Do not commit.',
    [string]$HarnessRoot = 'D:\Projects\deepseek-harness'
)
$ErrorActionPreference = 'Stop'
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$env:DSH_HOME = Join-Path $projectRoot '.artifacts\offline-harness'
$env:ALERTMESH_LOCAL_KEY = 'ollama-local-only'
$env:DSH_TELEMETRY_DISABLED = '1'
$model = (Invoke-RestMethod 'http://127.0.0.1:11434/api/tags' -TimeoutSec 10).models |
    Where-Object name -eq 'local-code:latest'
if (-not $model) { throw 'Required offline model local-code:latest is not installed.' }
if ($model.details.parameter_size -ne '3.84B') { throw 'Model changed: review size before running.' }
$launcher = Join-Path $HarnessRoot 'apps\cli\lib\bin.js'
if (-not (Test-Path -LiteralPath $launcher)) { throw "Built DeepSeek Harness missing: $launcher" }
New-Item -ItemType Directory -Force -Path $env:DSH_HOME | Out-Null
Push-Location $projectRoot
try {
    & node $launcher --profile headless --patch (Join-Path $PSScriptRoot 'cordis.patch.yml') $Task
    if ($LASTEXITCODE -ne 0) { throw "DeepSeek Harness exited with code $LASTEXITCODE" }
} finally { Pop-Location }
