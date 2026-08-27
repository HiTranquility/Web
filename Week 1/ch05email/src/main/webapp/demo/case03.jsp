<%--
================================================================================
  case03.jsp — phần VIEW của case này
================================================================================

  Trang này được AnnotationServlet forward tới (URL /annotation).
  Điểm cần thấy: getServletName() trả "MurachAnnotationServlet" — lấy từ thuộc
  tính name của annotation, KHÔNG phải tên lớp.
================================================================================
--%>
<% request.setAttribute("caseNumber", "03");
   request.setAttribute("caseTitle", "The @WebServlet annotation");
   request.setAttribute("caseSlides", "slides 10-11"); %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="problem">
    <strong>The problem</strong>
    Keeping mappings in <code>web.xml</code> means every new servlet is edited in two
    places, and the URL lives far away from the code that answers it. Since Servlet
    3.0 an annotation on the class does the same job.
</div>

<h3>The code from the slides</h3>
<pre>import javax.servlet.annotation.WebServlet;

<span class="hl">@WebServlet("/test")</span>
public class TestServlet extends HttpServlet {
    ...
}</pre>

<h3>The two other forms on slide 11</h3>
<pre>// map a servlet to multiple URLs
<span class="hl">@WebServlet(urlPatterns={"/emailList", "/email/*"})</span>

// specify an internal name for the servlet
<span class="hl">@WebServlet(name="MurachTestServlet", urlPatterns={"/test"})</span></pre>

<div class="result">
    <h3>Run it</h3>
    <p>This page is served by <code>AnnotationServlet</code>, which is declared like
       this &mdash; and which appears <em>nowhere</em> in <code>web.xml</code>:</p>
    <pre>@WebServlet(name = "MurachAnnotationServlet",
            urlPatterns = {"/annotation", "/anno/*"})</pre>
    <table>
        <tr><th>getServletName()</th>
            <td><code>${servletName}</code> &mdash; from the <code>name</code> attribute,
                not the class name</td></tr>
        <tr><th>getServletPath()</th><td><code>${servletPath}</code></td></tr>
        <tr><th>getPathInfo()</th><td><code>${empty pathInfo ? "null" : pathInfo}</code></td></tr>
    </table>
    <p>
        <a class="btn" href="${pageContext.request.contextPath}/annotation">/annotation</a>
        <a class="btn ghost" href="${pageContext.request.contextPath}/anno/anything/here">/anno/anything/here</a>
    </p>
</div>

<div class="note">
    <strong>Annotation or web.xml?</strong> Use the annotation for a plain mapping &mdash;
    it is shorter and it cannot drift out of sync with the class. Keep
    <code>web.xml</code> when the value must change without recompiling
    (see <a href="${pageContext.request.contextPath}/initParams">case 12</a>), for
    <code>&lt;error-page&gt;</code> and <code>&lt;context-param&gt;</code>, which have
    no annotation, and when you want every mapping visible in one file.
    Both work at once, which is what this application does.
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
