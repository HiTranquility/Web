<%--
================================================================================
  case13.jsp — phần VIEW của case này
================================================================================

  Các nút trên trang này CỐ Ý làm sập servlet. Mỗi nút ném một loại
  exception khác nhau, cả ba cùng rơi vào một khai báo <error-page> duy nhất
  trong web.xml.

  Trang đích là error_java.jsp — mở file đó ra xem cách đọc pageContext.exception.
================================================================================
--%>
<% request.setAttribute("caseNumber", "13");
   request.setAttribute("caseTitle", "Custom error handling");
   request.setAttribute("caseSlides", "slides 41-45"); %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="problem">
    <strong>The problem</strong>
    Left alone, a bad URL gives the user Tomcat's 404 page and an uncaught exception
    gives them a stack trace &mdash; which is ugly, off-brand, and tells an attacker
    your class names and line numbers. <code>&lt;error-page&gt;</code> replaces both.
</div>

<h3>The code from slides 42 and 44</h3>
<pre>&lt;!-- by HTTP status code --&gt;
&lt;error-page&gt;
    <span class="hl">&lt;error-code&gt;404&lt;/error-code&gt;</span>
    &lt;location&gt;/error_404.jsp&lt;/location&gt;
&lt;/error-page&gt;

&lt;!-- by Java exception type --&gt;
&lt;error-page&gt;
    <span class="hl">&lt;exception-type&gt;java.lang.Throwable&lt;/exception-type&gt;</span>
    &lt;location&gt;/error_java.jsp&lt;/location&gt;
&lt;/error-page&gt;</pre>

<div class="result">
    <h3>Run it &mdash; trigger a 404</h3>
    <p>
        <a class="btn" href="${pageContext.request.contextPath}/no-such-page">request a URL that doesn't exist</a>
        <a class="btn ghost" href="${pageContext.request.contextPath}/errorDemo?type=statusCode">response.sendError(404)</a>
    </p>

    <h3>Run it &mdash; throw a Java exception</h3>
    <p>All three are caught by the one <code>java.lang.Throwable</code> entry, because
       every exception inherits from it.</p>
    <p>
        <a class="btn" href="${pageContext.request.contextPath}/errorDemo?type=nullPointer">NullPointerException</a>
        <a class="btn ghost" href="${pageContext.request.contextPath}/errorDemo?type=arithmetic">ArithmeticException</a>
        <a class="btn ghost" href="${pageContext.request.contextPath}/errorDemo?type=custom">IllegalStateException</a>
    </p>
</div>

<div class="note">
    <strong>How the error page knows what happened.</strong> Slide 44 reads the
    exception straight out of the implicit page context:
    <pre style="margin:.6em 0 0">&lt;p&gt;Type: &#36;{pageContext.exception["class"]}&lt;/p&gt;
&lt;p&gt;Message: &#36;{pageContext.exception.message}&lt;/p&gt;</pre>
    <code>class</code> is in quotes because it is a reserved word in EL &mdash;
    <code>&#36;{pageContext.exception.class}</code> is a syntax error.
</div>

<div class="warn">
    <strong>Slide 33's comment, and why it is there.</strong> The book writes
    <code>&lt;!-- you can comment out these error tags when the app is in
    development --&gt;</code>. During development the stack trace <em>is</em> the useful
    output; a friendly page just hides the thing you need. Turn these on for production,
    and never show <code>${'$'}{pageContext.exception}</code> to a real user &mdash; the
    details belong in the log file (<a href="${pageContext.request.contextPath}/debug">case 16</a>),
    not on the screen.
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
