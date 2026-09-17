<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  _error404.jsp — MẢNH nội dung trang lỗi 404 nghệ thuật.
--%>
<div class="error-hero-card">
    <div class="error-hero-badge">🧭</div>
    <div class="error-hero-code">404</div>
    <h2 class="error-hero-title">Lạc Lối Trong Thư Viện</h2>
    <p class="error-hero-desc">
        Trang bạn đang tìm kiếm có thể đã đổi tên, bị gỡ bỏ hoặc chưa từng tồn tại trên giá sách.
        Đừng lo, kho tàng vạn cuốn sách hay vẫn luôn chào đón bạn!
    </p>

    <div class="error-hero-actions">
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/">
            🏠 Về trang chủ
        </a>
        <a class="btn btn-ghost" href="${pageContext.request.contextPath}/story?action=list">
            📚 Khám phá kho truyện
        </a>
        <a class="btn btn-ghost" href="${pageContext.request.contextPath}/rank">
            🏆 Bảng xếp hạng
        </a>
    </div>
</div>
