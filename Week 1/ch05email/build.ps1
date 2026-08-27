# Packages the application as ch05email.war for deployment to a real Tomcat 9.
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

$staging = 'build\war'
Remove-Item -Recurse -Force $staging -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path "$staging\WEB-INF\classes" | Out-Null

Copy-Item -Recurse -Force 'src\main\webapp\*' $staging
Copy-Item -Recurse -Force 'build\classes\*' "$staging\WEB-INF\classes" -ErrorAction SilentlyContinue

if (-not (Test-Path "$staging\WEB-INF\classes\murach")) {
    throw 'No compiled classes found. Run .\run.ps1 once first (it compiles), then re-run this.'
}

$war = Join-Path $PSScriptRoot 'build\ch05email.war'
Remove-Item -Force $war -ErrorAction SilentlyContinue
Compress-Archive -Path "$staging\*" -DestinationPath "$war.zip"
Move-Item "$war.zip" $war
Write-Host "Created $war"
