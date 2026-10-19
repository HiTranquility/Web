<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
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
            <c:choose>
                <c:when test="${discountPercent > 0}">
                    <div class="vip-price-group">
                        <del class="vip-orig-price"><fmt:formatNumber pattern="#,##0" value="${origPrice}"/> xu</del>
                        <span class="vip-coin-tag"><fmt:formatNumber pattern="#,##0" value="${unlockPrice}"/> xu</span>
                        <span class="badge-discount-tier">
                            <c:out value="${readerTierIcon}"/> -${discountPercent}% (<c:out value="${readerTierName}"/>)
                        </span>
                    </div>
                </c:when>
                <c:otherwise>
                    <span class="vip-coin-tag"><fmt:formatNumber pattern="#,##0" value="${unlockPrice}"/> xu</span>
                </c:otherwise>
            </c:choose>
        </div>
        <c:if test="${not empty sessionScope.currentUser}">
            <div class="vip-info-row">
                <span class="vip-info-label">Số dư ví của bạn:</span>
                <span class="vip-balance-tag"><fmt:formatNumber pattern="#,##0" value="${walletBalance != null ? walletBalance : 0}"/> xu</span>
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
            <form id="vipUnlockForm" action="${pageContext.request.contextPath}/chapter" method="post" class="vip-actions"
                  onsubmit="if(this.dataset.submitting) return false; this.dataset.submitting='1'; var b=this.querySelector('.vip-btn-unlock'); if(b){b.innerText='Đang mở khoá...';}">
                <input type="hidden" name="_csrf" value="${csrfToken}">
                <input type="hidden" name="action" value="unlock">
                <input type="hidden" name="id" value="${chapter.id}">
                <button type="button" class="btn btn-primary btn-lg vip-btn-unlock" onclick="openVipUnlockModal();">
                    🔓 Mở khoá chương ngay (<fmt:formatNumber pattern="#,##0" value="${unlockPrice}"/> xu)
                </button>
            </form>
        </c:when>

        <c:otherwise>
            <div class="vip-actions" style="display:flex; flex-direction:column; gap:12px; align-items:center;">
                <button type="button" class="btn btn-secondary btn-lg" disabled style="opacity:0.85; cursor:not-allowed;">
                    ⚠️ Số dư không đủ (Thiếu <fmt:formatNumber pattern="#,##0" value="${unlockPrice - walletBalance}"/> xu)
                </button>
                <a href="${pageContext.request.contextPath}/wallet?action=topup"
                   onclick="if(window.openQuickTopupModal){ window.openQuickTopupModal(${unlockPrice - walletBalance}, function(){ location.reload(); }); return false; }"
                   class="btn btn-warning btn-lg" style="font-weight:700; width:100%; max-width:320px; text-align:center;">
                    🪙 Nạp thêm xu vào ví ngay &rarr;
                </a>
            </div>
            <div class="vip-earn-tips text-center mt-3">
                <p class="text-amber">💡 <strong>Mẹo kiếm xu miễn phí:</strong> Bạn có thể điểm danh hàng ngày hoặc làm nhiệm vụ đọc truyện để nhận thêm xu!</p>
                <a href="${pageContext.request.contextPath}/user?action=me#gamification" class="btn btn-sm btn-ghost">
                    🎁 Đến Trạm Điểm danh &amp; Nhiệm vụ &rarr;
                </a>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%-- Hộp thoại xác nhận mở khoá chương VIP --%>
<%@ include file="/WEB-INF/views/_partials/_vip-unlock-modal.jsp" %>

