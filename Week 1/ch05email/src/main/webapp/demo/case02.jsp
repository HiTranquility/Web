<%--
================================================================================
  case02.jsp — phần VIEW của case này
================================================================================

  DỮ LIỆU DO MappingServlet ĐẶT VÀO REQUEST rồi forward sang đây.
  Vào thẳng /demo/case02.jsp thì mọi attribute đều rỗng — nên mỗi ô đều bọc
  trong ${empty x ? "..." : x} để trang vẫn đọc được khi chưa có dữ liệu.

  Toán tử "empty" của EL bắt được CẢ null LẪN chuỗi rỗng chỉ bằng một từ. Nếu
  viết ${x == null ? ...} thì chuỗi rỗng lọt qua và trang hiện ra một ô trắng
  khó hiểu.
================================================================================
--%>
<% request.setAttribute("caseNumber", "02");
   request.setAttribute("caseTitle", "Servlet mapping and URL patterns");
   request.setAttribute("caseSlides", "slides 7-9"); %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="problem">
    <strong>The problem</strong>
    A servlet class is a Java class &mdash; nothing about it says which URL should
    reach it. The <code>&lt;servlet-mapping&gt;</code> elements in
    <code>web.xml</code> make that connection, and one servlet can answer several
    patterns.
</div>

<h3>The code from the slides</h3>
<pre>&lt;!-- the definitions for the servlets --&gt;
&lt;servlet&gt;
    &lt;servlet-name&gt;<span class="hl">MappingServlet</span>&lt;/servlet-name&gt;
    &lt;servlet-class&gt;murach.demo.MappingServlet&lt;/servlet-class&gt;
&lt;/servlet&gt;

&lt;!-- the mapping for the servlets --&gt;
&lt;servlet-mapping&gt;
    &lt;servlet-name&gt;<span class="hl">MappingServlet</span>&lt;/servlet-name&gt;
    &lt;url-pattern&gt;/mapping&lt;/url-pattern&gt;
&lt;/servlet-mapping&gt;
&lt;servlet-mapping&gt;
    &lt;servlet-name&gt;<span class="hl">MappingServlet</span>&lt;/servlet-name&gt;
    &lt;url-pattern&gt;/email/*&lt;/url-pattern&gt;
&lt;/servlet-mapping&gt;</pre>

<p>The <code>&lt;servlet-name&gt;</code> is the glue: it is not a URL and not a class
   name, it just ties the two blocks together inside this one file.</p>

<div class="result">
    <h3>Run it</h3>
    <p>Each link below reaches <em>the same servlet instance</em>. Watch how
       <code>getServletPath()</code> and <code>getPathInfo()</code> split the URL
       differently for an exact pattern than for a <code>/*</code> pattern.</p>
    <p>
        <a class="btn" href="${pageContext.request.contextPath}/mapping">/mapping</a>
        <a class="btn ghost" href="${pageContext.request.contextPath}/email/">/email/</a>
        <a class="btn ghost" href="${pageContext.request.contextPath}/email/add">/email/add</a>
        <a class="btn ghost" href="${pageContext.request.contextPath}/email/list/2024">/email/list/2024</a>
    </p>

    <table>
        <tr><th>getRequestURI()</th><td><code>${requestURI}</code></td></tr>
        <tr><th>getContextPath()</th><td><code>${contextPath}</code></td></tr>
        <tr><th>getServletPath()</th>
            <td><code>${servletPath}</code> &mdash; the part that matched the pattern</td></tr>
        <tr><th>getPathInfo()</th>
            <td><code>${empty pathInfo ? "null" : pathInfo}</code>
                &mdash; ${empty pathInfo ? "an exact pattern leaves nothing over" : "this is what the * matched"}</td></tr>
        <tr><th>getServletName()</th><td><code>${servletName}</code></td></tr>
    </table>
</div>

<div class="note">
    <strong>Slide 9's three patterns.</strong>
    <code>/emailList</code> matches that one URL exactly.
    <code>/email/*</code> matches anything under <code>/email</code>, and the part
    after it lands in <code>getPathInfo()</code> &mdash; that is how you build URLs
    like <code>/email/add</code> and <code>/email/delete</code> from one servlet.
    <code>/email/add</code> matches only that one URL, and beats the
    <code>/*</code> pattern because Tomcat prefers the most specific match.
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
