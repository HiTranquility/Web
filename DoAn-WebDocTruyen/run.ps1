# =============================================================================
#  Build + chay Web Doc Truyen tren embedded Tomcat 9.
#  Usage:  .\run.ps1            -> http://localhost:8080/webdoctruyen/
#          .\run.ps1 -Port 9090
# =============================================================================
param([int]$Port = 8080)

$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

$libs     = Join-Path $PSScriptRoot '.libs'
$build    = Join-Path $PSScriptRoot 'build'
$classes  = Join-Path $build 'classes'
$toolsOut = Join-Path $build 'tools'
$repo     = 'https://repo1.maven.org/maven2'

function Get-LatestVersion($groupPath, $artifact, $prefix) {
    $url = "$repo/$groupPath/$artifact/maven-metadata.xml"
    $xml = [xml](Invoke-WebRequest -Uri $url -UseBasicParsing).Content
    ($xml.metadata.versioning.versions.version | Where-Object { $_ -like "$prefix*" } | Select-Object -Last 1)
}

New-Item -ItemType Directory -Force -Path $libs, $classes, $toolsOut | Out-Null

# ---- 1. Dependencies --------------------------------------------------------
$tomcatVersion = Get-LatestVersion 'org/apache/tomcat/embed' 'tomcat-embed-core' '9.0.'
$jars = @(
    # Servlet container
    @{ path = "org/apache/tomcat/embed/tomcat-embed-core/$tomcatVersion";   file = "tomcat-embed-core-$tomcatVersion.jar" },
    @{ path = "org/apache/tomcat/embed/tomcat-embed-jasper/$tomcatVersion"; file = "tomcat-embed-jasper-$tomcatVersion.jar" },
    @{ path = "org/apache/tomcat/embed/tomcat-embed-el/$tomcatVersion";     file = "tomcat-embed-el-$tomcatVersion.jar" },
    @{ path = "org/apache/tomcat/tomcat-annotations-api/$tomcatVersion";    file = "tomcat-annotations-api-$tomcatVersion.jar" },
    @{ path = "org/eclipse/jdt/ecj/3.33.0";                                 file = "ecj-3.33.0.jar" },
    # JSTL - cho <c:forEach>, <c:if>, <c:out> trong JSP
    @{ path = "javax/servlet/jstl/1.2";                                     file = "jstl-1.2.jar" },
    @{ path = "taglibs/standard/1.1.2";                                     file = "standard-1.1.2.jar" },
    # Driver MySQL
    @{ path = "com/mysql/mysql-connector-j/8.4.0";                          file = "mysql-connector-j-8.4.0.jar" }
)
foreach ($jar in $jars) {
    $dest = Join-Path $libs $jar.file
    if (-not (Test-Path $dest)) {
        Write-Host "Downloading $($jar.file) ..."
        Invoke-WebRequest -Uri "$repo/$($jar.path)/$($jar.file)" -OutFile $dest -UseBasicParsing
    }
}
$cp = (Get-ChildItem $libs -Filter *.jar | ForEach-Object { $_.FullName }) -join ';'

# ---- 2. Compile -------------------------------------------------------------
Write-Host 'Compiling application classes ...'
$sources = Get-ChildItem -Recurse -Path 'src\main\java' -Filter *.java | ForEach-Object { $_.FullName }
& javac -encoding UTF-8 -cp $cp -d $classes @sources
if ($LASTEXITCODE -ne 0) { throw 'Compile src/main/java that bai.' }

# ---- 3. Copy resources (db.properties) vao classpath ------------------------
# Maven lam viec nay tu dong; o day phai copy tay.
if (Test-Path 'src\main\resources') {
    Copy-Item -Recurse -Force 'src\main\resources\*' $classes -ErrorAction SilentlyContinue
}
if (-not (Test-Path (Join-Path $classes 'db.properties'))) {
    Write-Host ''
    Write-Host '  [!] Chua co src/main/resources/db.properties' -ForegroundColor Yellow
    Write-Host '      Web van chay duoc, trang chu se hien huong dan cai dat.' -ForegroundColor Yellow
    Write-Host ''
}

Write-Host 'Compiling dev server ...'
& javac -encoding UTF-8 -cp $cp -d $toolsOut 'tools\DevServer.java'
if ($LASTEXITCODE -ne 0) { throw 'Compile tools/DevServer.java that bai.' }

# ---- 4. Run -----------------------------------------------------------------
& java -cp "$toolsOut;$cp" "-Dport=$Port" DevServer
