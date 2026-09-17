<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  _error403.jsp — MẢNH nội dung trang lỗi 403 nghệ thuật.
--%>
<div class="error-hero-card">
    <div class="error-hero-badge">🛡️</div>
    <div class="error-hero-code">403</div>
    <h2 class="error-hero-title">Khu Vực Giới Hạn Quyền</h2>
    <p class="error-hero-desc">
        Trang này dành riêng cho quản trị viên hoặc tác giả sở hữu nội dung.
        Nếu bạn cho rằng đây là sự nhầm lẫn, hãy đăng nhập với tài khoản có quyền phù hợp.
    </p>

    <div class="error-hero-actions">
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/">
            🏠 Về trang chủ
        </a>
        <c:choose>
            <c:when test="${empty currentUser}">
                <a class="btn btn-ghost" href="${pageContext.request.contextPath}/auth?action=login">
                    🔑 Đăng nhập ngay
                </a>
            </c:when>
            <c:otherwise>
                <a class="btn btn-ghost" href="${pageContext.request.contextPath}/auth?action=logout">
                    🔄 Đổi tài khoản
                </a>
            </c:otherwise>
        </c:choose>
    </div>
</div>
