<%--
================================================================================
  case04.jsp — phần VIEW của case này
================================================================================

  Trang có ĐỦ CẢ BA CÁCH tạo request GET ở slide 13:
    - thẻ <a href="...?action=add&firstName=John">     (cách 3)
    - <form method="get">                              (cách 2)
    - thanh địa chỉ                                    (cách 1, bạn tự gõ)

  Chú ý dấu & trong href phải viết là &amp;. Đó là quy tắc của HTML, không
  phải của JSP: & là ký tự mở đầu một entity, để trần là HTML không hợp lệ.
  Trình duyệt vẫn giải mã lại thành & trước khi gửi đi, nên servlet không
  thấy khác gì.
================================================================================
--%>
<% request.setAttribute("caseNumber", "04");
   request.setAttribute("caseTitle", "The HTTP GET method");
   request.setAttribute("caseSlides", "slides 12-13"); %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="problem">
    <strong>The problem</strong>
    A GET request carries its parameters in the query string, after the
    <code>?</code>. There are three different ways to build one &mdash; and the
    servlet cannot tell them apart, which is exactly the point.
</div>

<h3>The three ways from slide 13</h3>
<pre>1: Enter the URL into the browser's address bar
   http://localhost:8080/ch05email/emailList?action=add&amp;firstName=John

2: Code a form that uses the GET method
   &lt;form action="emailList"&gt;
   &lt;form action="emailList" <span class="hl">method="get"</span>&gt;

3: Code an anchor tag
   &lt;a href="emailList<span class="hl">?action=join</span>"&gt;Display Email Entry Test&lt;/a&gt;</pre>

<div class="result">
    <h3>Run it &mdash; way 3, an anchor tag</h3>
    <p>
        <a class="btn" href="${pageContext.request.contextPath}/getDemo?action=add&amp;firstName=John&amp;lastName=Smith">
            getDemo?action=add&amp;firstName=John&amp;lastName=Smith</a>
    </p>

    <h3>Run it &mdash; way 2, a form with method="get"</h3>
    <form action="${pageContext.request.contextPath}/getDemo" method="get" class="stack">
        <p><label>firstName:</label><input type="text" name="firstName" value="Mary"></p>
        <p><label>lastName:</label><input type="text" name="lastName" value="Jones"></p>
        <input type="hidden" name="action" value="add">
        <input type="submit" value="Submit with GET">
    </form>
    <p style="color:#5b6b7c;font-size:.9em">Way 1 is the address bar &mdash; after you
       submit, edit the URL up there and press Enter. Same result.</p>

    <h3>What the servlet received</h3>
    <table>
        <tr><th>getMethod()</th><td><code>${empty method ? "-" : method}</code></td></tr>
        <tr><th>getQueryString()</th><td><code>${empty queryString ? "null - nothing after the ?" : queryString}</code></td></tr>
        <tr><th>getParameter("action")</th><td><code>${empty action ? "null" : action}</code></td></tr>
        <tr><th>getParameter("firstName")</th><td><code>${empty firstName ? "null" : firstName}</code></td></tr>
        <tr><th>getParameter("lastName")</th><td><code>${empty lastName ? "null" : lastName}</code></td></tr>
    </table>
</div>

<div class="warn">
    <strong>Slide 13's note, made concrete.</strong> A servlet must implement
    <code>doGet</code> to process a GET request. <code>GetParamsServlet</code>
    deliberately implements <em>only</em> <code>doGet</code> &mdash; so POSTing to it
    gives you <b>HTTP 405 Method Not Allowed</b>, straight from
    <code>HttpServlet</code>'s default <code>doPost</code>. Try it:
    <form action="${pageContext.request.contextPath}/getDemo" method="post" style="display:inline">
        <input type="submit" value="POST to /getDemo (expect 405)">
    </form>
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
