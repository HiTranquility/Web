# =============================================================================
#  them-index.ps1 - Bo sung index con thieu cho database DA CAI SAN
# =============================================================================
#  Chay:  powershell -ExecutionPolicy Bypass -File scripts\them-index.ps1
#
#  VI SAO CAN SCRIPT NAY
#    schema.sql da co day du index, nen ai cai MOI thi khong can chay gi ca.
#    Nhung database cai TU TRUOC do khong tu moc them index khi schema.sql
#    doi - MySQL khong co co che "dong bo lai" nao cho viec do.
#
#    Script nay danh cho dung truong hop day: da co du lieu that, khong muon
#    xoa di cai lai, chi can them phan con thieu.
#
#  VI SAO PHAI DUNG ROOT
#    Tai khoan truyen_app CO Y chi duoc cap SELECT/INSERT/UPDATE/DELETE.
#    Khong co INDEX, khong co ALTER, khong co DROP. Web chay binh thuong voi
#    bay nhieu quyen, va neu co lo hong SQL injection nao lot luoi thi ke tan
#    cong cung khong xoa noi bang nao.
#    Doi lai, viec doi cau truc bang phai lam bang root - dung nhu y do.
#
#  CHAY LAI NHIEU LAN CO SAO KHONG
#    Khong. Script kiem index da ton tai chua roi moi tao. Chay muoi lan cung
#    ra ket qua giong nhau, khong bao loi "Duplicate key name".
# =============================================================================
param(
    [string]$MysqlUser = 'root',
    [string]$Database  = 'webdoctruyen'
)

$ErrorActionPreference = 'Stop'
$ProjectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $ProjectRoot

# ---- Danh sach index can co ------------------------------------------------
# Them dong moi vao day khi schema.sql co them index moi.
$WantedIndexes = @(
    @{
        Table   = 'view_logs'
        Name    = 'idx_viewlog_user'
        Columns = 'user_id, story_id, viewed_at'
        Why     = 'Lich su doc (trang 31) va dai "Doc tiep" o trang chu.'
    }
)

# ---- Tim mysql.exe ---------------------------------------------------------
$mysql = (Get-Command mysql -ErrorAction SilentlyContinue).Source
if (-not $mysql) {
    $found = Get-ChildItem 'C:\Program Files\MySQL' -Recurse -Filter 'mysql.exe' `
                -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($found) { $mysql = $found.FullName }
}
if (-not $mysql) {
    Write-Host 'Khong tim thay mysql.exe.' -ForegroundColor Red
    Write-Host 'Cai MySQL 8 roi chay lai, hoac them thu muc bin cua MySQL vao PATH.'
    exit 1
}
Write-Host "MySQL client: $mysql" -ForegroundColor DarkGray

# ---- Kiem tra dich vu TRUOC khi hoi mat khau -------------------------------
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

# ---- Doc mat khau root -----------------------------------------------------
#  Read-Host -AsSecureString: go xong man hinh khong hien gi.
#  Mat khau KHONG di qua tham so dong lenh - de nguyen tren dong lenh thi no
#  nam trong lich su PowerShell va moi tien trinh khac tren may deu doc duoc
#  danh sach tham so.
Write-Host ''
Write-Host '  Nhap mat khau ROOT cua MySQL (go xong bam Enter, man hinh khong hien)' -ForegroundColor Cyan
$secure = Read-Host -AsSecureString '  Mat khau root'
$rootPlain = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure))

if ([string]::IsNullOrEmpty($rootPlain)) {
    Write-Host '  Chua nhap gi ca. Dung lai.' -ForegroundColor Red
    exit 1
}

$cnf = Join-Path $env:TEMP "mysqlcnf_$(Get-Random).cnf"

try {
    # File cau hinh tam, chi minh chu so huu doc duoc. Xoa o khoi finally.
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

    # ---- Kiem ket noi truoc --------------------------------------------
    & $mysql "--defaults-extra-file=$cnf" -e "USE $Database;" 2>&1 | Out-Null
    if ($LASTEXITCODE -ne 0) {
        Write-Host ''
        Write-Host "  Khong ket noi duoc, hoac khong co database '$Database'." -ForegroundColor Red
        Write-Host '  Sai mat khau root? Thu lai, hoac chay scripts\setup-db.ps1 neu chua cai.' -ForegroundColor Yellow
        exit 1
    }

    Write-Host ''
    $daTao = 0
    $daCo  = 0

    foreach ($ix in $WantedIndexes) {
        # Da co chua? Hoi information_schema chu khong tao bua roi bat loi -
        # bat loi "Duplicate key name" thi khong phan biet duoc voi loi that.
        $dem = & $mysql "--defaults-extra-file=$cnf" -N -B -e @"
SELECT COUNT(*) FROM information_schema.statistics
WHERE table_schema = '$Database'
  AND table_name   = '$($ix.Table)'
  AND index_name   = '$($ix.Name)';
"@
        if ($LASTEXITCODE -ne 0) {
            Write-Host "  Khong kiem tra duoc index $($ix.Name)." -ForegroundColor Red
            exit 1
        }

        if ([int]$dem -gt 0) {
            Write-Host "  [co san]  $($ix.Name) tren $($ix.Table)" -ForegroundColor DarkGray
            $daCo++
            continue
        }

        Write-Host "  [tao moi] $($ix.Name) tren $($ix.Table) ($($ix.Columns))" -ForegroundColor Yellow
        Write-Host "            $($ix.Why)" -ForegroundColor DarkGray

        & $mysql "--defaults-extra-file=$cnf" -e `
            "CREATE INDEX $($ix.Name) ON $Database.$($ix.Table) ($($ix.Columns));"
        if ($LASTEXITCODE -ne 0) {
            Write-Host "  That bai khi tao $($ix.Name)." -ForegroundColor Red
            exit 1
        }
        $daTao++
    }

    Write-Host ''
    Write-Host "  Xong. Tao moi: $daTao  -  Da co san: $daCo" -ForegroundColor Green
    Write-Host ''

} finally {
    # Xoa file cau hinh tam DU CO LOI HAY KHONG.
    # Nam trong finally chu khong phai cuoi khoi try: script dung giua chung
    # vi loi ma file mat khau con nam lai trong TEMP la tinh huong te nhat.
    if (Test-Path -LiteralPath $cnf) {
        Remove-Item -LiteralPath $cnf -Force -ErrorAction SilentlyContinue
    }
}
