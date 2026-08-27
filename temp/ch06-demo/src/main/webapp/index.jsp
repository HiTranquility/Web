<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
================================================================================
  index.jsp — SLIDE 28: JSP dùng cả hai file include
================================================================================
  Đây là trang chính của chương 6. Nó gộp gần như mọi thứ chương này dạy:
      slide 11  taglib directive khai báo JSTL
      slide 12  <c:if> hiển thị thông báo lỗi
      slide 21  EL đọc property của bean:  ${user.email}
      slide 28  bố cục dùng include header + footer
      slide 32  <c:import> include lúc CHẠY

  Đối chiếu với chương 2: cũng trang này nhưng viết bằng HTML thuần, không
  include, không EL. Chương 6 là bước nâng cấp đó.
================================================================================
--%>
<c:import url="/WEB-INF/includes/header.html" />

<h1>Join our email list</h1>
<p>To join our email list, enter your name and
   email address below.</p>

<%-- slide 12: chỉ hiện đoạn này khi servlet đã đặt message.
     Kiểm tra != null đúng như slide. --%>
<c:if test="${message != null}">
    <p class="err"><i>${message}</i></p>
</c:if>

<form action="emailList" method="post">
    <input type="hidden" name="action" value="add">

    <%-- slide 21: EL đọc property của bean.
         Lần đầu vào trang, user chưa tồn tại -> EL in ra rỗng, KHÔNG lỗi.
         Đó chính là "EL xử lý null tốt hơn" ở slide 22. --%>
    <label class="pad_top">Email:</label>
    <input type="email" name="email" value="${user.email}"><br>

    <label class="pad_top">First Name:</label>
    <input type="text" name="firstName" value="${user.firstName}"><br>

    <label class="pad_top">Last Name:</label>
    <input type="text" name="lastName" value="${user.lastName}"><br>

    <label>&nbsp;</label>
    <input type="submit" value="Join Now" class="margin_left">
</form>


<c:import url="/WEB-INF/includes/footer.jsp" />
