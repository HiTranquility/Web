# Chapter 5 — How to develop servlets

Text extracted from `Chapter 05 slides.pptx` (Murach's Java Servlets/JSP, 3rd Ed.).
All 58 slides are pictures, so the content below comes from the vector text inside
the slide images.

Every code listing has a runnable page in [`../ch05email/`](../ch05email/).

## Objectives

**Applied**

1. Code and test servlets that require the features presented in this chapter.
2. Use the web.xml file or an annotation to map a servlet to a URL pattern.
3. Provide for server-side data validation in your applications.
4. Use the web.xml file to set initialization parameters. Use servlets to get them.
5. Use the web.xml file to implement custom error handling.
6. Write debugging data for a servlet to either the console or a log file.

**Knowledge**

1. Describe servlets and servlet mapping, and their use of request and response objects.
2. Describe how parameters are passed to a servlet with the HTTP GET method.
3. List three reasons for using POST instead of GET.
4. Describe how the ServletContext object is used to get the path for a file. Describe
   the use of the init, doGet, doPost, and destroy methods in a servlet.
5. Explain why you should never use instance variables in servlets.
6. Describe the use of debugging data written to the console or log file.

## The 16 code cases

| # | Case | Slides | Runnable at |
|---|------|--------|-------------|
| 01 | A servlet that returns HTML | 4–6 | `/demo/case01.jsp` → `/test` |
| 02 | Servlet mapping and URL patterns | 7–9 | `/demo/case02.jsp` → `/mapping`, `/email/*` |
| 03 | The `@WebServlet` annotation | 10–11 | `/annotation`, `/anno/*` |
| 04 | The HTTP GET method | 12–13 | `/demo/case04.jsp` → `/getDemo` |
| 05 | GET versus POST | 14–15 | `/demo/case05.jsp` → `/postDemo` |
| 06 | `getParameter` / `getParameterValues` | 16–17 | `/demo/case06.jsp` → `/controls` |
| 07 | `ServletContext.getRealPath` | 18–20 | `/realPath` |
| 08 | Request attributes | 21–22 | `/attributes` |
| 09 | Forwarding a request | 23–24 | `/demo/case09.jsp` → `/forward` |
| 10 | Redirecting a response | 25–26 | `/demo/case10.jsp` → `/redirect` |
| 11 | Server-side data validation | 27–31 | `/join.jsp` → `/emailList` |
| 12 | Initialization parameters | 32–40 | `/initParams` |
| 13 | Custom error handling | 41–45 | `/demo/case13.jsp` → `/errorDemo` |
| 14 | The servlet lifecycle | 46–47 | `/lifecycle` |
| 15 | Instance variables aren't thread-safe | 48–50 | `/counter` |
| 16 | Console and log-file debugging | 52–58 | `/debug` |

Slide 51 (common servlet problems) has no code; it appears as a note on the case index.

## Concepts, by slide

**Servlet basics (slide 6).** `doGet` processes all HTTP GET requests, `doPost` all
POST requests. Both accept the `HttpServletRequest` (the request object) and the
`HttpServletResponse` (the response object).

**URL patterns (slide 9).** `/emailList` matches that one URL. `/email/*` matches any
URL in the `email` directory. `/email/add` matches only that URL.

**GET versus POST (slide 15).** Use GET when the request reads data and can be executed
repeatedly without causing problems. Use POST when the request writes data, when
repeating it may cause problems, when you don't want the parameters in the URL for
security reasons, when you don't want users bookmarking a page with its parameters, or
when you need to transfer more than 4 KB.

**Parameters (slide 16).** `getParameter(String)` returns one value as a String, or
`null`. `getParameterValues(String)` returns a `String[]` of all values, or `null`.

**ServletContext (slide 20).** Every servlet inherits `GenericServlet`, so
`getServletContext()` is always available. Use the context to read global
initialization parameters, work with global variables, and write to log files.

**Attributes (slide 21).** `setAttribute(String, Object)` stores any object in the
request under a name. `getAttribute(String)` returns it as `Object`, or `null`.
**Attributes reset between requests.**

**Initialization parameters (slide 38).** A `<context-param>` is available to all
servlets; read it from `getServletContext()`. An `<init-param>` inside a `<servlet>`
element is available to that servlet only; read it from `getServletConfig()`.

**The web.xml file (slide 34).** It lives in `WEB-INF`. Tomcat reads it at startup.
Incorrectly nested elements produce an error at that point. After modifying it,
redeploy the application or restart Tomcat.

**Instance variables (slide 50).** An instance variable belongs to the one instance of
the servlet and is shared by every thread that requests it. Instance variables are not
thread-safe: two threads can conflict when reading, modifying and updating the same
variable at once, causing lost updates.

**Debugging (slides 53, 58).** `System.out.println` writes to the servlet engine's
console; include the servlet name and variable name so the messages are readable.
`log(message)` and `log(message, throwable)` write to the server's log file — the
stack trace is the chain of method calls leading to the failure. A typical Tomcat log
is `<tomcat>/logs/localhost.yyyy-mm-dd.log` (slide 57).

**Common servlet problems (slide 51).** Won't compile → the compiler can't see the JAR
files for the required APIs. Won't run → the server isn't running, or the URL is wrong.
Changes not showing up → redeploy, restart, or turn on servlet reloading. Page displays
wrongly → view the generated HTML source and work backwards to the servlet.
