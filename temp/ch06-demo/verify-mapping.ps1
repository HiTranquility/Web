# =============================================================================
#  verify-mapping.ps1
#  Kiem tra moi so dong ghi trong docs/chapter06-mapping.md co con dung khong.
#
#  CHAY LAI FILE NAY MOI KHI BAN SUA CODE — them/bot mot dong la moi so dong
#  phia sau deu lech, va bang tra trong file .md se sai ma khong ai biet.
#
#  Dung: powershell -ExecutionPolicy Bypass -File verify-mapping.ps1
# =============================================================================
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

$checks = @(
  @{f='src/main/java/murach/business/User.java';           n=32; want='class User implements Serializable'; slide='4'}
  @{f='src/main/java/murach/business/User.java';           n=45; want='public User()';                      slide='4'}
  @{f='src/main/java/murach/business/User.java';           n=52; want='public User(String firstName';       slide='4'}
  @{f='src/main/java/murach/business/User.java';           n=65; want='getFirstName';                       slide='5'}
  @{f='src/main/java/murach/business/User.java';           n=85; want='setEmail';                           slide='5'}
  @{f='src/main/java/murach/email/EmailListServlet.java';  n=81; want='GregorianCalendar currentDate';      slide='7'}
  @{f='src/main/java/murach/email/EmailListServlet.java';  n=83; want='setAttribute("currentYear"';         slide='7'}
  @{f='src/main/java/murach/email/EmailListServlet.java';  n=50; want='new User(firstName';                 slide='8'}
  @{f='src/main/java/murach/email/EmailListServlet.java';  n=75; want='setAttribute("user"';                slide='8'}
  @{f='src/main/webapp/index.jsp';                         n=2;  want='taglib';                            slide='11'}
  @{f='src/main/webapp/index.jsp';                         n=26; want='c:if';                              slide='12'}
  @{f='src/main/webapp/index.jsp';                         n=18; want='c:import';                           slide='32'}
  @{f='src/main/webapp/index.jsp';                         n=50; want='c:import';                           slide='32'}
  @{f='src/main/webapp/index.jsp';                         n=37; want='${user.email}';                      slide='21'}
  @{f='src/main/webapp/thanks.jsp';                        n=31; want='c:import';                           slide='32'}
  @{f='src/main/webapp/thanks.jsp';                        n=40; want='${user.email}';                      slide='21'}
  @{f='src/main/webapp/thanks.jsp';                        n=56; want='form action';                        slide='30'}
  @{f='src/main/webapp/WEB-INF/includes/footer.jsp';       n=20; want='<%= currentYear %>';                 slide='15'}
  @{f='src/main/webapp/error_404.jsp';                     n=27; want='<%@ include';                        slide='31'}
  @{f='src/main/webapp/error_403.jsp';                     n=25; want='<%@ include';                        slide='31'}
  @{f='src/main/webapp/error_500.jsp';                     n=26; want='<%@ include';                        slide='31'}
  @{f='src/main/webapp/temp/loi_el.jsp';                   n=29; want='new User(';                          slide='34'}
  @{f='src/main/webapp/temp/loi_el.jsp';                   n=49; want='user.emailAddress';                  slide='34'}
  @{f='src/main/webapp/WEB-INF/web.xml';                   n=19; want='404';                                slide='35'}
  @{f='src/main/webapp/WEB-INF/web.xml';                   n=25; want='403';                                slide='-'}
  @{f='src/main/webapp/WEB-INF/web.xml';                   n=35; want='Throwable';                          slide='35'}
  @{f='src/main/webapp/WEB-INF/web.xml';                   n=42; want='500';                                slide='35'}
  @{f='src/main/java/murach/email/ErrorTestServlet.java';  n=27; want='@WebServlet';                        slide='-'}
  @{f='src/main/java/murach/email/ErrorTestServlet.java';  n=52; want='SC_FORBIDDEN';                       slide='-'}
  @{f='src/main/java/murach/email/ErrorTestServlet.java';  n=65; want='throw new';                          slide='-'}
)

$bad = 0
foreach ($c in $checks) {
    $lines = Get-Content -LiteralPath $c.f -Encoding UTF8
    $got = if ($c.n -le $lines.Count) { $lines[$c.n - 1] } else { '<qua cuoi file>' }
    if ($got -notlike "*$($c.want)*") {
        $bad++
        Write-Host ("  SAI  slide {0}  {1}:{2}" -f $c.slide, $c.f, $c.n) -ForegroundColor Red
        Write-Host ("       can  : {0}" -f $c.want)
        Write-Host ("       thuc : {0}" -f $got.Trim())
    }
}

Write-Host ''
if ($bad -eq 0) {
    Write-Host ("  OK - {0}/{0} so dong trong chapter06-mapping.md van dung." -f $checks.Count) -ForegroundColor Green
} else {
    Write-Host ("  {0}/{1} dong bi lech. Sua lai bang trong docs/chapter06-mapping.md." -f $bad, $checks.Count) -ForegroundColor Yellow
    exit 1
}
