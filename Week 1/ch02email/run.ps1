# Builds and runs the Chapter 2 Email List app on an embedded Tomcat 9.
# Usage:  .\run.ps1            -> http://localhost:8080/ch02email/
#         .\run.ps1 -Port 9090
param([int]$Port = 8080)

$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

$libs = Join-Path $PSScriptRoot '.libs'
$build = Join-Path $PSScriptRoot 'build'
$classes = Join-Path $build 'classes'
$toolsOut = Join-Path $build 'tools'
$repo = 'https://repo1.maven.org/maven2'

function Get-LatestVersion($groupPath, $artifact, $prefix) {
    $url = "$repo/$groupPath/$artifact/maven-metadata.xml"
    $xml = [xml](Invoke-WebRequest -Uri $url -UseBasicParsing).Content
    ($xml.metadata.versioning.versions.version | Where-Object { $_ -like "$prefix*" } | Select-Object -Last 1)
}

New-Item -ItemType Directory -Force -Path $libs, $classes, $toolsOut | Out-Null

# ---- 1. dependencies -------------------------------------------------------
$tomcatVersion = Get-LatestVersion 'org/apache/tomcat/embed' 'tomcat-embed-core' '9.0.'
$jars = @(
    @{ path = "org/apache/tomcat/embed/tomcat-embed-core/$tomcatVersion";        file = "tomcat-embed-core-$tomcatVersion.jar" },
    @{ path = "org/apache/tomcat/embed/tomcat-embed-jasper/$tomcatVersion";      file = "tomcat-embed-jasper-$tomcatVersion.jar" },
    @{ path = "org/apache/tomcat/embed/tomcat-embed-el/$tomcatVersion";          file = "tomcat-embed-el-$tomcatVersion.jar" },
    @{ path = "org/apache/tomcat/tomcat-annotations-api/$tomcatVersion";         file = "tomcat-annotations-api-$tomcatVersion.jar" },
    @{ path = "org/eclipse/jdt/ecj/3.33.0";                                      file = "ecj-3.33.0.jar" }
)
foreach ($jar in $jars) {
    $dest = Join-Path $libs $jar.file
    if (-not (Test-Path $dest)) {
        Write-Host "Downloading $($jar.file) ..."
        Invoke-WebRequest -Uri "$repo/$($jar.path)/$($jar.file)" -OutFile $dest -UseBasicParsing
    }
}
$cp = (Get-ChildItem $libs -Filter *.jar | ForEach-Object { $_.FullName }) -join ';'

# ---- 2. compile ------------------------------------------------------------
Write-Host 'Compiling application classes ...'
$sources = Get-ChildItem -Recurse -Path 'src\main\java' -Filter *.java | ForEach-Object { $_.FullName }
& javac -encoding UTF-8 -cp $cp -d $classes @sources
if ($LASTEXITCODE -ne 0) { throw 'Compilation of src/main/java failed.' }

Write-Host 'Compiling dev server ...'
& javac -encoding UTF-8 -cp $cp -d $toolsOut 'tools\DevServer.java'
if ($LASTEXITCODE -ne 0) { throw 'Compilation of tools/DevServer.java failed.' }

# ---- 3. run ----------------------------------------------------------------
& java -cp "$toolsOut;$cp" "-Dport=$Port" DevServer
