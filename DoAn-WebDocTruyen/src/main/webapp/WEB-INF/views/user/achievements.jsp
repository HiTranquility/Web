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
    <div><span>Đã mở khóa</span><strong>${earnedCount} <small>/ ${achievements.size()}</small></strong></div>
    <div><span>Thưởng hằng ngày</span><strong>+${badgeBonus}%</strong><small>Huy hiệu · tối đa 20%</small></div>
    <div><span>Hỗ trợ đề xuất truyện</span><strong>+${recommendationBoost}%</strong><small>Điểm xếp hạng · tối đa 10%</small></div>
</div>
<section class="achievement-section" aria-labelledby="perks-heading">
    <div class="achievement-section-head">
        <div><p class="achievement-eyebrow">01 / TÍCH LŨY ĐẶC QUYỀN</p><h2 id="perks-heading">Huy hiệu của bạn</h2></div>
        <span class="achievement-note">Tự động áp dụng khi đạt</span>
    </div>
    <p class="muted">% huy hiệu cộng trên thưởng gốc, không nhân chồng lên VIP/cấp bậc. Xu làm tròn một lần khi nhận.
        Một chương được ghi nhận khi đã đọc ít nhất 30 giây và đến 80% nội dung.
        Chương sáng tác đủ nội dung cần từ 200 ký tự hoặc 3 trang ảnh.
        Uy tín hỗ trợ truyện phù hợp với độc giả; không đảm bảo vị trí hay lượt xem. Huy hiệu sáng tác có trọng số cao hơn.</p>
    <div class="achievement-grid">
        <c:forEach var="a" items="${achievements}">
            <article class="achievement-card ${a.unlocked ? 'is-earned' : 'is-locked'}">
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
            </article>
        </c:forEach>
    </div>
</section>
<section class="achievement-section" id="milestones" aria-labelledby="milestones-heading">
    <div class="achievement-section-head">
        <div><p class="achievement-eyebrow">02 / NHẬN XU THEO CỘT MỐC</p><h2 id="milestones-heading">Thành tựu nhận thưởng</h2></div>
        <a href="${pageContext.request.contextPath}/wallet">Ví xu của tôi →</a>
    </div>
    <p class="muted">Mỗi thành tựu nhận một lần cho tài khoản. Phần thưởng này không nhân theo huy hiệu hay VIP.</p>
    <div class="achievement-milestones">
        <c:forEach var="a" items="${achievements}">
            <c:if test="${a.rewardCoins gt 0}">
                <article class="achievement-milestone">
                    <span class="achievement-icon" aria-hidden="true"><c:out value="${a.icon}"/></span>
                    <div class="achievement-milestone-copy"><h3><c:out value="${a.title}"/></h3><p class="muted"><c:out value="${a.progressText}"/></p></div>
                    <strong class="achievement-coins">+${a.rewardCoins} xu</strong>
                    <c:choose>
                        <c:when test="${a.claimed}"><span class="achievement-claimed">✓ Đã nhận</span></c:when>
                        <c:when test="${a.claimable and rewardsAvailable}">
                            <form action="${pageContext.request.contextPath}/user" method="post">
                                <input type="hidden" name="_csrf" value="${csrfToken}">
                                <input type="hidden" name="action" value="claim-achievement">
                                <input type="hidden" name="key" value="${a.id}">
                                <button type="submit" class="btn btn-primary btn-sm">Nhận ${a.rewardCoins} xu</button>
                            </form>
                        </c:when>
                        <c:otherwise><span class="achievement-pending">${a.unlocked ? 'Chưa thể nhận' : 'Chưa đạt'}</span></c:otherwise>
                    </c:choose>
                </article>
            </c:if>
        </c:forEach>
    </div>
</section>
