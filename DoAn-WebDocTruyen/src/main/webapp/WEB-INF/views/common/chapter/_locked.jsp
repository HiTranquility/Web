<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
================================================================================
  chapter/_locked.jsp — Hàng rào chương VIP & Mở khoá bằng xu (ISSUE-020)
================================================================================
  Bảo mật 100%: Nội dung chương bị khoá hoàn toàn không tồn tại trong HTML
  (chapter.content = "" từ máy chủ).
================================================================================
--%>
<div class="vip-locked-card" id="vip-lock-container">
    <div class="vip-badge-icon" aria-hidden="true">🔒</div>
    <h2 class="vip-title">Chương VIP — Cần mở khoá</h2>
    <p class="vip-desc">
        Chương này là nội dung độc quyền được tác giả bảo hộ bản quyền.
        <br>Mở khoá <strong>1 lần duy nhất</strong> để đọc mãi mãi!
    </p>

    <div class="vip-price-box">
        <div class="vip-info-row">
            <span class="vip-info-label">Giá mở khoá:</span>
            <span class="vip-coin-tag"><c:out value="${unlockPrice}"/> Xu</span>
        </div>
        <c:if test="${not empty sessionScope.currentUser}">
            <div class="vip-info-row">
                <span class="vip-info-label">Số dư ví của bạn:</span>
                <span class="vip-balance-tag"><c:out value="${walletBalance}"/> Xu</span>
            </div>
        </c:if>
    </div>

    <c:choose>
        <c:when test="${empty sessionScope.currentUser}">
            <div class="vip-actions">
                <a href="${pageContext.request.contextPath}/auth?action=login" class="btn btn-primary btn-lg">
                    🔑 Đăng nhập để mở khoá
                </a>
            </div>
            <p class="muted small text-center mt-2">Chưa có tài khoản? <a href="${pageContext.request.contextPath}/auth?action=register">Đăng ký thành viên nhận ngay 100 xu!</a></p>
        </c:when>

        <c:when test="${walletBalance >= unlockPrice}">
            <form action="${pageContext.request.contextPath}/chapter" method="post" class="vip-actions"
                  onsubmit="if(this.dataset.submitting) return false; this.dataset.submitting='1'; var b=this.querySelector('.vip-btn-unlock'); if(b){b.innerText='Đang mở khoá...';}">
                <input type="hidden" name="_csrf" value="${csrfToken}">
                <input type="hidden" name="action" value="unlock">
                <input type="hidden" name="id" value="${chapter.id}">
                <button type="submit" class="btn btn-primary btn-lg vip-btn-unlock">
                    🔓 Mở khoá chương ngay (<c:out value="${unlockPrice}"/> xu)
                </button>
            </form>
        </c:when>

        <c:otherwise>
            <div class="vip-actions">
                <button type="button" class="btn btn-secondary btn-lg" disabled>
                    ⚠️ Số dư không đủ (Thiếu <c:out value="${unlockPrice - walletBalance}"/> xu)
                </button>
            </div>
            <div class="vip-earn-tips text-center mt-3">
                <p class="text-amber">💡 <strong>Mẹo kiếm xu:</strong> Bạn có thể điểm danh hàng ngày hoặc làm nhiệm vụ đọc truyện để nhận thêm xu miễn phí!</p>
                <a href="${pageContext.request.contextPath}/user?action=me" class="btn btn-sm btn-ghost">
                    🎁 Đến Trung tâm Nhiệm vụ &amp; Điểm danh &rarr;
                </a>
            </div>
        </c:otherwise>
    </c:choose>
</div>
