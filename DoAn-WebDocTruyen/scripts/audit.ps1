# Rà soát hoàn toàn local. Không tải dependency, không nạp cấu hình/dữ liệu thật.
$ErrorActionPreference = 'Stop'
$auditProject = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $auditProject
$auditRoot = Join-Path $auditProject ('build\audit\run-' + [Guid]::NewGuid().ToString('N'))
$auditClasses = Join-Path $auditRoot 'classes'
$auditTests = Join-Path $auditRoot 'test-classes'
$auditTools = Join-Path $auditRoot 'tools'
$auditJunit = Join-Path $auditProject '.libs\junit-platform-console-standalone-1.10.2.jar'
if (-not (Test-Path -LiteralPath $auditJunit)) { throw 'Thieu JUnit local trong .libs. Audit khong tai file qua mang.' }
New-Item -ItemType Directory -Force -Path $auditClasses,$auditTests,$auditTools | Out-Null
$auditCp = (Get-ChildItem -LiteralPath (Join-Path $auditProject '.libs') -Filter *.jar | ForEach-Object FullName) -join ';'
$auditSources = Get-ChildItem -LiteralPath 'src\main\java' -Recurse -Filter *.java | ForEach-Object FullName
& javac -encoding UTF-8 -cp $auditCp -d $auditClasses @auditSources
if ($LASTEXITCODE -ne 0) { throw 'Main Java compile failed' }
$auditTestSources = Get-ChildItem -LiteralPath 'src\test\java' -Recurse -Filter *.java | ForEach-Object FullName
& javac -encoding UTF-8 -cp "$auditCp;$auditClasses" -d $auditTests @auditTestSources
if ($LASTEXITCODE -ne 0) { throw 'Test Java compile failed' }
$auditNetworkFlags = @('-Dhttp.proxyHost=127.0.0.1', '-Dhttp.proxyPort=9', '-Dhttps.proxyHost=127.0.0.1', '-Dhttps.proxyPort=9')
# Chỉ các class vừa biên dịch; không copy src/main/resources.
$ErrorActionPreference = 'Continue' # Windows PowerShell 5 xem stderr của Java như ErrorRecord.
& java '-Dfile.encoding=UTF-8' @auditNetworkFlags -jar $auditJunit execute `
    --class-path "$auditCp;$auditClasses;$auditTests" --scan-class-path $auditTests `
    --details=summary --disable-banner *> (Join-Path $auditRoot 'unit-tests.log')
$auditUnitExit = $LASTEXITCODE
$ErrorActionPreference = 'Stop'
Get-Content -LiteralPath (Join-Path $auditRoot 'unit-tests.log') | Select-Object -Last 16
if ($auditUnitExit -ne 0) { throw "JUnit failed. See $auditRoot\unit-tests.log" }
& javac -encoding UTF-8 -cp "$auditCp;$auditClasses" -d $auditTools 'tools\LocalAuditSmoke.java'
if ($LASTEXITCODE -ne 0) { throw 'Smoke tool compile failed' }
$ErrorActionPreference = 'Continue'
& java '-Dfile.encoding=UTF-8' @auditNetworkFlags "-Daudit.root=$auditRoot" `
    -cp "$auditCp;$auditClasses;$auditTools" LocalAuditSmoke *> (Join-Path $auditRoot 'http-smoke.log')
$auditHttpExit = $LASTEXITCODE
$ErrorActionPreference = 'Stop'
Get-Content -LiteralPath (Join-Path $auditRoot 'http-smoke.log') | Select-String 'PASS |HTTP_SMOKE_OK|AssertionError'
if ($auditHttpExit -ne 0) { throw "HTTP smoke failed. See $auditRoot\http-smoke.log" }
$env:AUDIT_RENDERED_DIR = Join-Path $auditRoot 'rendered'
try {
    & node --test scripts/audit-web.test.cjs scripts/achievements.test.cjs
    if ($LASTEXITCODE -ne 0) { throw 'JavaScript tests failed' }
} finally { Remove-Item Env:\AUDIT_RENDERED_DIR -ErrorAction SilentlyContinue }
& node --check src/main/webapp/assets/js/firebase-auth.js
if ($LASTEXITCODE -ne 0) { throw 'Firebase JavaScript syntax failed' }
Write-Host "LOCAL_AUDIT_OK $auditRoot"
