<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%--
================================================================================
  error_java.jsp — trang cho mọi exception Java                (slide 44-45)
================================================================================

  ĐƯỢC GỌI KHI NÀO
    Khai báo trong web.xml:
        <error-page>
            <exception-type>java.lang.Throwable</exception-type>
            <location>/error_java.jsp</location>
        </error-page>
    Mọi exception đều kế thừa Throwable nên một khai báo bắt được tất cả.

  isErrorPage="true" — THUỘC TÍNH QUAN TRỌNG NHẤT CỦA FILE NÀY
    Nó là thứ làm cho biến ngầm định pageContext.exception có giá trị. Thiếu
    nó thì EL in ra rỗng, trang trông như bị hỏng, và không có bất kỳ thông
    báo lỗi nào để lần ra nguyên nhân. Đây là lỗi hay gặp nhất khi làm trang lỗi.

  ${pageContext.exception["class"]} — VÌ SAO PHẢI CÓ NGOẶC VUÔNG
    "class" là từ khoá dành riêng trong EL. Viết ${pageContext.exception.class}
    là lỗi cú pháp, trang không biên dịch được. Ngoặc vuông kèm nháy đơn/kép
    là cách thoát khỏi ràng buộc đó — cùng lý do với error_404.jsp.

    Hai dòng này chép nguyên từ slide 44.

  TRÊN PRODUCTION THÌ BỎ PHẦN "Details" ĐI
    Loại exception và thông điệp là manh mối cho kẻ tấn công: chúng tiết lộ tên
    lớp, tên thư viện, đôi khi cả cấu trúc database. Ở đây giữ lại vì đang học.
    Chi tiết kỹ thuật thuộc về log file (CASE 16), không thuộc về màn hình.
================================================================================
--%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Java Error</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/main.css" type="text/css"/>
</head>
<body>
<div class="wrap">
    <h1>Java Error</h1>
    <p>Sorry, Java has thrown an exception.</p>
    <p>To continue, click the Back button.</p>

    <h2>Details</h2>
    <p>Type: ${pageContext.exception["class"]}</p>
    <p>Message: ${pageContext.exception.message}</p>

    <div class="note">
        Both lines come straight from slide 44. <code>isErrorPage="true"</code> at the
        top of this file is what makes <code>pageContext.exception</code> available
        &mdash; without it, EL prints nothing here and the page looks broken for no
        obvious reason.
        <br><br>
        <code>["class"]</code> uses bracket notation because <code>class</code> is
        reserved in EL. Everything else can use the dot.
    </div>

    <div class="warn">
        In production, print the apology and nothing else. The type and message are
        here because this is a teaching app &mdash; on a real site they go to the log
        file instead.
    </div>

    <div class="footnav"><a href="${pageContext.request.contextPath}/demo/case13.jsp">&larr; Back to case 13</a></div>
</div>
</body>
</html>
