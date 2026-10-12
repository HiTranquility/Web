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
            <div class="panel panel-ok" style="line-height: 1.6;">
                <b>Đã gửi thư thành công!</b><br>
                Nếu địa chỉ email này tồn tại trên hệ thống, chúng tôi đã gửi thư kèm liên kết hướng dẫn đặt lại mật khẩu.<br>
                Vui lòng kiểm tra hộp thư đến (và thư mục Spam/Quảng cáo nếu chưa thấy) trong vòng 30 phút tới.
            </div>

            <c:if test="${not empty devLink}">
                <div style="margin-top: 16px; padding: 14px 16px; background: rgba(56, 189, 248, 0.08); border: 1px solid rgba(56, 189, 248, 0.3); border-radius: 10px; font-size: 0.9rem; text-align: left;">
                    <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 6px;">
                        <span style="font-size: 1.1rem;">🛠️</span>
                        <strong style="color: #38bdf8;">Chế độ Đồ án / Môi trường Thử nghiệm:</strong>
                    </div>
                    <div style="color: var(--text-mut); font-size: 0.84rem; line-height: 1.5; margin-bottom: 10px;">
                        Hệ thống đang chạy cục bộ (chưa bật gửi email SMTP thực tế ra ngoài Internet). Bạn có thể bấm trực tiếp vào liên kết bên dưới để hoàn tất việc đổi mật khẩu ngay lập tức:
                    </div>
                    <div>
                        <a href="${devLink}" class="btn btn-primary" style="display: inline-block; padding: 7px 14px; font-size: 0.85rem; font-weight: 700; text-decoration: none;">
                            🔑 Đặt Lại Mật Khẩu Ngay &rarr;
                        </a>
                    </div>
                </div>
            </c:if>
        </c:when>

        <c:otherwise>
            <form method="post" action="${pageContext.request.contextPath}/auth">
                <input type="hidden" name="_csrf" value="${csrfToken}">
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
