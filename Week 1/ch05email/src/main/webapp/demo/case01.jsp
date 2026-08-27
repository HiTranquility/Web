<%--
================================================================================
  case01.jsp — phần VIEW của case này
================================================================================

  TRANG NÀY KHÔNG NHẬN DỮ LIỆU TỪ SERVLET.
  Nó chỉ là trang tĩnh giới thiệu case, có hai nút bấm sang /test. Chính
  TestServlet mới là thứ tự sinh HTML — xem file TestServlet.java.

  Để ý: nút "GET /test" là thẻ <a>, nút "POST /test" là <form method="post">.
  Đó chính là cách 3 và cách 2 của slide 13 (CASE 04).
================================================================================
--%>
<% request.setAttribute("caseNumber", "01");
   request.setAttribute("caseTitle", "A servlet that returns HTML");
   request.setAttribute("caseSlides", "slides 4-6"); %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="problem">
    <strong>The problem</strong>
    Sometimes a servlet has no JSP behind it and still has to answer with a page.
    It builds the HTML itself, writing to the response's <code>PrintWriter</code>.
</div>

<h3>The code from the slides</h3>
<pre>public class TestServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        <span class="hl">response.setContentType("text/html");</span>
        <span class="hl">PrintWriter out = response.getWriter();</span>
        try {
            out.println("&lt;h1&gt;HTML from servlet&lt;/h1&gt;");
        } finally {
            out.close();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        <span class="hl">doPost(request, response);</span>
    }
}</pre>

<div class="result">
    <h3>Run it</h3>
    <p>The servlet is mapped to <code>/test</code> in <code>web.xml</code>. Both
       buttons reach the same code, because <code>doGet</code> just calls
       <code>doPost</code>.</p>
    <p>
        <a class="btn" href="${pageContext.request.contextPath}/test">GET /test</a>
        <form action="${pageContext.request.contextPath}/test" method="post" style="display:inline">
            <input type="submit" value="POST /test">
        </form>
    </p>
</div>

<div class="note">
    <strong>Why doGet calls doPost.</strong> A servlet answers a GET request only if
    it implements <code>doGet</code>, and a POST only if it implements
    <code>doPost</code>. Writing the logic once and having one delegate to the other
    means the servlet responds to both without duplicating anything.
    <br><br>
    <strong>setContentType comes first.</strong> Call it before
    <code>getWriter()</code>, or the response is already committed to the default
    encoding and your charset is ignored.
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
