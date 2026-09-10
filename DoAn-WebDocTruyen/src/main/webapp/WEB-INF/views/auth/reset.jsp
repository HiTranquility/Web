<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
================================================================================
  auth/reset.jsp — MẢNH NỘI DUNG: Đặt lại mật khẩu             TRANG 22
================================================================================
  TẦNG: views/ · layout: auth

  Nhận: token (String — đã được AuthServlet kiểm là còn hiệu lực)

  Token đi theo trường ẩn chứ không nằm trên thanh địa chỉ khi gửi form:
  POST giấu nó khỏi lịch sử duyệt web và khỏi nhật ký máy chủ.
  AuthServlet vẫn kiểm lại token một lần nữa ở bước POST — trường ẩn là thứ
  người dùng sửa được, nên không có gì được tin chỉ vì nó ẩn.
================================================================================
--%>
<div class="auth-card">
    <h1>Đặt lại mật khẩu</h1>
    <p class="auth-sub">Nhập mật khẩu mới cho tài khoản của bạn.</p>

    <c:if test="${not empty message}">
        <div class="panel panel-warn"><c:out value="${message}"/></div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/auth">
        <input type="hidden" name="_csrf" value="${csrfToken}">
        <input type="hidden" name="action" value="reset">
        <input type="hidden" name="token" value="<c:out value='${token}'/>">

        <div class="field">
            <label for="password">Mật khẩu mới</label>
            <input type="password" id="password" name="password"
                   minlength="6" required autofocus>
            <p class="field-hint">Từ 6 ký tự trở lên.</p>
        </div>

        <div class="field">
            <label for="confirmPassword">Nhập lại mật khẩu mới</label>
            <input type="password" id="confirmPassword" name="confirmPassword"
                   minlength="6" required>
        </div>

        <button type="submit" class="btn btn-primary btn-block">Đổi mật khẩu</button>
    </form>

    <p class="auth-alt">
        Liên kết chỉ dùng được <b>một lần</b> và hết hạn sau 30 phút.
    </p>
</div>
