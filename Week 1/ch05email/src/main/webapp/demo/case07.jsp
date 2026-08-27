<%--
================================================================================
  case07.jsp — phần VIEW của case này
================================================================================

  Đường dẫn hiện trên trang này KHÁC trên máy người khác. Đó chính
  là điều case này muốn chứng minh, nên trang cố ý in ra giá trị thật thay vì
  mô tả suông.

  Nút "Try to fetch it directly" trỏ vào /WEB-INF/EmailList.txt và sẽ ra 404 —
  bằng chứng Tomcat chặn mọi truy cập trực tiếp vào WEB-INF.
================================================================================
--%>
<% request.setAttribute("caseNumber", "07");
   request.setAttribute("caseTitle", "ServletContext.getRealPath");
   request.setAttribute("caseSlides", "slides 18-20"); %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="problem">
    <strong>The problem</strong>
    Your servlet knows a file as <code>/WEB-INF/EmailList.txt</code>. But
    <code>java.io.File</code> has no idea what that means &mdash; it needs an absolute
    path on the server's disk, and that path is different on your laptop, on the lab
    machine, and in production.
</div>

<h3>The code from slide 19</h3>
<pre>ServletContext sc = this.getServletContext();
String path = <span class="hl">sc.getRealPath("/WEB-INF/EmailList.txt")</span>;

// a more concise way to write the same code
String path = this.getServletContext()
                  .getRealPath("/WEB-INF/EmailList.txt");</pre>

<div class="result">
    <h3>What it returned on this machine, right now</h3>
    <table>
        <tr><th>the relative path</th><td><code>${relativePath}</code></td></tr>
        <tr><th>getRealPath() returned</th><td><code>${realPath}</code></td></tr>
        <tr><th>both forms agree</th>
            <td><span class="${bothFormsAgree ? 'yes' : 'no'}">${bothFormsAgree ? "yes - they are the same call" : "no"}</span></td></tr>
        <tr><th>the context root</th><td><code>${contextRoot}</code></td></tr>
        <tr><th>does the file exist yet</th>
            <td><span class="${fileExists ? 'yes' : 'no'}">${fileExists ? "yes" : "not yet"}</span>
                ${fileExists ? "" : "&mdash; submit case 11 once and it appears"}
                ${fileExists ? fileSize : ""}${fileExists ? " bytes" : ""}</td></tr>
    </table>
    <p style="color:#5b6b7c;font-size:.9em">Slide 19 shows the book's answer:
       <code>C:\murach\servlet_and_jsp\netbeans\book_apps\ch05email\build\web\WEB-INF\EmailList.txt</code>.
       Yours is different &mdash; that is the whole lesson.</p>
</div>

<div class="warn">
    <strong>Why /WEB-INF.</strong> Tomcat refuses to serve anything under
    <code>/WEB-INF</code> to a browser, so the data file is unreachable by URL while
    still being readable by your code. Put <code>EmailList.txt</code> in the web root
    instead and anyone can download your whole mailing list.
    <a class="btn ghost" href="${pageContext.request.contextPath}/WEB-INF/EmailList.txt">Try to fetch it directly</a>
</div>

<div class="note">
    <strong>Slide 20.</strong> <code>getServletContext()</code> is available to every
    servlet because servlets inherit <code>GenericServlet</code>. Besides real paths,
    the context is how you read global initialization parameters
    (<a href="${pageContext.request.contextPath}/initParams">case 12</a>), share
    application-wide variables, and write to log files
    (<a href="${pageContext.request.contextPath}/debug">case 16</a>).
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
