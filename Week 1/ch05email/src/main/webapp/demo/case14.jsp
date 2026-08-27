<%--
================================================================================
  case14.jsp — phần VIEW của case này
================================================================================

  ${eventLog} là MỘT chuỗi đã nối sẵn bằng String.join trong servlet,
  không phải List. Cố ý làm vậy để in được bằng <pre> mà không cần <c:forEach>
  của JSTL (chương 9 mới học).

  Xuống dòng hiển thị đúng là nhờ thẻ <pre> giữ nguyên ký tự 
.
================================================================================
--%>
<% request.setAttribute("caseNumber", "14");
   request.setAttribute("caseTitle", "The servlet lifecycle");
   request.setAttribute("caseSlides", "slides 46-47"); %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="problem">
    <strong>The problem</strong>
    Five methods, and no obvious way to see which runs when. The order matters: put
    setup in the wrong one and it runs on every single request instead of once.
</div>

<h3>The five methods, slide 46</h3>
<pre>public void init() throws ServletException

public void <span class="hl">service</span>(HttpServletRequest request,
                    HttpServletResponse response)

public void doGet(HttpServletRequest request,
                  HttpServletResponse response)

public void doPost(HttpServletRequest request,
                   HttpServletResponse response)

public void destroy()</pre>

<div class="result">
    <h3>What actually happened, in order</h3>
    <p>Every call is timestamped as it happens. Reload a few times and watch which
       lines repeat and which one never does.</p>
    <pre>${eventLog}</pre>
    <p>
        <a class="btn" href="${pageContext.request.contextPath}/lifecycle">reload (GET)</a>
        <form action="${pageContext.request.contextPath}/lifecycle" method="post" style="display:inline">
            <input type="submit" value="send a POST">
        </form>
    </p>
    <table>
        <tr><th>this servlet instance</th>
            <td><code>LifecycleServlet@${instanceId}</code> &mdash; the same hex value on
                every reload, because there is only ever one instance</td></tr>
    </table>
</div>

<div class="note">
    <strong>What the log proves.</strong>
    <code>init()</code> appears exactly once, at the top, no matter how many times you
    reload &mdash; Tomcat creates the servlet on the first request and keeps it.
    <code>service()</code> runs before every <code>doGet</code>/<code>doPost</code>;
    it is the dispatcher that reads the HTTP method and picks the right one.
    <code>destroy()</code> will not appear until you stop the server.
</div>

<div class="warn">
    <strong>Slide 47's warning.</strong> It is bad practice to override
    <code>service</code> &mdash; do it and you take over dispatching for every HTTP
    method, including HEAD and OPTIONS, which <code>HttpServlet</code> was handling
    correctly for free. This demo overrides it only to make the sequence visible, and
    still calls <code>super.service(...)</code> so the normal dispatch continues.
    In real code, override <code>doGet</code> or <code>doPost</code>.
</div>

<div class="note">
    <strong>Why init() matters.</strong> One instance, created once, means
    <code>init()</code> is where expensive setup belongs &mdash; a database connection
    pool, a config file read. Put it in <code>doGet</code> and you pay for it on every
    request. Note the corollary, which is
    <a href="${pageContext.request.contextPath}/counter">case 15</a>: one instance
    shared by every thread is also why instance variables are dangerous.
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
