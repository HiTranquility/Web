# =============================================================================
#  migrate-004.ps1 - Chạy migration thêm chapter_id cho bảng comments (ISSUE-004)
# =============================================================================
#  Chạy:  powershell -ExecutionPolicy Bypass -File scripts\migrate-004.ps1
# =============================================================================
param(
    [string]$MysqlUser = 'root',
    [string]$Database  = 'webdoctruyen',
    [string]$RootPassword = ''
)

$ErrorActionPreference = 'Stop'
$ProjectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $ProjectRoot

# ---- Tim mysql.exe ---------------------------------------------------------
$mysql = (Get-Command mysql -ErrorAction SilentlyContinue).Source
if (-not $mysql) {
    $found = Get-ChildItem 'C:\Program Files\MySQL' -Recurse -Filter 'mysql.exe' `
                -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($found) { $mysql = $found.FullName }
}
if (-not $mysql) {
    Write-Host 'Khong tim thay mysql.exe.' -ForegroundColor Red
    exit 1
}

# ---- Doc mat khau root -----------------------------------------------------
$rootPlain = $RootPassword
if ([string]::IsNullOrEmpty($rootPlain)) {
    Write-Host ''
    Write-Host '  Nhap mat khau ROOT cua MySQL (go xong bam Enter, man hinh khong hien):' -ForegroundColor Cyan
    $secure = Read-Host -AsSecureString '  Mat khau root'
    $rootPlain = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto(
        [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure))
}

if ([string]::IsNullOrEmpty($rootPlain)) {
    Write-Host '  Chua nhap mat khau root. Dung lai.' -ForegroundColor Red
    exit 1
}

$cnf = Join-Path $env:TEMP "mysqlcnf_$(Get-Random).cnf"

try {
    @"
[client]
user=$MysqlUser
password="$rootPlain"
default-character-set=utf8mb4
"@ | Set-Content -LiteralPath $cnf -Encoding ASCII

    $acl = Get-Acl -LiteralPath $cnf
    $acl.SetAccessRuleProtection($true, $false)
    $acl.AddAccessRule((New-Object System.Security.AccessControl.FileSystemAccessRule(
        $env:USERNAME, 'FullControl', 'Allow')))
    Set-Acl -LiteralPath $cnf -AclObject $acl

    $rootPlain = $null
    [System.GC]::Collect()

    Write-Host ''
    Write-Host 'Dang thuc thi database/migration-004-chapter-comments.sql...' -ForegroundColor Yellow

    & $mysql "--defaults-extra-file=$cnf" $Database -e "source database/migration-004-chapter-comments.sql"
    if ($LASTEXITCODE -ne 0) {
        Write-Host '  Chay migration that bai. Kiem tra lai mat khau root.' -ForegroundColor Red
        exit 1
    }

    Write-Host '  => Chay migration thanh cong! Bang comments da ho tro chapter_id cho ISSUE-004.' -ForegroundColor Green
    Write-Host ''

} finally {
    Remove-Item -LiteralPath $cnf -Force -ErrorAction SilentlyContinue
}
