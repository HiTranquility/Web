<%--
================================================================================
  case12.jsp — phần VIEW của case này
================================================================================

  Trang in ra cả trường hợp ĐỌC ĐÚNG lẫn ĐỌC SAI phạm vi, để bạn
  thấy tận mắt cái null mà trình biên dịch không hề cảnh báo.

  Khối <pre> chứa ví dụ annotation có ghi @WebInitParam — đúng tên thật, khác
  với @InitParam mà slide 37 in thiếu.
================================================================================
--%>
<% request.setAttribute("caseNumber", "12");
   request.setAttribute("caseTitle", "Initialization parameters");
   request.setAttribute("caseSlides", "slides 35-40"); %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="problem">
    <strong>The problem</strong>
    A customer-service address, a file path, an API key &mdash; values that change
    between the lab machine and production. Hard-code them and every change means a
    recompile. Put them in <code>web.xml</code> and you edit one file and redeploy.
</div>

<h3>The code from slide 35</h3>
<pre>&lt;!-- available to EVERY servlet in the application --&gt;
<span class="hl">&lt;context-param&gt;</span>
    &lt;param-name&gt;custServEmail&lt;/param-name&gt;
    &lt;param-value&gt;custserv@murach.com&lt;/param-value&gt;
&lt;/context-param&gt;

&lt;servlet&gt;
    &lt;servlet-name&gt;EmailListServlet&lt;/servlet-name&gt;
    &lt;servlet-class&gt;murach.email.EmailListServlet&lt;/servlet-class&gt;
    &lt;!-- available to THIS servlet only --&gt;
    <span class="hl">&lt;init-param&gt;</span>
        &lt;param-name&gt;relativePathToFile&lt;/param-name&gt;
        &lt;param-value&gt;/WEB-INF/EmailList.txt&lt;/param-value&gt;
    &lt;/init-param&gt;
&lt;/servlet&gt;</pre>

<h3>Reading them, from slide 40</h3>
<pre>// available to all servlets
String custServEmail = this.<span class="hl">getServletContext()</span>
                           .getInitParameter("custServEmail");

// available to the current servlet only
String relativePath = this.<span class="hl">getServletConfig()</span>
                          .getInitParameter("relativePathToFile");</pre>

<div class="result">
    <h3>What this servlet read</h3>
    <table>
        <tr><th>getServletContext()<br>.getInitParameter("custServEmail")</th>
            <td><code>${empty custServEmail ? "null" : custServEmail}</code></td></tr>
        <tr><th>getServletConfig()<br>.getInitParameter("relativePathToFile")</th>
            <td><code>${empty relativePath ? "null" : relativePath}</code></td></tr>
        <tr><th>after getRealPath()</th>
            <td><code>${empty resolvedPath ? "-" : resolvedPath}</code></td></tr>
        <tr><th>the same name from the <em>wrong</em> scope<br>
                <code>getServletContext().getInitParameter("relativePathToFile")</code></th>
            <td><span class="${wrongScopeIsNull ? 'yes' : 'no'}">${wrongScopeIsNull ? "null" : "found it?!"}</span>
                &mdash; a servlet init-param is invisible to the context</td></tr>
    </table>
</div>

<div class="note">
    <strong>Two objects, one method name.</strong> That last row is the trap.
    <code>getInitParameter</code> exists on both <code>ServletContext</code> and
    <code>ServletConfig</code> with the same signature, so calling it on the wrong one
    compiles perfectly and returns <code>null</code> at runtime. Context =
    <code>&lt;context-param&gt;</code>, config = <code>&lt;init-param&gt;</code>.
</div>

<div class="note">
    <strong>The annotation form, slide 37.</strong>
    <pre style="margin:.6em 0 0">@WebServlet(urlPatterns={"/emailList"},
            initParams={@WebInitParam(name="relativePathToFile",
                                      value="/WEB-INF/EmailList.txt")})</pre>
    Slide 37 abbreviates it to <code>@InitParam</code>; the real annotation is
    <code>@WebInitParam</code>. This is why <code>InitParamServlet</code> and
    <code>EmailListServlet</code> are the two servlets in this app still mapped the
    old way &mdash; their parameters live in <code>web.xml</code>, so their mappings
    may as well too.
</div>

<div class="warn">
    <strong>Slide 34.</strong> Tomcat reads <code>web.xml</code> once, at startup. Edit
    it and nothing happens until you redeploy or restart. And if the elements are
    nested wrongly, the application won't start at all &mdash; Tomcat reports the error
    instead of the app.
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
