<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
================================================================================
  views/error/404.jsp — Trang báo lỗi 404: Không tìm thấy trang
================================================================================
--%>
<div class="error-page-wrap" style="text-align:center; padding: 60px 20px; max-width: 600px; margin: 0 auto;">
    <div class="error-code-badge" style="font-size: 5.5rem; font-weight: 900; line-height: 1; letter-spacing: -2px; background: linear-gradient(135deg, var(--ember-lit, #f97316), var(--ember-deep, #ea580c)); -webkit-background-clip: text; -webkit-text-fill-color: transparent; margin-bottom: 16px;">
        404
    </div>
    <div style="font-size: 3rem; margin-bottom: 12px;">📖💨</div>
    <h2 style="font-size: 1.6rem; font-weight: 700; margin-bottom: 12px; color: var(--text, #f8fafc);">
        Trang này dường như đã bị lạc giữa các dòng truyện…
    </h2>
    <p style="color: var(--text-dim, #94a3b8); font-size: 1rem; line-height: 1.6; margin-bottom: 28px;">
        Đường dẫn bạn vừa truy cập có thể đã được đổi tên, bị tác giả gỡ bỏ, hoặc chưa từng tồn tại trên hệ thống.
    </p>

    <div style="display: inline-flex; gap: 12px; flex-wrap: wrap; justify-content: center;">
        <a href="${pageContext.request.contextPath}/" class="btn btn-primary" style="padding: 10px 22px; font-weight: 600;">
            🏠 Quay về Trang chủ
        </a>
        <a href="${pageContext.request.contextPath}/story?action=list" class="btn btn-ghost" style="padding: 10px 20px;">
            📚 Khám phá Kho truyện
        </a>
    </div>
</div>
