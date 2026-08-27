<%--
================================================================================
  case05.jsp — phần VIEW của case này
================================================================================

  Hai form giống hệt nhau, chỉ khác method="get" và method="post",
  cùng trỏ vào /postDemo. Bấm lần lượt rồi nhìn thanh địa chỉ là thấy ngay
  khác biệt mà slide 15 mô tả.
================================================================================
--%>
<% request.setAttribute("caseNumber", "05");
   request.setAttribute("caseTitle", "GET versus POST");
   request.setAttribute("caseSlides", "slides 14-15"); %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="problem">
    <strong>The problem</strong>
    Both methods send the same parameters to the same servlet. The difference is
    <em>where</em> they travel &mdash; and that decides whether they are visible,
    bookmarkable, repeatable, and size-limited.
</div>

<h3>The code from the slides</h3>
<pre>&lt;form action="emailList" <span class="hl">method="post"</span>&gt;</pre>

<div class="result">
    <h3>Run it &mdash; the same form, submitted both ways</h3>
    <p>Type something you would not want in a URL, then send it each way and compare
       the address bar.</p>

    <form action="${pageContext.request.contextPath}/postDemo" method="get" class="stack">
        <p><label>secret:</label><input type="text" name="secret" value="my-password-123"></p>
        <input type="submit" value="Send with GET">
    </form>
    <form action="${pageContext.request.contextPath}/postDemo" method="post" class="stack">
        <p><label>secret:</label><input type="text" name="secret" value="my-password-123"></p>
        <input type="submit" value="Send with POST">
    </form>

    <h3>What the servlet received</h3>
    <table>
        <tr><th>getMethod()</th><td><code>${empty method ? "-" : method}</code></td></tr>
        <tr><th>getParameter("secret")</th><td><code>${empty secret ? "null" : secret}</code></td></tr>
        <tr><th>getQueryString()</th>
            <td><code>${empty queryString ? "null" : queryString}</code>
                &mdash; ${empty queryString ? "a POST puts nothing in the URL" : "a GET puts everything in the URL"}</td></tr>
        <tr><th>getContentType()</th><td><code>${empty contentType ? "null - a GET has no body" : contentType}</code></td></tr>
    </table>
    <p style="color:#5b6b7c;font-size:.9em">Both submissions carry the same value.
       Only the GET leaks it into the address bar, the browser history, and the
       server's access log.</p>
</div>

<h3>Slide 15's rules</h3>
<table>
    <tr><th style="width:9em">Use GET when</th>
        <td>the request only <b>reads</b> data from the server, and running it twice
            changes nothing. Safe to bookmark, safe to refresh, safe for the browser
            to prefetch.</td></tr>
    <tr><th>Use POST when</th>
        <td>the request <b>writes</b> data; running it twice would cause problems
            (a double order); you don't want parameters in the URL for security
            reasons; you don't want users bookmarking a page with its parameters;
            or you need to transfer more than 4&nbsp;KB.</td></tr>
</table>

<div class="note">
    <strong>The refresh test.</strong> Send the form with POST, then press F5. The
    browser asks whether to resubmit &mdash; that warning <em>is</em> the "executing
    this request twice may cause problems" rule, enforced by the browser. Do the same
    after a GET and it silently repeats, no question asked.
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
