# =============================================================================
#  test.ps1 - Chay test tu dong
# =============================================================================
#  Chay:  powershell -ExecutionPolicy Bypass -File scripts\test.ps1
#
#  KHONG CAN MYSQL, KHONG CAN CHAY WEB.
#    Test o day chi kiem cac ham THUAN: cho dau vao, nhan dau ra, khong dung
#    toi CSDL hay session. Vi vay chay trong vai giay va khong bao gio "lan
#    nay hong lan sau chay" vi du lieu khac nhau.
#
#    Do cung la ly do chon dung may ham nay de viet test truoc: chung la phan
#    de test nhat MA VAN tung co loi that.
#
#  DUNG JUnit 5 ban "console standalone" - mot file jar chay duoc thang, khong
#  can Maven hay Gradle. Hop voi du an nay: run.ps1 cung dich bang javac chu
#  khong qua cong cu build nao.
# =============================================================================
$ErrorActionPreference = 'Stop'

# Ten test viet bang tieng Viet, khong dat dong nay thi console in ra dau hoi.
try { [Console]::OutputEncoding = [System.Text.Encoding]::UTF8 } catch { }

$ProjectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $ProjectRoot

$libs       = Join-Path $ProjectRoot '.libs'
$classes    = Join-Path $ProjectRoot 'build\classes'
$testOut    = Join-Path $ProjectRoot 'build\test-classes'
$repo       = 'https://repo1.maven.org/maven2'
$junitJar   = Join-Path $libs 'junit-platform-console-standalone-1.10.2.jar'

New-Item -ItemType Directory -Force -Path $libs, $testOut | Out-Null

# ---- Tai JUnit neu chua co -------------------------------------------------
if (-not (Test-Path $junitJar)) {
    Write-Host 'Tai JUnit 5 (~2.5 MB, chi lan dau)...' -ForegroundColor Yellow
    Invoke-WebRequest -UseBasicParsing -OutFile $junitJar `
        -Uri "$repo/org/junit/platform/junit-platform-console-standalone/1.10.2/junit-platform-console-standalone-1.10.2.jar"
}

# ---- Dich code chinh truoc -------------------------------------------------
#  Test kiem code that, nen code that phai duoc dich lai truoc moi lan chay.
#  Bo qua buoc nay thi test chay tren ban .class cu va bao "xanh" cho mot loi
#  vua duoc them vao - kieu nham nguy hiem nhat.
$cp = (Get-ChildItem $libs -Filter *.jar | ForEach-Object { $_.FullName }) -join ';'

Write-Host 'Dich code chinh...' -ForegroundColor DarkGray
New-Item -ItemType Directory -Force -Path $classes | Out-Null
$sources = Get-ChildItem 'src\main\java' -Recurse -Filter *.java |
           ForEach-Object { $_.FullName }
& javac -encoding UTF-8 -cp $cp -d $classes @sources
if ($LASTEXITCODE -ne 0) {
    Write-Host 'Code chinh khong dich duoc. Sua loi bien dich truoc da.' -ForegroundColor Red
    exit 1
}

# ---- Dich test -------------------------------------------------------------
Write-Host 'Dich test...' -ForegroundColor DarkGray
$testSources = Get-ChildItem 'src\test\java' -Recurse -Filter *.java |
               ForEach-Object { $_.FullName }
if (-not $testSources) {
    Write-Host 'Khong tim thay file test nao trong src\test\java.' -ForegroundColor Red
    exit 1
}
& javac -encoding UTF-8 -cp "$cp;$classes" -d $testOut @testSources
if ($LASTEXITCODE -ne 0) {
    Write-Host 'Test khong dich duoc.' -ForegroundColor Red
    exit 1
}

# ---- Chay ------------------------------------------------------------------
Write-Host ''
& java '-Dfile.encoding=UTF-8' -jar $junitJar execute `
    --class-path "$classes;$testOut" `
    --scan-class-path $testOut `
    --details=tree `
    --disable-banner

# Tra ve dung ma thoat cua JUnit: 0 = tat ca dat.
# Can cho viec chay tu dong sau nay - script goi test phai biet no hong hay khong.
exit $LASTEXITCODE
