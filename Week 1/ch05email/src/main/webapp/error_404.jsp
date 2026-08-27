<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%--
================================================================================
  error_404.jsp — trang cho mã trạng thái 404                  (slide 42-43)
================================================================================

  ĐƯỢC GỌI KHI NÀO
    Khai báo trong web.xml:
        <error-page>
            <error-code>404</error-code>
            <location>/error_404.jsp</location>
        </error-page>
    Kích hoạt khi URL không tồn tại, hoặc khi code gọi response.sendError(404).

  THÔNG TIN VỀ LỖI LẤY Ở ĐÂU
    Tomcat đặt sẵn một loạt attribute có tên bắt đầu bằng "javax.servlet.error.":
        javax.servlet.error.status_code    mã trạng thái
        javax.servlet.error.request_uri    URL người dùng đã yêu cầu
        javax.servlet.error.message        thông điệp
        javax.servlet.error.exception      object exception (nếu có)

    Đọc bằng cú pháp NGOẶC VUÔNG:
        ${requestScope['javax.servlet.error.request_uri']}

    Bắt buộc dùng ngoặc vuông vì tên có dấu chấm. Viết
    ${requestScope.javax.servlet.error.request_uri} thì EL hiểu là đi vào
    property "javax", rồi property "servlet"... và không tìm thấy gì.
    Quy tắc chung của EL: tên có dấu chấm, dấu gạch ngang, hoặc trùng từ khoá
    thì phải dùng ngoặc vuông và nháy đơn.

  TẠI SAO KHÔNG CẦN isErrorPage="true" Ở ĐÂY
    Vì trang này chỉ đọc attribute thường, không đụng tới pageContext.exception.
    Trang error_java.jsp thì cần — xem file đó.

  THANH ĐỊA CHỈ VẪN GIỮ URL SAI
    Tomcat forward nội bộ tới đây chứ không redirect. Nhờ vậy trình duyệt vẫn
    nhận đúng mã 404 thật (quan trọng với công cụ tìm kiếm), và người dùng vẫn
    thấy mình đã gõ nhầm cái gì.
================================================================================
--%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>404 Error</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/main.css" type="text/css"/>
</head>
<body>
<div class="wrap">
    <h1>404 Error</h1>
    <p>The server was not able to find the file you requested.</p>
    <p>To continue, click the Back button.</p>

    <div class="result">
        <table>
            <tr><th>the URL that was requested</th>
                <td><code>${empty requestScope['javax.servlet.error.request_uri']
                            ? "-" : requestScope['javax.servlet.error.request_uri']}</code></td></tr>
            <tr><th>status code</th>
                <td><code>${requestScope['javax.servlet.error.status_code']}</code></td></tr>
        </table>
    </div>

    <div class="note">
        This page is <code>/error_404.jsp</code>, wired up by the
        <code>&lt;error-page&gt;</code> element in <code>web.xml</code>. Notice the
        address bar still shows the URL you asked for &mdash; Tomcat forwards here
        internally rather than redirecting, so the browser still gets a real 404 status.
    </div>

    <div class="footnav"><a href="${pageContext.request.contextPath}/">&larr; Back to the case index</a></div>
</div>
</body>
</html>
