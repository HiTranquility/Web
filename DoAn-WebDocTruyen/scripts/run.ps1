# =============================================================================
#  Build + chay Web Doc Truyen tren embedded Tomcat 9.
#  Usage:  .\run.ps1            -> http://localhost:8080/
#          .\run.ps1 -Port 9090
# =============================================================================
param([int]$Port = 8080)

$ErrorActionPreference = 'Stop'
# Script nam trong scripts/ -> lui ve thu muc goc du an
$ProjectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $ProjectRoot

$libs     = Join-Path $ProjectRoot '.libs'
$build    = Join-Path $ProjectRoot 'build'
$classes  = Join-Path $build 'classes'
$toolsOut = Join-Path $build 'tools'
$repo     = 'https://repo1.maven.org/maven2'

New-Item -ItemType Directory -Force -Path $libs, $classes, $toolsOut | Out-Null

# ---- 1. Dependencies --------------------------------------------------------
#  GHIM PHIÊN BẢN, KHÔNG LẤY "MỚI NHẤT".
#    Trước đây script hỏi Maven bản Tomcat 9 mới nhất ở MỖI lần chạy. Hậu quả:
#    không có mạng là không chạy được; mỗi lần Tomcat ra bản mới lại tải thêm
#    một bộ jar, và .libs giữ CẢ HAI bản cùng lúc trên classpath (đã gặp:
#    9.0.121 và 9.0.122) — lớp nào được nạp trước là chuyện may rủi.
#    Bản 9.0.106+ có giới hạn maxPartCount — xem tools/DevServer.java.
#  Muốn nâng Tomcat: sửa đúng một dòng dưới đây.
$tomcatVersion = '9.0.122'
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
    @{ path = "com/mysql/mysql-connector-j/8.4.0";                          file = "mysql-connector-j-8.4.0.jar" },
    # Connection Pool HikariCP & SLF4J
    @{ path = "com/zaxxer/HikariCP/4.0.3";                                 file = "HikariCP-4.0.3.jar" },
    @{ path = "org/slf4j/slf4j-api/1.7.36";                                file = "slf4j-api-1.7.36.jar" },
    @{ path = "org/slf4j/slf4j-simple/1.7.36";                             file = "slf4j-simple-1.7.36.jar" },
    # JavaMail / SMTP (ISSUE-002)
    @{ path = "com/sun/mail/javax.mail/1.6.2";                             file = "javax.mail-1.6.2.jar" },
    @{ path = "com/sun/activation/javax.activation/1.2.0";                 file = "javax.activation-1.2.0.jar" }
)
foreach ($jar in $jars) {
    $dest = Join-Path $libs $jar.file
    if (-not (Test-Path $dest)) {
        Write-Host "Downloading $($jar.file) ..."
        Invoke-WebRequest -Uri "$repo/$($jar.path)/$($jar.file)" -OutFile $dest -UseBasicParsing
    }
}

# Dọn jar Tomcat của phiên bản KHÁC còn sót trong .libs (từ thời còn lấy
# "mới nhất") — hai bản cùng nằm trên classpath là nạp lớp may rủi.
Get-ChildItem $libs -Filter 'tomcat-*.jar' |
    Where-Object { $_.Name -notlike "*-$tomcatVersion.jar" } |
    ForEach-Object { Write-Host "Removing stale $($_.Name)"; Remove-Item $_.FullName }

$cp = (Get-ChildItem $libs -Filter *.jar | ForEach-Object { $_.FullName }) -join ';'

# ---- 2. Compile -------------------------------------------------------------
Write-Host 'Compiling application classes ...'
Remove-Item -Recurse -Force "$classes\*" -ErrorAction SilentlyContinue
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
    Write-Host '  [!] Chua co db.properties — web chay nhung KHONG CO DU LIEU.' -ForegroundColor Yellow
    Write-Host '      Chay truoc:  powershell -File scripts\setup-db.ps1' -ForegroundColor Yellow
    Write-Host ''
}

Write-Host 'Compiling dev server ...'
& javac -encoding UTF-8 -cp $cp -d $toolsOut 'tools\DevServer.java'
if ($LASTEXITCODE -ne 0) { throw 'Compile tools/DevServer.java that bai.' }

# ---- 4. Run -----------------------------------------------------------------
& java -cp "$toolsOut;$cp" "-Dport=$Port" DevServer
