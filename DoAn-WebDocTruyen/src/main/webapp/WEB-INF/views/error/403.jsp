<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
================================================================================
  views/error/403.jsp — Trang báo lỗi 403: Không đủ quyền (bug-002)

  Nội dung lấy nguyên từ views/common/page/_error403.jsp cũ, chỉ đổi chỗ ở —
  ISSUE-016 làm 404 và 500 nhưng bỏ quên 403, nên web.xml phải giữ lại nguyên
  một bộ error-page cũ chỉ vì mã lỗi này. Đưa 403 về chung một cửa ErrorServlet
  thì bộ cũ mới xoá được.

  Dùng lại các lớp .error-hero-* đã có sẵn trong components.css, KHÔNG viết
  style nội tuyến — để đổi giao diện trang lỗi chỉ phải sửa một chỗ.
================================================================================
--%>
<div class="error-hero-card">
    <div class="error-hero-badge">🛡️</div>
    <div class="error-hero-code">403</div>
    <h2 class="error-hero-title">${csrfFailure ? 'Biểu mẫu đã hết hạn' : 'Khu Vực Giới Hạn Quyền'}</h2>
    <p class="error-hero-desc">
        <c:choose>
        <c:when test="${csrfFailure}">
        Phiên biểu mẫu không còn hợp lệ. Hãy mở lại trang cần thao tác rồi thử lại.
        </c:when>
        <c:otherwise>
        Trang này dành riêng cho quản trị viên hoặc tác giả sở hữu nội dung.
        Nếu bạn cho rằng đây là sự nhầm lẫn, hãy đăng nhập với tài khoản có quyền phù hợp.
        </c:otherwise>
        </c:choose>
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
                <form id="logout403Form" action="${pageContext.request.contextPath}/auth" method="post" style="display:none;">
                    <input type="hidden" name="action" value="logout">
                    <input type="hidden" name="_csrf" value="${csrfToken}">
                </form>
                <button type="submit" form="logout403Form" class="btn btn-ghost">
                    🔄 Đổi tài khoản
                </button>
            </c:otherwise>
        </c:choose>
    </div>
</div>
