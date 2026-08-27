<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Chapter 5 - How to develop servlets</title>
    <link rel="stylesheet" href="styles/main.css" type="text/css"/>
</head>
<body>
<div class="wrap">
    <h1>Chapter 5 &mdash; How to develop servlets</h1>
    <p style="color:#5b6b7c;margin-top:0">
        Every code listing in the 58 slides, turned into one runnable page each.
        Each page states the problem, shows the code from the slide, and then
        actually runs it so you can see the answer instead of reading about it.
    </p>

    <ol class="cases">
        <li><a href="demo/case01.jsp"><span class="num">01</span><span class="title">A servlet that returns HTML</span><span class="what">PrintWriter, setContentType, doGet calling doPost</span></a></li>
        <li><a href="demo/case02.jsp"><span class="num">02</span><span class="title">Servlet mapping &amp; URL patterns</span><span class="what">web.xml, /email/* versus an exact pattern</span></a></li>
        <li><a href="annotation"><span class="num">03</span><span class="title">The @WebServlet annotation</span><span class="what">mapping without touching web.xml</span></a></li>
        <li><a href="demo/case04.jsp"><span class="num">04</span><span class="title">The HTTP GET method</span><span class="what">three ways to append parameters to a URL</span></a></li>
        <li><a href="demo/case05.jsp"><span class="num">05</span><span class="title">GET versus POST</span><span class="what">where the parameters travel, and when to use which</span></a></li>
        <li><a href="demo/case06.jsp"><span class="num">06</span><span class="title">getParameter / getParameterValues</span><span class="what">text box, check box, multi-select list box</span></a></li>
        <li><a href="realPath"><span class="num">07</span><span class="title">ServletContext.getRealPath</span><span class="what">turning /WEB-INF/file.txt into a disk path</span></a></li>
        <li><a href="attributes"><span class="num">08</span><span class="title">Request attributes</span><span class="what">setAttribute, getAttribute, and the cast</span></a></li>
        <li><a href="demo/case09.jsp"><span class="num">09</span><span class="title">Forwarding a request</span><span class="what">RequestDispatcher.forward to HTML, JSP, servlet</span></a></li>
        <li><a href="demo/case10.jsp"><span class="num">10</span><span class="title">Redirecting a response</span><span class="what">sendRedirect, and how it differs from forward</span></a></li>
        <li><a href="join.jsp"><span class="num">11</span><span class="title">Server-side data validation</span><span class="what">the chapter's email-list app, with a &#36;{message}</span></a></li>
        <li><a href="initParams"><span class="num">12</span><span class="title">Initialization parameters</span><span class="what">context-param versus init-param</span></a></li>
        <li><a href="demo/case13.jsp"><span class="num">13</span><span class="title">Custom error handling</span><span class="what">error-page for a 404 and for an exception</span></a></li>
        <li><a href="lifecycle"><span class="num">14</span><span class="title">The servlet lifecycle</span><span class="what">init, service, doGet, destroy - in order</span></a></li>
        <li><a href="counter"><span class="num">15</span><span class="title">Instance variables aren't thread-safe</span><span class="what">a stress test that really loses updates</span></a></li>
        <li><a href="debug"><span class="num">16</span><span class="title">Console and log-file debugging</span><span class="what">System.out.println, log(), log() with a stack trace</span></a></li>
    </ol>

    <div class="note">
        <strong>Slide 51 &mdash; common servlet problems.</strong>
        Won't compile? The servlet API jar isn't on the classpath.
        Won't run? Wrong URL, or the server isn't up.
        Changes not showing? Redeploy or restart &mdash; editing a
        <code>.java</code> file needs a restart, editing a <code>.jsp</code> does not.
    </div>
</div>
</body>
</html>
