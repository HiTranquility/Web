# =============================================================================
#  setup-db.ps1 - Cai dat toan bo database bang MOT lenh
# =============================================================================
#  Chay:  powershell -ExecutionPolicy Bypass -File scripts\setup-db.ps1
#
#  Script se:
#    1. Tao database + 13 bang       (schema.sql)
#    2. Tao tai khoan MySQL cho app
#    3. Nap du lieu mau              (sample_data.sql)
#    4. Sinh file db.properties
#
#  CHI HOI MAT KHAU ROOT MOT LAN.
#    Ban truoc goi mysql ba lan voi -p nen phai go mat khau ba lan. Nay doc
#    mot lan roi ghi vao mot file cau hinh TAM, dua cho mysql bang
#    --defaults-extra-file, va xoa file do ngay trong khoi finally.
#
#    Vi sao khong dung bien moi truong MYSQL_PWD cho gon: chinh tai lieu cua
#    MySQL khuyen khong dung, vi tien trinh khac tren may doc duoc bien moi
#    truong. File tam song vai giay va chi minh ban doc duoc thi kin hon.
#
#    Mat khau root KHONG bao gio duoc ghi vao db.properties, khong ghi ra man
#    hinh, khong luu vao lich su lenh.
# =============================================================================
param(
    [string]$MysqlUser = 'root',
    [string]$AppPassword = ''
)

$ErrorActionPreference = 'Stop'
# Script nam trong scripts/ -> lui ve thu muc goc du an
$ProjectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $ProjectRoot

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

# ---- Kiem tra dich vu MySQL truoc khi hoi mat khau -------------------------
# Hoi mat khau roi moi bao "MySQL chua chay" la lang phi mot lan go.
$svc = Get-Service -Name 'MySQL*' -ErrorAction SilentlyContinue |
       Where-Object { $_.Status -eq 'Running' } | Select-Object -First 1
if (-not $svc) {
    Write-Host ''
    Write-Host '  Dich vu MySQL chua chay.' -ForegroundColor Red
    Write-Host '  Bat len roi chay lai:  net start MySQL80' -ForegroundColor Yellow
    Write-Host ''
    exit 1
}
Write-Host "Dich vu:      $($svc.Name) (dang chay)" -ForegroundColor DarkGray

# ---- Mat khau cho tai khoan ung dung (KHAC mat khau root) ------------------
if (-not $AppPassword) {
    # Sinh ngau nhien de ban khoi phai nghi. Doi bang -AppPassword neu muon.
    $bytes = New-Object byte[] 12
    [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
    $AppPassword = [Convert]::ToBase64String($bytes) -replace '[^A-Za-z0-9]', ''
    if ($AppPassword.Length -lt 12) { $AppPassword = $AppPassword + 'Aa1' }
}

# ---- Doc mat khau root DUNG MOT LAN ----------------------------------------
Write-Host ''
Write-Host "  Nhap mat khau ROOT cua MySQL (go xong bam Enter, man hinh khong hien)" -ForegroundColor Cyan
$secure = Read-Host -AsSecureString "  Mat khau root"
$rootPlain = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure))

if ([string]::IsNullOrEmpty($rootPlain)) {
    Write-Host '  Chua nhap gi ca. Dung lai.' -ForegroundColor Red
    exit 1
}

$cnf = Join-Path $env:TEMP "mysqlcnf_$(Get-Random).cnf"
$ok = $false

try {
    # File cau hinh tam. Dat quyen chi minh chu so huu doc duoc.
    @"
[client]
user=$MysqlUser
password="$rootPlain"
default-character-set=utf8mb4
"@ | Set-Content -LiteralPath $cnf -Encoding ASCII

    $acl = Get-Acl -LiteralPath $cnf
    $acl.SetAccessRuleProtection($true, $false)   # bo quyen ke thua
    $acl.AddAccessRule((New-Object System.Security.AccessControl.FileSystemAccessRule(
        $env:USERNAME, 'FullControl', 'Allow')))
    Set-Acl -LiteralPath $cnf -AclObject $acl

    # Xoa bien chua mat khau ro ngay khi khong can nua
    $rootPlain = $null
    [System.GC]::Collect()

    # ---- 1. Schema ---------------------------------------------------------
    Write-Host ''
    Write-Host '[1/4] Tao database va 13 bang...' -ForegroundColor Yellow
    & $mysql "--defaults-extra-file=$cnf" -e "source database/schema.sql"
    if ($LASTEXITCODE -ne 0) {
        Write-Host ''
        Write-Host '  That bai o buoc 1 - tao database.' -ForegroundColor Red
        Write-Host '  Nguyen nhan thuong gap:' -ForegroundColor Yellow
        Write-Host '    - Go sai mat khau root'
        Write-Host '    - MySQL cu hon 8.0.16 (schema dung rang buoc CHECK)'
        Write-Host ''
        exit 1
    }

    # ---- 2. Tai khoan ung dung ---------------------------------------------
    # Sinh cau lenh tai cho, khong sua file setup_user.sql - de mat khau khong
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

        # DAU \ PHAI DOI THANH / TRUOC KHI DUA VAO LENH source.
        #
        # mysql client coi \ trong "source ..." la ky tu ESCAPE, khong phai
        # dau phan cach thu muc. Duong dan tam kieu Windows
        #     C:\Users\Admin\AppData\Local\Temp\grant_123.sql
        # bi doc thanh \U -> mysql bao "ERROR at line 1: Unknown command '\U'"
        # va buoc 2 chet, trong khi buoc 1 vua chay xong binh thuong.
        #
        # Buoc 1 va 3 khong dinh loi nay vi chung dung duong dan TUONG DOI
        # (database/schema.sql) von da co san dau /.
        $tmpFwd = $tmp -replace '\\', '/'
        & $mysql "--defaults-extra-file=$cnf" -e "source $tmpFwd"
        if ($LASTEXITCODE -ne 0) {
            Write-Host '  That bai o buoc 2 - tao tai khoan MySQL.' -ForegroundColor Red
            exit 1
        }
    } finally {
        # Xoa ngay - file nay chua mat khau cua tai khoan ung dung
        Remove-Item -LiteralPath $tmp -Force -ErrorAction SilentlyContinue
    }

    # ---- 3. Du lieu mau ----------------------------------------------------
    Write-Host '[3/4] Nap du lieu mau...' -ForegroundColor Yellow
    & $mysql "--defaults-extra-file=$cnf" webdoctruyen -e "source database/sample_data.sql"
    if ($LASTEXITCODE -ne 0) {
        Write-Host '  That bai o buoc 3 - nap du lieu mau.' -ForegroundColor Red
        Write-Host '  Database va cac bang da tao xong; chi thieu du lieu mau.' -ForegroundColor Yellow
        exit 1
    }

    $ok = $true

} finally {
    # Xoa file cau hinh tam DU CO LOI HAY KHONG.
    # Day la ly do phai dung try/finally chu khong xoa o cuoi script: script
    # thoat giua chung vi loi thi dong xoa o cuoi khong bao gio chay.
    Remove-Item -LiteralPath $cnf -Force -ErrorAction SilentlyContinue
}

if (-not $ok) { exit 1 }

# ---- 4. db.properties ------------------------------------------------------
Write-Host '[4/4] Ghi file db.properties...' -ForegroundColor Yellow
$props = @"
# Sinh tu dong boi setup-db.ps1 - KHONG commit file nay len git
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
Write-Host '   Chay web:   powershell -ExecutionPolicy Bypass -File scripts\run.ps1'
Write-Host '   Mo:         http://localhost:8080/'
Write-Host ''
Write-Host '   TAI KHOAN DANG NHAP' -ForegroundColor Cyan
Write-Host '     admin    / admin123   <- quan tri vien'
Write-Host '     mocmien  / 123456     <- tac gia'
Write-Host '     thuytien / 123456     <- doc gia'
Write-Host '     spammer  / 123456     <- da bi khoa (thu de xem co che chan)'
Write-Host ''
