<%--
================================================================================
  case10.jsp — phần VIEW của case này
================================================================================

  ${param.via} đọc THAM SỐ trên URL, khác với ${via} đọc ATTRIBUTE.
  Ở đây phải dùng param vì redirect làm mất hết attribute — thông tin duy nhất
  sống sót là cái nằm trên URL mà servlet gắn vào lúc sendRedirect.

  param là object ngầm định của EL, tương đương request.getParameter().
================================================================================
--%>
<% request.setAttribute("caseNumber", "10");
   request.setAttribute("caseTitle", "Redirecting a response");
   request.setAttribute("caseSlides", "slides 25-26"); %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="problem">
    <strong>The problem</strong>
    A forward never leaves the server, so it cannot send the user to another site &mdash;
    and it leaves a URL in the address bar that no longer matches the page. A redirect
    sends <b>302</b> back to the browser, and the browser fetches the new URL itself.
</div>

<h3>The code from slide 26</h3>
<pre>// relative to the current directory
<span class="hl">response.sendRedirect("email");</span>

// relative to the servlet engine
response.sendRedirect("/musicStore/email/");

// to a different web server
response.sendRedirect("http://www.murach.com/email/");</pre>

<div class="result">
    <h3>Run it</h3>
    <p>
        <a class="btn" href="${pageContext.request.contextPath}/redirect?target=relative">relative to the current directory</a>
        <a class="btn ghost" href="${pageContext.request.contextPath}/redirect?target=contextRelative">relative to the servlet engine</a>
    </p>
    <p style="font-size:.9em;color:#5b6b7c">A third button would send you to
       murach.com and off this app entirely &mdash; the servlet supports
       <code>?target=external</code> if you want to see it.</p>

    <table>
        <tr><th>arrived via</th>
            <td><code>${empty param.via ? "you opened this page directly" : param.via}</code></td></tr>
        <tr><th>the URL you see up top</th>
            <td><code>${pageContext.request.requestURI}</code>
                &mdash; the real page, not <code>/redirect</code></td></tr>
        <tr><th>the attribute the servlet set</th>
            <td>${empty setBy ? '<span class="null">gone - a redirect starts a brand new request</span>' : setBy}</td></tr>
    </table>
</div>

<h3>forward or sendRedirect?</h3>
<table>
    <tr><th style="width:12em"></th><th style="width:auto">forward</th><th>sendRedirect</th></tr>
    <tr><th>round trips</th><td>one</td><td>two &mdash; 302, then the browser asks again</td></tr>
    <tr><th>address bar</th><td>unchanged</td><td>changes to the target</td></tr>
    <tr><th>request attributes</th><td>kept</td><td>lost</td></tr>
    <tr><th>can leave the app</th><td>no</td><td>yes, any URL at all</td></tr>
    <tr><th>use it for</th>
        <td>handing data to a JSP inside your own app &mdash; the MVC default</td>
        <td>after a POST that wrote data, so refresh can't resubmit; and for external URLs</td></tr>
</table>

<div class="note">
    <strong>Post/Redirect/Get.</strong> The second row of that table is the reason the
    pattern exists. Finish a POST with a redirect and the browser's final request is a
    harmless GET &mdash; so F5 re-runs the GET, not the order.
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
