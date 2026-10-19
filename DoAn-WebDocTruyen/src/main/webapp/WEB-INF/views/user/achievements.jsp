<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<nav class="achievement-breadcrumb" aria-label="Đường dẫn">
    <a href="${pageContext.request.contextPath}/user?action=me">Hồ sơ của tôi</a><span aria-hidden="true">/</span><span>Thành tích &amp; Đặc quyền</span>
</nav>
<header class="achievement-hero">
    <div>
        <p class="achievement-eyebrow">HÀNH TRÌNH CỦA BẠN</p>
        <h1>Đọc, sáng tác.<br>Tích lũy thành tích.</h1>
        <p class="muted">Mỗi cột mốc mở thêm đặc quyền. Mỗi thành tựu là một phần thưởng riêng.</p>
        <a class="btn btn-ghost" href="${pageContext.request.contextPath}/user?action=me#gamification">Nhiệm vụ hôm nay →</a>
    </div>
    <div class="achievement-hero-mark" aria-hidden="true">🏅</div>
</header>
<c:if test="${not rewardsAvailable}">
    <p class="panel panel-warn">Bạn đang xem thử trang thành tích. Hiện chưa thể ghi nhận thành tích hoặc nhận xu.</p>
</c:if>
<div class="achievement-summary">
    <div><span>Đã mở khóa</span><strong>${earnedCount} <small>/ ${achievements.size()}</small></strong><small>Huy hiệu đạt được</small></div>
    <div><span>Xu thành tựu đã nhận</span><strong><span data-claimed-coins>${claimedCoins}</span> <small>xu</small></strong><small><span data-claimed-count>${claimedCount}</span> mốc đã nhận</small></div>
    <div><span>Thưởng hằng ngày</span><strong>+${badgeBonus}%</strong><small>Huy hiệu · tối đa 20%</small></div>
    <div><span>Hỗ trợ đề xuất truyện</span><strong>+${recommendationBoost}%</strong><small>Điểm xếp hạng · tối đa 10%</small></div>
</div>
<%@ include file="_reward-summary.jsp" %>
<section class="achievement-section" data-achievement-browser data-tab="all" data-view="grid" aria-labelledby="perks-heading">
    <h2 id="perks-heading">Huy hiệu &amp; thành tựu</h2>
    <p class="muted">Huy hiệu tự áp dụng khi đạt, cộng trên thưởng gốc và không nhân chồng lên VIP. Mỗi mốc xu chỉ nhận một lần.
        Đọc ít nhất 30 giây và đến 80% chương để ghi nhận. Chương sáng tác cần từ 200 ký tự hoặc 3 trang ảnh.</p>
    <p class="muted">Huy hiệu giúp tăng điểm đề xuất tối đa 10%; mốc sáng tác có trọng số cao hơn. Truyện vẫn cần phù hợp độc giả, không đảm bảo vị trí hay lượt xem.</p>
    <p><strong data-claimable-count>${claimableCount}</strong> thành tựu đủ điều kiện chưa nhận · <strong data-claimable-coins>${claimableCoins}</strong> xu đang chờ nhận · Đã nhận: <strong data-claimed-coins-sub>${claimedCoins}</strong> xu.</p>
    <p>Số dư ví: <strong data-wallet-balance>${walletBalance}</strong> xu · <a href="${pageContext.request.contextPath}/wallet">Mở ví →</a></p>
    <div data-browser-controls hidden>
        <div class="achievement-tabs" role="tablist" aria-label="Loại thành tích">
            <button type="button" id="badge-tab" role="tab" aria-selected="true" aria-controls="achievement-results" data-tab="perks">Huy hiệu</button>
            <button type="button" id="milestones-heading" role="tab" aria-selected="false" tabindex="-1" aria-controls="achievement-results" data-tab="milestones">Nhận xu</button>
        </div>
        <div class="achievement-toolbar">
            <label class="achievement-search">Tìm thành tựu<input type="search" data-search placeholder="Tên hoặc điều kiện, có dấu hay không dấu…" maxlength="150"></label>
            <label>Nhóm<select data-category><option value="">Tất cả nhóm</option></select></label>
            <label>Trạng thái<select data-status><option value="">Tất cả trạng thái</option><option value="earned">Đã mở khóa</option><option value="locked">Chưa đạt</option><option value="claimable">Có thể nhận xu</option><option value="claimed">Đã nhận xu</option></select></label>
            <label>Sắp xếp<select data-sort><option value="catalog">Theo hành trình</option><option value="progress">Gần hoàn thành</option><option value="coins">Xu cao nhất</option><option value="name">Tên A–Z</option></select></label>
        </div>
        <div class="achievement-browser-bar"><span data-result-count></span><div role="group" aria-label="Cách hiển thị"><button type="button" data-view="grid" aria-pressed="true">Lưới</button><button type="button" data-view="list" aria-pressed="false">Danh sách</button></div></div>
    </div>
    <p data-claim-message role="status" aria-live="polite" aria-atomic="true"></p>
    <div id="achievement-results" class="achievement-grid">
        <c:forEach var="a" items="${achievements}">
            <article data-achievement="${a.id}" data-category="<c:out value="${a.category}"/>" data-earned="${a.unlocked}" data-claimed="${a.claimed}" data-coins="${a.rewardCoins}" data-progress="${a.progressPercent}" class="achievement-card ${a.unlocked ? 'is-earned' : 'is-locked'}">
                <div class="achievement-card-top"><span class="achievement-icon" aria-hidden="true"><c:out value="${a.icon}"/></span>
                    <span class="achievement-status">${a.unlocked ? 'Đã mở khóa' : 'Đang tích lũy'}</span></div>
                <p class="achievement-category"><c:out value="${a.category}"/></p>
                <h3><c:out value="${a.title}"/></h3>
                <p class="achievement-description"><c:out value="${a.description}"/></p>
                <c:choose>
                    <c:when test="${a.id eq 'vip_patron'}"><p class="achievement-effects">Theo thời hạn VIP · không cộng uy tín</p></c:when>
                    <c:otherwise><p class="achievement-effects">+${a.bonusPercent}% thưởng ngày <span>·</span> +${a.recommendationWeight} điểm uy tín</p></c:otherwise>
                </c:choose>
                <label class="achievement-progress-label" for="badge-progress-${a.id}"><c:out value="${a.progressText}"/></label>
                <progress id="badge-progress-${a.id}" value="${a.progressPercent}" max="100">${a.progressPercent}%</progress>
                <c:if test="${a.rewardCoins gt 0}"><div class="achievement-reward"><strong class="achievement-coins">+${a.rewardCoins} xu</strong>
                    <c:choose>
                        <c:when test="${a.claimed}"><span class="achievement-claimed">✓ Đã nhận</span></c:when>
                        <c:when test="${a.claimable and rewardsAvailable}">
                            <form data-claim-achievement action="${pageContext.request.contextPath}/user" method="post">
                                <input type="hidden" name="_csrf" value="${csrfToken}">
                                <input type="hidden" name="action" value="claim-achievement">
                                <input type="hidden" name="key" value="${a.id}">
                                <button type="submit" class="btn btn-primary btn-sm">Nhận ${a.rewardCoins} xu</button>
                            </form>
                        </c:when>
                        <c:otherwise><span class="achievement-pending">${a.unlocked ? 'Chưa thể nhận' : 'Chưa đạt'}</span></c:otherwise>
                    </c:choose>                </div></c:if>
            </article>
        </c:forEach>
    </div>
    <p data-empty hidden>Không tìm thấy thành tựu phù hợp. <button type="button" data-reset>Xóa bộ lọc</button></p>
    <nav class="achievement-pagination" data-pagination hidden aria-label="Trang thành tựu">
        <button type="button" data-prev>← Trước</button><span data-page aria-live="polite"></span><button type="button" data-next>Tiếp →</button>
        <label>Mỗi trang <select data-page-size><option value="12">12</option><option value="24">24</option></select></label>
    </nav>
</section>
<script src="${pageContext.request.contextPath}/assets/js/achievements.js" defer></script>
