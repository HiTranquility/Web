<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  register.jsp — MẢNH nội dung, dùng khung layout/auth.jsp
  Nhận: message · username · email (giữ lại chữ đã gõ khi lỗi)
--%>
<h1>Đăng ký</h1>
<p>Đã có tài khoản?
   <a href="${pageContext.request.contextPath}/auth?action=login">Đăng nhập</a></p>

<c:if test="${not empty message}">
    <p class="form-error"><c:out value="${message}"/></p>
</c:if>

<form action="${pageContext.request.contextPath}/auth" method="post">
    <input type="hidden" name="action" value="register">

    <label for="username">Tên đăng nhập</label>
    <input type="text" id="username" name="username"
           value="<c:out value='${username}'/>"
           pattern="[a-zA-Z0-9_]{3,50}"
           title="3-50 ký tự, chỉ chữ, số và dấu gạch dưới" autofocus required>

    <label for="email">Email</label>
    <input type="email" id="email" name="email"
           value="<c:out value='${email}'/>" required>

    <label for="password">Mật khẩu</label>
    <input type="password" id="password" name="password" minlength="6" required>

    <label for="confirm">Nhập lại mật khẩu</label>
    <input type="password" id="confirm" name="confirm" minlength="6" required>

    <%--
      Mục tiêu đồ án yêu cầu có "điều dẫn sử dụng và luật cho người dùng".
      Ô tick này là chỗ người dùng chấp nhận nội quy — servlet kiểm lại lần nữa
      ở phía server, vì thuộc tính required của HTML ai cũng tắt được.
    --%>
    <label class="check">
        <input type="checkbox" name="agree" value="yes" required>
        Tôi đã đọc và đồng ý với
        <a href="${pageContext.request.contextPath}/page?name=rules" target="_blank">nội quy cộng đồng</a>
    </label>

    <button type="submit" class="btn btn-primary">Tạo tài khoản</button>
</form>
