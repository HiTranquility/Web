<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%--
================================================================================
  thanks.jsp — trang xác nhận của CASE 11                      (slide 27)
================================================================================

  AI GỌI TRANG NÀY
    Chỉ EmailListServlet, và chỉ sau khi dữ liệu đã hợp lệ VÀ đã ghi file xong.
    Gõ thẳng /thanks.jsp thì attribute "user" không tồn tại và ba dòng thông
    tin hiện ra trống trơn — vì EL gặp null thì in rỗng chứ không báo lỗi.

  TOÀN BỘ TRANG KHÔNG CÓ MỘT DÒNG JAVA NÀO
    Không scriptlet <% %>, không import, không ép kiểu. Chỉ có ${user.email}.
    Đó chính là điều MVC hướng tới: JSP thuần hiển thị.

    Nhắc lại cơ chế: ${user.email} KHÔNG đọc field email, nó gọi getEmail().
    Xoá get method trong lớp User là trang này lặng lẽ in ra rỗng — không có
    exception, không có cảnh báo. Xem lại User.java.

  NÚT RETURN DÙNG method="get"
    Vì nó chỉ ĐỌC (hiện lại form), không GHI gì cả — đúng quy tắc slide 15
    (CASE 05). Tham số ẩn action=join báo cho servlet biết cần rẽ nhánh nào.

    Slide 19 (ch02) viết <form action="">, tức submit lại chính trang này và
    ra một trang trống. Ở đây sửa thành action="emailList" để request đi qua
    controller.
================================================================================
--%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Murach's Java Servlets and JSP</title>
    <link rel="stylesheet" href="styles/main.css" type="text/css"/>
</head>
<body>
<div class="wrap">
    <div class="topbar">
        <a href="${pageContext.request.contextPath}/">&larr; All 16 cases</a>
        <span class="slides">slides 27-31</span>
    </div>

    <h1>Thanks for joining our email list</h1>

    <p>Here is the information that you entered:</p>

    <table style="max-width:30em">
        <tr><th>Email:</th><td>${user.email}</td></tr>
        <tr><th>First Name:</th><td>${user.firstName}</td></tr>
        <tr><th>Last Name:</th><td>${user.lastName}</td></tr>
    </table>

    <p>To enter another email address, click the Return button.</p>

    <form action="emailList" method="get">
        <input type="hidden" name="action" value="join">
        <input type="submit" value="Return">
    </form>

    <div class="note">
        <strong>It was written to disk.</strong> The servlet asked
        <code>getServletConfig().getInitParameter("relativePathToFile")</code> for the
        file name (<a href="${pageContext.request.contextPath}/initParams">case 12</a>),
        turned it into a real path with <code>getRealPath</code>
        (<a href="${pageContext.request.contextPath}/realPath">case 07</a>), and logged
        the address to the console and the log file
        (<a href="${pageContext.request.contextPath}/debug">case 16</a>).
    </div>

    <div class="footnav"><a href="${pageContext.request.contextPath}/">&larr; Back to the case index</a></div>
</div>
</body>
</html>
