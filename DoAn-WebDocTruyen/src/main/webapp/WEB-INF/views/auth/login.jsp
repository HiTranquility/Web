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
    <input type="hidden" name="_csrf" value="${csrfToken}">
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

<div class="auth-divider">
    <span>hoặc tiếp tục với</span>
</div>

<button type="button" class="btn-google-login">
    <svg width="18" height="18" viewBox="0 0 18 18">
        <path d="M17.64 9.2c0-.637-.057-1.251-.164-1.84H9v3.481h4.844c-.209 1.125-.843 2.078-1.796 2.717v2.258h2.908c1.702-1.567 2.684-3.874 2.684-6.616z" fill="#4285F4"/>
        <path d="M9 18c2.43 0 4.467-.806 5.956-2.184l-2.908-2.258c-.806.54-1.837.86-3.048.86-2.344 0-4.328-1.584-5.036-3.711H.957v2.332C2.438 15.983 5.482 18 9 18z" fill="#34A853"/>
        <path d="M3.964 10.707c-.18-.54-.282-1.117-.282-1.707s.102-1.167.282-1.707V4.961H.957C.347 6.173 0 7.548 0 9s.348 2.827.957 4.039l3.007-2.332z" fill="#FBBC05"/>
        <path d="M9 3.58c1.321 0 2.508.454 3.44 1.345l2.582-2.58C13.463.891 11.426 0 9 0 5.482 0 2.438 2.017.957 4.961L3.964 7.293C4.672 5.166 6.656 3.58 9 3.58z" fill="#EA4335"/>
    </svg>
    <span>Đăng nhập với Google</span>
</button>

<p class="auth-alt">
    <a href="${pageContext.request.contextPath}/auth?action=forgot">Quên mật khẩu?</a>
</p>



