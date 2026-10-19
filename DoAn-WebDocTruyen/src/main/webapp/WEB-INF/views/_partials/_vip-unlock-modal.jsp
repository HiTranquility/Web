<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--
================================================================================
  _vip-unlock-modal.jsp — HỘP THOẠI XÁC NHẬN MỞ KHOÁ CHƯƠNG VIP
================================================================================
  Hiển thị chi tiết số xu trừ, chiết khấu cấp bậc thành viên và số dư sau khi mở.
  Tích hợp chuyển đổi mượt mà sang Nạp Xu Nhanh nếu số dư không đủ.
================================================================================
--%>
<div class="app-modal-backdrop" id="vipUnlockModalBackdrop" onclick="if(event.target===this) closeVipUnlockModal();">
    <div class="app-modal-card vip-unlock-card" role="dialog" aria-modal="true" aria-labelledby="vipUnlockTitle">
        <div class="app-modal-head">
            <div class="modal-title-wrap">
                <span class="modal-title-icon">🔓</span>
                <div>
                    <h3 id="vipUnlockTitle">Xác Nhận Mở Khoá Chương VIP</h3>
                    <p class="modal-subtitle">Mở khoá 1 lần duy nhất — Đọc vĩnh viễn không giới hạn</p>
                </div>
            </div>
            <button type="button" class="app-modal-close" onclick="closeVipUnlockModal()" aria-label="Đóng">&times;</button>
        </div>

        <div class="app-modal-body">
            <%-- Thông tin chương truyện --%>
            <div class="vip-unlock-info-card">
                <div class="unlock-story-title" id="vipModalStoryTitle"><c:out value="${story.title}"/></div>
                <div class="unlock-chapter-title" id="vipModalChapterTitle">
                    Chương ${chapter.chapterNo}: <c:out value="${chapter.title}"/>
                </div>
            </div>

            <%-- Bảng tính chi phí mở khoá --%>
            <div class="vip-breakdown-table">
                <div class="breakdown-row">
                    <span class="breakdown-label">Giá niêm yết:</span>
                    <span class="breakdown-val" id="vipModalOrigPrice">
                        <fmt:formatNumber pattern="#,##0" value="${origPrice != null ? origPrice : chapter.coinPrice}"/> xu
                    </span>
                </div>

                <c:if test="${discountPercent > 0}">
                    <div class="breakdown-row discount-row">
                        <span class="breakdown-label">
                            Ưu đãi cấp đọc giả (<c:out value="${readerTierName}"/>):
                        </span>
                        <span class="breakdown-val text-green" id="vipModalDiscount">
                            -${discountPercent}%
                        </span>
                    </div>
                </c:if>

                <div class="breakdown-divider"></div>

                <div class="breakdown-row highlight-row">
                    <span class="breakdown-label">Giá mở khoá thực tế:</span>
                    <span class="breakdown-val coin-highlight" id="vipModalUnlockPrice">
                        <b><fmt:formatNumber pattern="#,##0" value="${unlockPrice != null ? unlockPrice : chapter.coinPrice}"/></b> xu
                    </span>
                </div>

                <div class="breakdown-row">
                    <span class="breakdown-label">Số dư ví hiện tại:</span>
                    <span class="breakdown-val" id="vipModalBalance">
                        <fmt:formatNumber pattern="#,##0" value="${walletBalance != null ? walletBalance : 0}"/> xu
                    </span>
                </div>

                <div class="breakdown-row remaining-row">
                    <span class="breakdown-label">Số dư ví sau khi mở:</span>
                    <span class="breakdown-val" id="vipModalRemaining">
                        <c:choose>
                            <c:when test="${walletBalance >= unlockPrice}">
                                <b class="text-green"><fmt:formatNumber pattern="#,##0" value="${walletBalance - unlockPrice}"/> xu</b>
                            </c:when>
                            <c:otherwise>
                                <b class="text-danger">Không đủ xu</b>
                            </c:otherwise>
                        </c:choose>
                    </span>
                </div>
            </div>

            <%-- Cảnh báo nếu không đủ xu --%>
            <div id="vipModalShortageAlert" class="modal-status-alert status-warn"
                 style="${walletBalance < unlockPrice ? 'display:block;' : 'display:none;'}">
                ⚠️ Số dư ví của bạn không đủ để mở khoá chương này (Thiếu <b><fmt:formatNumber pattern="#,##0" value="${unlockPrice > walletBalance ? (unlockPrice - walletBalance) : 0}"/> xu</b>).
            </div>
        </div>

        <div class="app-modal-foot">
            <button type="button" class="btn btn-ghost" onclick="closeVipUnlockModal()">Huỷ bỏ</button>

            <%-- Nút hành động tương ứng với số dư --%>
            <c:choose>
                <c:when test="${walletBalance >= unlockPrice}">
                    <button type="button" class="btn btn-primary btn-lg" id="vipConfirmSubmitBtn" onclick="executeVipUnlock()">
                        🔓 Xác Nhận Mở Khoá (-<fmt:formatNumber pattern="#,##0" value="${unlockPrice}"/> xu)
                    </button>
                </c:when>
                <c:otherwise>
                    <button type="button" class="btn btn-warning btn-lg" id="vipConfirmTopupBtn"
                            onclick="closeVipUnlockModal(); openQuickTopupModal(${unlockPrice - walletBalance}, function(){ location.reload(); });">
                        🪙 Nạp Thêm Xu Ngay &rarr;
                    </button>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<script>
(function() {
    window.openVipUnlockModal = function() {
        var modal = document.getElementById('vipUnlockModalBackdrop');
        if (!modal) return;
        if (modal.parentElement !== document.body) {
            document.body.appendChild(modal);
        }
        modal.classList.add('is-open');
        document.body.style.overflow = 'hidden';
    };

    window.closeVipUnlockModal = function() {
        var modal = document.getElementById('vipUnlockModalBackdrop');
        if (!modal) return;
        modal.classList.remove('is-open');
        document.body.style.overflow = '';
    };

    window.executeVipUnlock = function() {
        var form = document.getElementById('vipUnlockForm');
        var btn = document.getElementById('vipConfirmSubmitBtn');
        if (btn) {
            btn.disabled = true;
            btn.innerHTML = '⏳ Đang mở khoá...';
        }
        if (form) {
            form.submit();
        }
    };

    // Đóng modal khi bấm phím Escape
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' || e.keyCode === 27) {
            var modal = document.getElementById('vipUnlockModalBackdrop');
            if (modal && modal.classList.contains('is-open')) {
                closeVipUnlockModal();
            }
        }
    });
})();
</script>

