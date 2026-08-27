# Chapter 2 — Email List application (MVC)

The application from *Murach's Java Servlets and JSP* (3rd Ed.), chapter 2, laid out
as a standard Java web application. See [../docs/chapter02-notes.md](../docs/chapter02-notes.md)
for the slide content this was built from.

## Layout

```
ch02email/
├── pom.xml                                  Maven WAR project (Servlet 3.1 / Tomcat 9)
├── run.ps1                                  build + run on an embedded Tomcat
├── build.ps1                                package build/ch02email.war
├── tools/DevServer.java                     the embedded Tomcat launcher (dev only)
└── src/main/
    ├── java/murach/
    │   ├── business/User.java               model  — the JavaBean
    │   ├── data/UserDB.java                 data access layer
    │   └── email/EmailListServlet.java      controller
    └── webapp/
        ├── index.html                       view — the form
        ├── thanks.jsp                       view — the confirmation page
        ├── styles/main.css
        └── WEB-INF/web.xml                  deployment descriptor
```

Each MVC layer sits in its own package, exactly as the chapter describes: the servlet
never touches the data store directly, and the JSP never touches Java code — it only
reads the `user` request attribute through EL (`${user.email}`).

## Running it

### Option 1 — one command, no server to install

```bash
powershell -ExecutionPolicy Bypass -File run.ps1
```

Downloads Tomcat 9's embedded jars into `.libs/` on first run (~5 MB, from Maven
Central), compiles the classes, and serves the app at:

<http://localhost:8080/ch02email/>

Use `-Port 9090` if 8080 is taken. `src/main/webapp` is served in place, so edits to
the HTML, JSP and CSS appear on refresh; changing a `.java` file needs a restart.

### Option 2 — IntelliJ / NetBeans / Eclipse

Open `pom.xml` as a project, attach a Tomcat 9 run configuration, and deploy the
`ch02email` artifact. `javax.servlet-api` is scoped `provided`, so the container
supplies it.

### Option 3 — a real Tomcat 9 install

```bash
powershell -ExecutionPolicy Bypass -File build.ps1
```

Then copy `build/ch02email.war` into `<tomcat>/webapps/`.

> Tomcat **9**, not 10+. This chapter uses the `javax.servlet` packages; Tomcat 10
> renamed them to `jakarta.servlet` and will not run this code as written.

## Where the data goes

Chapter 2 comes before any database chapter, so `UserDB` writes tab-delimited lines to
a text file:

```
%USERPROFILE%\murach\EmailList.txt
```

Override with `-Dmurach.emaillist.file=C:\path\to\EmailList.txt`. Later chapters swap
the body of `UserDB.insert()` for JDBC or JPA — nothing else in the app changes, which
is the point of keeping the data access layer separate.

## Three deliberate differences from the slides

1. **`UserDB` is not shown in the slides.** They only call `UserDB.insert(user)`, so
   the file-based implementation here is mine.
2. **The Return button on `thanks.jsp`.** The slides show `<form action="">`, which
   re-submits to `thanks.jsp` itself and renders an empty page. It is `action="emailList"`
   here so the `action=join` parameter reaches the controller and you get the form back.
3. **UTF-8.** The book's code leaves the JSP on the servlet default of ISO-8859-1, so a
   name like `Đinh Thị` comes back as `?inh Th?`. `thanks.jsp` now carries a
   `contentType="text/html; charset=UTF-8"` page directive and the servlet calls
   `request.setCharacterEncoding("UTF-8")`. Drop both if you want the code byte-identical
   to the slides.
