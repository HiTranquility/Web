<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  _error500.jsp — MẢNH nội dung trang lỗi 500 nghệ thuật.
--%>
<div class="error-hero-card">
    <div class="error-hero-badge">⚡</div>
    <div class="error-hero-code">500</div>
    <h2 class="error-hero-title">Trang Sách Tạm Thời Gián Đoạn</h2>
    <p class="error-hero-desc">
        Hệ thống gặp sự cố bất ngờ trong quá trình xử lý yêu cầu của bạn.
        Thông tin chi tiết đã được ghi nhận vào nhật ký máy chủ để khắc phục.
    </p>

    <div class="error-hero-actions">
        <button type="button" class="btn btn-primary" onclick="window.location.reload()">
            🔄 Thử tải lại
        </button>
        <a class="btn btn-ghost" href="${pageContext.request.contextPath}/">
            🏠 Về trang chủ
        </a>
    </div>
</div>
