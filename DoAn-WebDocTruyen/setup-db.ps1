# =============================================================================
#  setup-db.ps1 — Cai dat toan bo database bang MOT lenh
# =============================================================================
#  Chay:  powershell -ExecutionPolicy Bypass -File setup-db.ps1
#
#  Script se:
#    1. Tao database + 7 bang        (schema.sql)
#    2. Tao tai khoan MySQL cho app  (setup_user.sql)
#    3. Nap du lieu mau              (sample_data.sql)
#    4. Sinh file db.properties
#
#  BAN CHI PHAI GO MAT KHAU ROOT MOT LAN, va go thang vao mysql —
#  script khong luu, khong doc, khong nhin thay mat khau root cua ban.
# =============================================================================
param(
    [string]$MysqlUser = 'root',
    [string]$AppPassword = ''
)

$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

# ---- Tim mysql.exe ---------------------------------------------------------
$mysql = (Get-Command mysql -ErrorAction SilentlyContinue).Source
if (-not $mysql) {
    $candidates = Get-ChildItem 'C:\Program Files\MySQL' -Recurse -Filter 'mysql.exe' `
                    -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($candidates) { $mysql = $candidates.FullName }
}
if (-not $mysql) {
    Write-Host 'Khong tim thay mysql.exe.' -ForegroundColor Red
    Write-Host 'Cai MySQL 8 roi chay lai, hoac them thu muc bin cua MySQL vao PATH.'
    exit 1
}
Write-Host "MySQL client: $mysql" -ForegroundColor DarkGray

# ---- Mat khau cho tai khoan ung dung (KHAC mat khau root) ------------------
if (-not $AppPassword) {
    # Sinh ngau nhien de ban khoi phai nghi. Doi bang -AppPassword neu muon.
    $bytes = New-Object byte[] 12
    [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
    $AppPassword = [Convert]::ToBase64String($bytes) -replace '[^A-Za-z0-9]', ''
    if ($AppPassword.Length -lt 12) { $AppPassword = $AppPassword + 'Aa1' }
}

Write-Host ''
Write-Host '  Se chay 3 buoc. Nhap mat khau ROOT cua MySQL khi duoc hoi.' -ForegroundColor Cyan
Write-Host '  (go thang vao mysql, script khong nhin thay)' -ForegroundColor DarkGray
Write-Host ''

# ---- 1. Schema -------------------------------------------------------------
Write-Host '[1/4] Tao database va 7 bang...' -ForegroundColor Yellow
& $mysql -u $MysqlUser -p --default-character-set=utf8mb4 -e "source database/schema.sql"
if ($LASTEXITCODE -ne 0) { Write-Host 'That bai o buoc 1.' -ForegroundColor Red; exit 1 }

# ---- 2. Tai khoan ung dung -------------------------------------------------
# Sinh cau lenh tai cho, khong sua file setup_user.sql — de mat khau khong
# bao gio nam trong file duoc commit len git.
Write-Host '[2/4] Tao tai khoan MySQL cho ung dung...' -ForegroundColor Yellow
$grantSql = @"
CREATE USER IF NOT EXISTS 'truyen_app'@'localhost' IDENTIFIED BY '$AppPassword';
ALTER USER 'truyen_app'@'localhost' IDENTIFIED BY '$AppPassword';
GRANT SELECT, INSERT, UPDATE, DELETE ON webdoctruyen.* TO 'truyen_app'@'localhost';
FLUSH PRIVILEGES;
"@
$tmp = Join-Path $env:TEMP "grant_$(Get-Random).sql"
try {
    Set-Content -LiteralPath $tmp -Value $grantSql -Encoding UTF8
    & $mysql -u $MysqlUser -p --default-character-set=utf8mb4 -e "source $tmp"
    if ($LASTEXITCODE -ne 0) { Write-Host 'That bai o buoc 2.' -ForegroundColor Red; exit 1 }
} finally {
    # Xoa ngay file tam — no chua mat khau cua tai khoan ung dung
    Remove-Item -LiteralPath $tmp -Force -ErrorAction SilentlyContinue
}

# ---- 3. Du lieu mau --------------------------------------------------------
Write-Host '[3/4] Nap du lieu mau...' -ForegroundColor Yellow
& $mysql -u $MysqlUser -p --default-character-set=utf8mb4 webdoctruyen -e "source database/sample_data.sql"
if ($LASTEXITCODE -ne 0) { Write-Host 'That bai o buoc 3.' -ForegroundColor Red; exit 1 }

# ---- 4. db.properties ------------------------------------------------------
Write-Host '[4/4] Ghi file db.properties...' -ForegroundColor Yellow
$props = @"
# Sinh tu dong boi setup-db.ps1 — KHONG commit file nay len git
db.url=jdbc:mysql://localhost:3306/webdoctruyen?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true&useSSL=false
db.username=truyen_app
db.password=$AppPassword
"@
New-Item -ItemType Directory -Force -Path 'src/main/resources' | Out-Null
Set-Content -LiteralPath 'src/main/resources/db.properties' -Value $props -Encoding UTF8

Write-Host ''
Write-Host '  ================================================' -ForegroundColor Green
Write-Host '   XONG. Database da san sang.' -ForegroundColor Green
Write-Host '  ================================================' -ForegroundColor Green
Write-Host ''
Write-Host '   Chay web:   powershell -ExecutionPolicy Bypass -File run.ps1'
Write-Host '   Mo:         http://localhost:8080/'
Write-Host ''
Write-Host '   TAI KHOAN DANG NHAP' -ForegroundColor Cyan
Write-Host '     admin    / admin123   <- quan tri vien'
Write-Host '     mocmien  / 123456     <- tac gia'
Write-Host '     thuytien / 123456     <- doc gia'
Write-Host '     spammer  / 123456     <- da bi khoa (thu de xem co che chan)'
Write-Host ''
