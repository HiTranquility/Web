<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
================================================================================
  views/error/500.jsp — Trang báo lỗi 500: Sự cố hệ thống
================================================================================
--%>
<div class="error-page-wrap" style="text-align:center; padding: 60px 20px; max-width: 600px; margin: 0 auto;">
    <div class="error-code-badge" style="font-size: 5.5rem; font-weight: 900; line-height: 1; letter-spacing: -2px; background: linear-gradient(135deg, #ef4444, #dc2626); -webkit-background-clip: text; -webkit-text-fill-color: transparent; margin-bottom: 16px;">
        500
    </div>
    <div style="font-size: 3rem; margin-bottom: 12px;">⚡🛠️</div>
    <h2 style="font-size: 1.6rem; font-weight: 700; margin-bottom: 12px; color: var(--text, #f8fafc);">
        Hệ thống đang gặp sự cố tạm thời
    </h2>
    <p style="color: var(--text-dim, #94a3b8); font-size: 1rem; line-height: 1.6; margin-bottom: 28px;">
        Máy chủ không thể xử lý yêu cầu lúc này. Chúng tôi đã ghi nhận và đang khắc phục. Bạn vui lòng thử lại sau giây lát nhé!
    </p>

    <div style="display: inline-flex; gap: 12px; flex-wrap: wrap; justify-content: center;">
        <a href="${pageContext.request.contextPath}/" class="btn btn-primary" style="padding: 10px 22px; font-weight: 600;">
            🏠 Quay về Trang chủ
        </a>
        <button type="button" onclick="window.location.reload();" class="btn btn-ghost" style="padding: 10px 20px;">
            🔄 Tải lại trang
        </button>
    </div>
</div>
