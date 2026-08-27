<%--
================================================================================
  case09.jsp — phần VIEW của case này
================================================================================

  Hai dòng cuối bảng cố tình để cạnh nhau vì chúng KHÁC NHAU:
    originalURI                    -> /ch05email/forward     (servlet lưu lại)
    pageContext.request.requestURI -> /ch05email/demo/case09.jsp

  Sau khi forward, bên trong trang đích getRequestURI() trả về đường dẫn của
  ĐÍCH, vì Tomcat đã ghi đè. Thanh địa chỉ thì vẫn giữ URL gốc. Không biết chỗ
  này rất dễ kết luận sai là "forward có đổi URL".
================================================================================
--%>
<% request.setAttribute("caseNumber", "09");
   request.setAttribute("caseTitle", "Forwarding a request");
   request.setAttribute("caseSlides", "slides 23-24"); %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="problem">
    <strong>The problem</strong>
    The servlet decided <em>what</em> to show; a JSP knows <em>how</em> to show it.
    <code>forward</code> hands the same request and response objects to that JSP,
    entirely inside the server. The browser never finds out.
</div>

<h3>The code from slide 24</h3>
<pre>// forward to an HTML page
String url = "/index.html";
<span class="hl">getServletContext().getRequestDispatcher(url)
    .forward(request, response);</span>

// forward to a JSP
String url = "/thanks.jsp";
getServletContext().getRequestDispatcher(url)
    .forward(request, response);

// forward to another servlet
String url = "/cart/displayInvoice";
getServletContext().getRequestDispatcher(url)
    .forward(request, response);</pre>

<div class="result">
    <h3>Run it &mdash; all three targets</h3>
    <p>
        <a class="btn" href="${pageContext.request.contextPath}/forward?target=jsp">forward to a JSP</a>
        <a class="btn ghost" href="${pageContext.request.contextPath}/forward?target=html">forward to an HTML page</a>
        <a class="btn ghost" href="${pageContext.request.contextPath}/forward?target=servlet">forward to another servlet</a>
    </p>

    <h3>Two things to check in the address bar</h3>
    <table>
        <tr><th>the attribute set before forwarding</th>
            <td>${empty setBy ? '<span class="null">nothing - you opened this page directly</span>' : setBy}</td></tr>
        <tr><th>forwarded to</th>
            <td><code>${empty forwardedTo ? "-" : forwardedTo}</code></td></tr>
        <tr><th>the URL the browser asked for</th>
            <td><code>${empty originalURI ? "-" : originalURI}</code>
                &mdash; and it is still what the address bar shows</td></tr>
        <tr><th>what this JSP sees as<br><code>request.getRequestURI()</code></th>
            <td><code>${pageContext.request.requestURI}</code>
                &mdash; the target, because the forward rewrote it internally</td></tr>
    </table>
</div>

<div class="note">
    <strong>Those last two rows are not a contradiction.</strong> The browser never
    learns about the forward, so the address bar keeps showing <code>/forward</code>.
    But inside the server the request really was re-targeted, so the JSP's own
    <code>getRequestURI()</code> reports the JSP. If you need the original from inside
    the target, Tomcat keeps it in the
    <code>javax.servlet.forward.request_uri</code> attribute &mdash; or you save it
    yourself before forwarding, which is what this servlet does.
</div>

<div class="note">
    <strong>The attribute survived.</strong> That is the difference that matters. A
    forward reuses the request object, so everything the servlet put in it is still
    there when the JSP renders &mdash; which is the whole mechanism behind
    <a href="${pageContext.request.contextPath}/attributes">case 08</a> and behind MVC
    in general. Compare with
    <a href="${pageContext.request.contextPath}/demo/case10.jsp">case 10</a>, where a
    redirect throws it away.
</div>

<div class="warn">
    <strong>Forward once.</strong> Once the response is committed you cannot forward
    again &mdash; a second <code>forward</code> throws
    <code>IllegalStateException</code>. In practice: work out the URL first, then
    forward exactly once at the end of the method, which is how every servlet in this
    chapter is written.
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
