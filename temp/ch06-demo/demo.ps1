# =============================================================================
#  demo.ps1 — chay server roi in ra danh sach URL de demo
#  Dung: powershell -ExecutionPolicy Bypass -File demo.ps1
# =============================================================================
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

Write-Host ''
Write-Host '  CAC URL DE DEMO' -ForegroundColor Cyan
Write-Host '  ---------------------------------------------------------------'
Write-Host '   Trang demo (bat dau tu day)   http://localhost:8080/temp/'
Write-Host ''
Write-Host '   Trang chinh                   http://localhost:8080/'
Write-Host '   Loi 404                       http://localhost:8080/404'
Write-Host '   Loi 403                       http://localhost:8080/403'
Write-Host '   Loi 500                       http://localhost:8080/500'
Write-Host '   Loi 500 do EL (slide 34)      http://localhost:8080/temp/loi_el.jsp'
Write-Host '   URL bia -> 404                http://localhost:8080/bat-ky-gi'
Write-Host '  ---------------------------------------------------------------'
Write-Host '   Ctrl+C de dung server'
Write-Host ''

& "$PSScriptRoot\run.ps1" @args
