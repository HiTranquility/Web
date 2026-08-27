<%--
================================================================================
  case16.jsp — phần VIEW của case này
================================================================================

  Trang này chỉ cho bạn thấy NỘI DUNG đã được ghi. Muốn thấy chỗ nó
  thật sự đến thì phải nhìn vào TERMINAL đang chạy server — đó mới là console
  của Tomcat.

  Đây là điểm khác biệt quan trọng: log không hiện trên web, và đó là chủ ý.
================================================================================
--%>
<% request.setAttribute("caseNumber", "16");
   request.setAttribute("caseTitle", "Console and log-file debugging");
   request.setAttribute("caseSlides", "slides 52-58"); %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="problem">
    <strong>The problem</strong>
    You cannot step through a servlet the way you step through a <code>main</code>
    method &mdash; the server calls it, not you. So you print. The chapter gives two
    destinations, and they end up in different places.
</div>

<h3>The code from slides 52, 55 and 56</h3>
<pre>// to the servlet engine's console
<span class="hl">System.out.println("EmailListServlet email: " + email);</span>

// to the server's log file
<span class="hl">log("email=" + email);</span>

// to the log file, followed by the stack trace
try {
    UserIO.add(user, path);
} catch(IOException e) {
    <span class="hl">log("An IOException occurred.", e);</span>
}</pre>

<div class="result">
    <h3>Run it</h3>
    <form action="${pageContext.request.contextPath}/debug" method="get" class="stack">
        <p><label>email:</label><input type="text" name="email" value="${empty email ? 'jsmith@gmail.com' : email}"></p>
        <p><input type="submit" value="print to console and log"></p>
    </form>
    <p><a class="btn ghost" href="${pageContext.request.contextPath}/debug?email=${empty email ? 'jsmith@gmail.com' : email}&amp;throwIt=yes">
        now make it fail, and log the stack trace</a></p>

    <h3>What was just written</h3>
    <table>
        <tr><th>to the console<br><code>System.out.println</code></th>
            <td><code>${empty consoleLine ? "- submit the form" : consoleLine}</code></td></tr>
        <tr><th>to the log file<br><code>log(String)</code></th>
            <td><code>${empty logLine ? "- submit the form" : logLine}</code></td></tr>
        <tr><th>to the log file with a trace<br><code>log(String, Throwable)</code></th>
            <td>${empty stackTraceMessage
                  ? '<span class="null">nothing - use the second button</span>'
                  : stackTraceMessage}</td></tr>
    </table>
    <p style="color:#5b6b7c;font-size:.9em">
        <b>Go look at the terminal you started the server in.</b> That is the Tomcat
        console, and both the <code>System.out</code> line and the <code>log()</code>
        line are sitting there right now &mdash; the <code>log()</code> one prefixed
        with a timestamp and the servlet name.
    </p>
</div>

<h3>Where each one lands</h3>
<table>
    <tr><th style="width:11em"><code>System.out.println</code></th>
        <td>the console. Fast to write, gone when you close the terminal, and it says
            nothing about <em>who</em> printed it &mdash; which is why slide 53 insists
            you include the servlet name and the variable name yourself.</td></tr>
    <tr><th><code>log(message)</code></th>
        <td>the server's log file, via
            <code>ServletContext.log</code>. Timestamped and tagged with the servlet
            name automatically. On a real Tomcat that is
            <code>&lt;tomcat&gt;/logs/localhost.<em>yyyy-mm-dd</em>.log</code> (slide 57).</td></tr>
    <tr><th><code>log(message, t)</code></th>
        <td>the same, plus the full stack trace &mdash; the chain of method calls that
            led to the failure (slide 58). This is the one that actually tells you
            where a bug is.</td></tr>
</table>

<div class="note">
    <strong>Reading slide 56's trace.</strong> The book's example ends with
    <code>at murach.data.UserIO.add(UserIO.java:11)</code> and
    <code>at murach.email.EmailListServlet.doPost(EmailListServlet.java:38)</code>.
    Read a stack trace <em>bottom-up</em> to follow the call chain, but look
    <em>top-down</em> for the first line in your own package &mdash; that is almost
    always where the bug is. Here it says line 11 of your <code>UserIO</code>, and the
    cause on the first line: <code>(Access is denied)</code>.
</div>

<div class="warn">
    <strong>Don't ship the print statements.</strong> They cost time on every request,
    they clutter the log for everyone else, and one careless
    <code>System.out.println("password: " + pwd)</code> writes a credential to a file
    someone else can read. Real applications use a logging framework with levels, so
    debug output can be switched off without editing the code.
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
