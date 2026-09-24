$candidates = @(
    'password', 'admin123', 'root1234', 'mysql123', 'Admin123', 'root@123', 
    '12344321', '87654321', 'webadmin', 'rootroot', 'Admin@123', 'Admin@1',
    'mysql80', 'root80', 'root123', 'Admin1234', '11111111', '00000000',
    'adminadmin', 'mysqlmysql', 'rootpass', 'admin@12', 'admin@1234',
    '1234567a', '12345678a', 'admin12345', 'webdoctruyen', 'webdoc12'
)

$mysql = 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe'

foreach ($p in $candidates) {
    & $mysql -u root "--password=$p" -e "SELECT 1;" 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "FOUND_PASS: $p"
        exit 0
    }
}
Write-Host "NOT_FOUND"
