<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
================================================================================
  auth/forgot.jsp — MẢNH NỘI DUNG: Quên mật khẩu               TRANG 21
================================================================================
  TẦNG: views/ · layout: auth

  Nhận: sent (boolean) · devLink (String, chỉ có trong đồ án)
================================================================================
--%>
<div class="auth-card">
    <h1>Quên mật khẩu</h1>
    <p class="auth-sub">
        Nhập email đã đăng ký. Chúng tôi sẽ gửi liên kết đặt lại mật khẩu.
    </p>

    <c:if test="${not empty message}">
        <div class="panel panel-warn"><c:out value="${message}"/></div>
    </c:if>

    <c:choose>
        <c:when test="${sent}">
            <%--
              Câu này hiện KỂ CẢ khi email không tồn tại — cố ý.
              Báo "email này chưa đăng ký" là biến trang thành công cụ dò xem
              ai có tài khoản ở đây. Xem ghi chú trong AuthServlet.forgot().
            --%>
            <div class="panel panel-ok">
                Nếu email đó có tài khoản, liên kết đặt lại đã được gửi đi.
                Kiểm tra hộp thư trong 30 phút tới.
            </div>

            <c:if test="${not empty devLink}">
                <div class="panel panel-warn">
                    <b>Chế độ đồ án — không có máy chủ gửi thư.</b><br>
                    Liên kết lẽ ra nằm trong email, ở đây hiện thẳng ra:
                    <a href="${devLink}">Đặt lại mật khẩu ngay</a>
                    <p class="muted-note" style="margin-top:8px">
                        Trong hệ thống thật, hiện liên kết này ra màn hình là
                        lỗ hổng nghiêm trọng — ai gõ email của người khác cũng
                        chiếm được tài khoản của họ.
                    </p>
                </div>
            </c:if>
        </c:when>

        <c:otherwise>
            <form method="post" action="${pageContext.request.contextPath}/auth">
                <input type="hidden" name="action" value="forgot">

                <div class="field">
                    <label for="email">Email</label>
                    <input type="email" id="email" name="email" required autofocus
                           placeholder="ban@example.com">
                </div>

                <button type="submit" class="btn btn-primary btn-block">
                    Gửi liên kết đặt lại
                </button>
            </form>
        </c:otherwise>
    </c:choose>

    <p class="auth-alt">
        Nhớ ra rồi?
        <a href="${pageContext.request.contextPath}/auth?action=login">Đăng nhập</a>
    </p>
</div>
