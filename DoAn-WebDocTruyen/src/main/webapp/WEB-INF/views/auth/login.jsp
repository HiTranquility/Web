<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  login.jsp — MẢNH nội dung, dùng khung layout/auth.jsp
  Nhận: message (String, có thể null) · username (giữ lại chữ đã gõ)
--%>
<h1>Đăng nhập</h1>
<p>Chưa có tài khoản?
   <a href="${pageContext.request.contextPath}/auth?action=register">Đăng ký</a></p>

<%-- Chỉ hiện khi servlet có đặt message. Dùng "not empty" thay vì "!= null"
     để bắt được cả trường hợp chuỗi rỗng. --%>
<c:if test="${not empty message}">
    <p class="form-error"><c:out value="${message}"/></p>
</c:if>

<%--
  method="post" vì đây là hành động GHI (tạo phiên đăng nhập), và vì mật khẩu
  không được nằm trên thanh địa chỉ. Quy tắc chọn GET/POST: standards §01.
--%>
<form action="${pageContext.request.contextPath}/auth" method="post">
    <input type="hidden" name="action" value="login">

    <label for="username">Tên đăng nhập</label>
    <%-- value="<c:out .../>" — escape vì đây là chữ NGƯỜI DÙNG vừa gõ.
         Không escape thì gõ  "><script>  vào là thoát ra khỏi thuộc tính. --%>
    <input type="text" id="username" name="username"
           value="<c:out value='${username}'/>" autofocus required>

    <label for="password">Mật khẩu</label>
    <input type="password" id="password" name="password" required>

    <button type="submit" class="btn btn-primary">Đăng nhập</button>
</form>
