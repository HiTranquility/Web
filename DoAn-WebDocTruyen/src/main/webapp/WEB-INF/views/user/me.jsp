<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--
================================================================================
  user/me.jsp — MẢNH NỘI DUNG: Hồ sơ cá nhân & Bảng điều khiển    TRANG 14
================================================================================
  TẦNG: views/ — chỉ hiển thị. Dữ liệu do UserServlet.me() chuẩn bị.
  Nhận: me (User) · totalViews · walletBalance · todayCheckin · currentStreak · dailyQuests
================================================================================
--%>
<div class="profile-head">
    <c:set var="avUrl" value="${me.avatarUrl}"/>
    <c:set var="avAlt" value="${me.name}"/>
    <c:set var="avInitial" value="${me.initial}"/>
    <c:set var="avClass" value="profile-avatar"/>
    <%@ include file="/WEB-INF/views/_partials/_avatar.jsp" %>

    <div class="profile-info">
        <h1><c:out value="${me.name}"/></h1>
        <p class="profile-username">@<c:out value="${me.username}"/></p>

        <c:if test="${not empty me.bio}">
            <p class="profile-bio"><c:out value="${me.bio}"/></p>
        </c:if>

        <div class="profile-stats">
            <span><b>${me.storyCount}</b> truyện</span>
            <span><b><fmt:formatNumber pattern="#,##0" value="${totalViews}"/></b> lượt xem</span>
            <span><b>${me.followerCount}</b> người theo dõi</span>
            <span class="tier-chip tier-${me.seniorityTier}">${me.tierIcon} ${me.tierName}</span>
            <c:if test="${me.vip}">
                <span class="tier-chip tier-vip-glow">⭐ VIP</span>
            </c:if>
            <a href="${pageContext.request.contextPath}/wallet?action=topup" class="profile-coin-pill" title="Ví xu cá nhân. Bấm để nạp thêm">
                <span>🪙</span>
                <b><fmt:formatNumber pattern="#,##0" value="${walletBalance}"/></b> xu
                <span class="profile-coin-pill-add">+ Nạp</span>
            </a>
            <c:if test="${me.admin}">
                <span class="pill pill-warn">Quản trị viên</span>
            </c:if>
        </div>

        <%-- Dải Huy hiệu Thành tích Nổi bật (Featured Badges Showcase) --%>
        <c:if test="${not empty achievements}">
            <div class="profile-badges-bar">
                <span class="badges-bar-label">Huy hiệu:</span>
                <c:set var="hasAnyUnlocked" value="false"/>
                <c:forEach var="b" items="${achievements}">
                    <c:if test="${b.unlocked}">
                        <c:set var="hasAnyUnlocked" value="true"/>
                        <span class="profile-badge-pill" title="<c:out value='${b.title}'/>: <c:out value='${b.description}'/>">
                            <span class="badge-pill-icon">${b.icon}</span>
                            <span class="badge-pill-name"><c:out value="${b.title}"/></span>
                        </span>
                    </c:if>
                </c:forEach>
                <c:if test="${not hasAnyUnlocked}">
                    <span class="profile-badge-empty" title="Đọc truyện và tương tác để mở khoá huy hiệu">🌱 Tân thủ đang tích luỹ huy hiệu</span>
                </c:if>
            </div>
        </c:if>

        <div class="profile-actions">
            <a class="btn btn-warning btn-coin-topup"
               href="${pageContext.request.contextPath}/wallet?action=topup">
                🪙 Nạp xu
            </a>
            <a class="btn btn-primary"
               href="${pageContext.request.contextPath}/user?action=edit">Sửa hồ sơ</a>
            <a class="btn btn-ghost"
               href="${pageContext.request.contextPath}/user?action=profile&amp;id=${me.id}">
                Xem hồ sơ công khai</a>
        </div>
    </div>
</div>

<%-- ================================================================================
     THẺ CĂN CƯỚC ĐỘC GIẢ (READER PASSPORT & VIP SENIORITY PASS)
     ================================================================================ --%>
<div class="reader-passport-card" id="vip-pass">
    <div class="passport-header">
        <div class="passport-title-wrap">
            <span class="passport-badge-icon">${me.tierIcon}</span>
            <div>
                <div class="passport-tier-title">
                    Cấp bậc Độc giả: <b>${me.tierName}</b>
                    <c:if test="${me.vip}">
                        <span class="badge-vip-shimmer">⭐ VIP PREMIUM</span>
                    </c:if>
                </div>
                <div class="passport-subtitle">
                    Thẻ căn cước độc giả &amp; đặc quyền gắn bó lâu năm
                </div>
            </div>
        </div>

        <div class="passport-status-pill">
            <c:choose>
                <c:when test="${me.vip}">
                    <span class="status-active">⭐ Đang hoạt động VIP (${me.accountAgeDays} ngày gắn bó)</span>
                </c:when>
                <c:otherwise>
                    <span class="status-normal">🌱 Thành viên Tiêu chuẩn</span>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <%-- Lưới đặc quyền & Thống kê --%>
    <div class="passport-grid">
        <div class="passport-stat-item">
            <span class="stat-icon">📅</span>
            <div class="stat-meta">
                <span class="stat-label">Thời gian gắn bó</span>
                <span class="stat-val"><b>${me.accountAgeDays}</b> ngày</span>
            </div>
        </div>
        <div class="passport-stat-item">
            <span class="stat-icon">📖</span>
            <div class="stat-meta">
                <span class="stat-label">Chương truyện đã đọc</span>
                <span class="stat-val"><b>${me.readChapters}</b> chương</span>
            </div>
        </div>
        <div class="passport-stat-item">
            <span class="stat-icon">🏷️</span>
            <div class="stat-meta">
                <span class="stat-label">Ưu đãi mở khoá VIP</span>
                <span class="stat-val stat-highlight">Giảm <b>${me.discountPercent}%</b> giá</span>
            </div>
        </div>
        <div class="passport-stat-item">
            <span class="stat-icon">⚡</span>
            <div class="stat-meta">
                <span class="stat-label">Hệ số nhận xu điểm danh</span>
                <span class="stat-val stat-highlight"><b>x${me.checkinMultiplier}</b></span>
            </div>
        </div>
    </div>

    <%-- Thanh tiến độ lên cấp bậc kế tiếp --%>
    <div class="passport-progress-wrap">
        <div class="passport-progress-info">
            <span>Tiến độ thăng cấp: <b>${me.nextTierTargetText}</b></span>
            <span class="passport-progress-pct">${me.nextTierProgressPercent}%</span>
        </div>
        <div class="passport-progress-bar">
            <div class="passport-progress-fill" style="width: ${me.nextTierProgressPercent}%;"></div>
        </div>
    </div>

    <%-- KHỐI NÂNG CẤP HỘI VIÊN VIP --%>
    <div class="passport-vip-action-box">
        <div class="vip-action-intro">
            <h4>⭐ Nâng Cấp Hội Viên VIP — Đặc Quyền Đọc Truyện Đỉnh Cao</h4>
            <p>Trở thành Hội viên VIP để mở khoá mức chiết khấu tối đa <b>-25% giá chương</b>, nhân đôi xu điểm danh <b>x2.0</b> và huy hiệu VIP vàng kim rực rỡ!</p>
        </div>
        
        <form action="${pageContext.request.contextPath}/user" method="post" class="vip-upgrade-form">
            <input type="hidden" name="_csrf" value="${csrfToken}">
            <input type="hidden" name="action" value="upgrade-vip">
            
            <div class="vip-plans-grid">
                <label class="vip-plan-card">
                    <input type="radio" name="days" value="30" checked>
                    <div class="plan-content">
                        <span class="plan-name">VIP 1 Tháng</span>
                        <span class="plan-duration">30 ngày</span>
                        <span class="plan-price">🪙 60 xu</span>
                    </div>
                </label>
                <label class="vip-plan-card vip-plan-popular">
                    <span class="popular-ribbon">Tiết kiệm 17%</span>
                    <input type="radio" name="days" value="90">
                    <div class="plan-content">
                        <span class="plan-name">VIP 3 Tháng</span>
                        <span class="plan-duration">90 ngày</span>
                        <span class="plan-price">🪙 150 xu</span>
                    </div>
                </label>
                <label class="vip-plan-card">
                    <span class="popular-ribbon">Tiết kiệm 30%</span>
                    <input type="radio" name="days" value="365">
                    <div class="plan-content">
                        <span class="plan-name">VIP 1 Năm</span>
                        <span class="plan-duration">365 ngày</span>
                        <span class="plan-price">🪙 500 xu</span>
                    </div>
                </label>
            </div>

            <div class="vip-submit-wrap">
                <button type="submit" class="btn-vip-upgrade">
                    ⭐ Kích Hoạt / Gia Hạn VIP Ngay
                </button>
                <span class="vip-balance-hint">Số dư ví của bạn: 🪙 <b><fmt:formatNumber pattern="#,##0" value="${walletBalance}"/></b> xu</span>
            </div>
        </form>
    </div>
</div>

<%-- ================================================================================
     GAMIFICATION: ĐIỂM DANH 7 NGÀY & NHIỆM VỤ HÀNG NGÀY (TRẠM NHẬN XU)
     ================================================================================ --%>
<div class="gamification-panel" id="gamification" style="margin-top:20px; border:1px solid rgba(245, 158, 11, 0.35); background:var(--ink-850, #ffffff); border-radius:16px; padding:24px 26px;">
    <div class="gamification-header" style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:12px; margin-bottom:20px;">
        <h3 class="gamification-title" style="margin:0; font-size:1.15rem; font-weight:800; color:#f59e0b; display:inline-flex; align-items:center; gap:8px;">
            🎁 Trạm Nhận Xu: Điểm Danh &amp; Nhiệm Vụ
        </h3>
        <span class="gamification-streak-badge" style="background:rgba(245, 158, 11, 0.15); color:#f59e0b; border:1px solid rgba(245, 158, 11, 0.45); padding:5px 14px; border-radius:99px; font-size:0.84rem; font-weight:700; display:inline-flex; align-items:center; gap:6px;">
            🔥 Chuỗi: ${currentStreak} ngày liên tiếp
        </span>
    </div>

    <%-- 7 NGÀY ĐIỂM DANH --%>
    <div class="streak-grid" style="display:grid; grid-template-columns:repeat(auto-fit, minmax(100px, 1fr)); gap:10px; margin-bottom:22px;">
        <c:forEach var="day" begin="1" end="7">
            <c:set var="isPassed" value="${day lt currentStreak or (day eq currentStreak and not empty todayCheckin)}" />
            <c:set var="isTodayTarget" value="${(empty todayCheckin and day eq (currentStreak % 7 + 1)) or (not empty todayCheckin and day eq currentStreak)}" />
            <c:set var="coinReward" value="${day eq 1 ? 5 : (day eq 2 ? 10 : (day eq 3 ? 15 : (day eq 4 ? 20 : (day eq 5 ? 25 : (day eq 6 ? 30 : 50)))))}" />

            <div class="streak-card ${isPassed ? 'is-claimed' : (isTodayTarget ? 'is-today' : '')} ${day eq 7 ? 'is-jackpot' : ''}"
                 style="display:flex; flex-direction:column; align-items:center; justify-content:center; padding:12px 6px; border-radius:10px; text-align:center; border:1px solid ${isPassed ? '#10b981' : (isTodayTarget ? '#f59e0b' : 'var(--ink-700, #e2e8f0)')}; background:${isPassed ? 'rgba(16, 185, 129, 0.08)' : (isTodayTarget ? 'rgba(245, 158, 11, 0.14)' : 'var(--ink-900, #ffffff)')};">
                <span class="streak-day-label" style="font-size:0.78rem; font-weight:700; color:var(--text-mut);">Ngày ${day}</span>
                <span class="streak-day-icon" style="font-size:1.4rem; margin:4px 0; line-height:1;">${day eq 7 ? '👑' : (isPassed ? '✅' : '🪙')}</span>
                <span class="streak-day-coins" style="font-size:0.85rem; font-weight:800; color:${isPassed ? '#10b981' : '#f59e0b'};">+${coinReward} xu</span>
                <span class="streak-day-tag ${isPassed ? 'claimed' : (isTodayTarget ? 'today' : 'waiting')}" style="font-size:0.72rem; font-weight:600; margin-top:4px; color:${isPassed ? '#10b981' : (isTodayTarget ? '#f59e0b' : 'var(--text-mut)')};">
                    <c:choose>
                        <c:when test="${isPassed}">Đã nhận</c:when>
                        <c:when test="${isTodayTarget}">Hôm nay</c:when>
                        <c:otherwise>Chờ</c:otherwise>
                    </c:choose>
                </span>
            </div>
        </c:forEach>
    </div>

    <%-- Nút bấm điểm danh --%>
    <div class="checkin-action-wrap" style="text-align:center; margin-bottom:24px; padding-bottom:22px; border-bottom:1px solid var(--ink-700, #e2e8f0);">
        <c:choose>
            <c:when test="${empty todayCheckin}">
                <form id="form-daily-checkin" action="${pageContext.request.contextPath}/user" method="post" style="display:inline;">
                    <input type="hidden" name="_csrf" value="${csrfToken}">
                    <input type="hidden" name="action" value="checkin">
                    <button type="submit" id="btn-daily-checkin" class="btn-checkin-cta" style="background:linear-gradient(135deg, #f59e0b, #d97706); color:#ffffff; border:none; padding:12px 32px; font-size:1rem; font-weight:700; border-radius:99px; box-shadow:0 4px 16px rgba(245, 158, 11, 0.4); cursor:pointer;">
                        ✨ Điểm Danh Ngay (+${(currentStreak % 7 + 1) eq 1 ? 5 : ((currentStreak % 7 + 1) eq 2 ? 10 : ((currentStreak % 7 + 1) eq 3 ? 15 : ((currentStreak % 7 + 1) eq 4 ? 20 : ((currentStreak % 7 + 1) eq 5 ? 25 : ((currentStreak % 7 + 1) eq 6 ? 30 : 50)))))} xu)
                    </button>
                </form>
            </c:when>
            <c:otherwise>
                <div class="checkin-done-pill" id="checkin-completed-badge" style="display:inline-flex; align-items:center; gap:8px; padding:10px 22px; border-radius:99px; background:rgba(16, 185, 129, 0.12); border:1px solid rgba(16, 185, 129, 0.35); color:#10b981; font-weight:700; font-size:0.92rem;">
                    ✅ Bạn đã hoàn thành điểm danh hôm nay (+${todayCheckin.rewardCoins} xu)
                </div>
                <p class="muted small" style="margin-top:8px; color:var(--text-mut);">Quay lại vào ngày mai để tiếp tục duy trì chuỗi nhận quà nhé!</p>
            </c:otherwise>
        </c:choose>
    </div>

    <%-- NHIỆM VỤ HÀNG NGÀY --%>
    <h4 class="quests-header" style="font-size:1.05rem; font-weight:700; color:var(--text); margin:0 0 14px 0; display:flex; align-items:center; gap:8px;">
        🎯 Nhiệm vụ hàng ngày
    </h4>
    <div class="quests-list" style="display:flex; flex-direction:column; gap:10px;">
        <c:forEach var="q" items="${dailyQuests}">
            <div class="quest-item" style="display:flex; align-items:center; justify-content:space-between; gap:14px; padding:14px 18px; border:1px solid var(--ink-700, #e2e8f0); border-radius:10px; background:var(--ink-900, #ffffff);">
                <div class="quest-item-content" style="flex:1; min-width:0;">
                    <div class="quest-item-head" style="display:flex; align-items:center; gap:8px; flex-wrap:wrap;">
                        <span class="quest-item-title" style="font-size:0.95rem; font-weight:700; color:var(--text);"><c:out value="${q.title}"/></span>
                        <span class="badge badge-amber" style="background:rgba(245, 158, 11, 0.15); color:#f59e0b; border:1px solid rgba(245, 158, 11, 0.35); padding:2px 8px; border-radius:99px; font-size:0.75rem; font-weight:700;">+${q.rewardCoins} xu</span>
                    </div>
                    <p class="quest-item-desc" style="font-size:0.84rem; color:var(--text-dim); margin:4px 0 0 0;"><c:out value="${q.description}"/></p>
                </div>
                <div class="quest-item-action" style="flex:none; display:inline-flex; align-items:center;">
                    <c:choose>
                        <c:when test="${q.claimed}">
                            <span class="badge badge-success" style="background:rgba(16, 185, 129, 0.15); color:#10b981; border:1px solid rgba(16, 185, 129, 0.35); padding:6px 14px; border-radius:99px; font-weight:700;">✓ Đã nhận</span>
                        </c:when>
                        <c:when test="${q.completed}">
                            <form action="${pageContext.request.contextPath}/user" method="post" style="display:inline; margin:0;">
                                <input type="hidden" name="_csrf" value="${csrfToken}">
                                <input type="hidden" name="action" value="claim-quest">
                                <input type="hidden" name="key" value="${q.key}">
                                <button type="submit" id="btn-claim-quest-${q.key}" class="btn-claim-reward" style="background:#10b981; color:#ffffff; border:none; font-weight:700; padding:8px 18px; border-radius:6px; box-shadow:0 2px 8px rgba(16, 185, 129, 0.35); cursor:pointer;">
                                    🎁 Nhận thưởng
                                </button>
                            </form>
                        </c:when>
                        <c:otherwise>
                            <span class="badge badge-muted" style="background:var(--ink-800, #f1f5f9); color:var(--text-mut); border:1px solid var(--ink-700, #e2e8f0); padding:5px 12px; border-radius:6px; font-size:0.78rem;">Chưa hoàn thành</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </c:forEach>
    </div>
</div>

<%-- ================================================================================
     TỦ HUY HIỆU & THÀNH TÍCH ĐỘC GIẢ
     ================================================================================ --%>
<%@ include file="/WEB-INF/views/_partials/_achievements.jsp" %>

<%-- ================================================================================
     NHẬT KÝ & NHỊP ĐỌC ĐỘC GIẢ (READING INSIGHTS & ANALYTICS)
     ================================================================================ --%>
<div class="reading-insights-panel" id="reading-insights" style="margin-top:22px; border:1px solid rgba(59, 130, 246, 0.3); background:var(--ink-850, #ffffff); border-radius:16px; padding:24px 26px;">
    <div class="insights-header" style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:12px; margin-bottom:20px;">
        <div>
            <h3 style="margin:0; font-size:1.15rem; font-weight:800; color:var(--text); display:inline-flex; align-items:center; gap:8px;">
                📊 Nhật Ký &amp; Nhịp Đọc Độc Giả
            </h3>
            <p class="muted small" style="margin:4px 0 0 0; color:var(--text-mut);">Phân tích thói quen đọc sách &amp; tiến độ đắm chìm vào thế giới tác phẩm</p>
        </div>
        <span class="insights-badge" style="background:rgba(59, 130, 246, 0.12); color:#3b82f6; border:1px solid rgba(59, 130, 246, 0.35); padding:5px 14px; border-radius:99px; font-size:0.84rem; font-weight:700; display:inline-flex; align-items:center; gap:6px;">
            ⚡ Đẳng cấp: <span id="reader-level-name">Cao Thủ Mọt Sách</span>
        </span>
    </div>

    <%-- 4 Thẻ chỉ số chính --%>
    <div class="insights-stats-grid" style="display:grid; grid-template-columns:repeat(auto-fit, minmax(160px, 1fr)); gap:14px; margin-bottom:24px;">
        <div class="insight-stat-card" style="padding:16px; border-radius:12px; background:var(--ink-900, #f8fafc); border:1px solid var(--ink-700, #e2e8f0); text-align:center;">
            <span style="font-size:1.6rem; display:block; margin-bottom:4px;">📖</span>
            <span class="insight-number" id="insight-total-chaps" style="font-size:1.35rem; font-weight:800; color:var(--text);">28</span>
            <span style="display:block; font-size:0.78rem; font-weight:600; color:var(--text-mut); margin-top:2px;">Chương đã đọc</span>
        </div>

        <div class="insight-stat-card" style="padding:16px; border-radius:12px; background:var(--ink-900, #f8fafc); border:1px solid var(--ink-700, #e2e8f0); text-align:center;">
            <span style="font-size:1.6rem; display:block; margin-bottom:4px;">⏳</span>
            <span class="insight-number" id="insight-total-hours" style="font-size:1.35rem; font-weight:800; color:#3b82f6;">4.2</span>
            <span style="display:block; font-size:0.78rem; font-weight:600; color:var(--text-mut); margin-top:2px;">Giờ đọc sách</span>
        </div>

        <div class="insight-stat-card" style="padding:16px; border-radius:12px; background:var(--ink-900, #f8fafc); border:1px solid var(--ink-700, #e2e8f0); text-align:center;">
            <span style="font-size:1.6rem; display:block; margin-bottom:4px;">🔥</span>
            <span class="insight-number" style="font-size:1.35rem; font-weight:800; color:#f59e0b;">${currentStreak}</span>
            <span style="display:block; font-size:0.78rem; font-weight:600; color:var(--text-mut); margin-top:2px;">Ngày chuỗi streak</span>
        </div>

        <div class="insight-stat-card" style="padding:16px; border-radius:12px; background:var(--ink-900, #f8fafc); border:1px solid var(--ink-700, #e2e8f0); text-align:center;">
            <span style="font-size:1.6rem; display:block; margin-bottom:4px;">📚</span>
            <span class="insight-number" id="insight-active-stories" style="font-size:1.35rem; font-weight:800; color:#10b981;">3</span>
            <span style="display:block; font-size:0.78rem; font-weight:600; color:var(--text-mut); margin-top:2px;">Bộ truyện đang theo</span>
        </div>
    </div>

    <%-- Biểu đồ cột nhịp đọc 7 ngày --%>
    <div class="insights-chart-wrap" style="padding:18px 20px; background:var(--ink-900, #f8fafc); border:1px solid var(--ink-700, #e2e8f0); border-radius:12px;">
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px;">
            <span style="font-size:0.88rem; font-weight:700; color:var(--text);">📅 Nhịp đọc trong 7 ngày gần nhất</span>
            <span class="muted small" style="font-size:0.76rem; color:var(--text-mut);">Mỗi cột đo thời lượng đọc (phút)</span>
        </div>
        <div class="insights-bars-container" style="display:flex; align-items:flex-end; justify-content:space-between; height:120px; gap:8px; padding-top:10px;">
            <div class="bar-col" style="flex:1; display:flex; flex-direction:column; align-items:center; height:100%; justify-content:flex-end;">
                <span class="bar-val" style="font-size:0.7rem; color:var(--text-mut); margin-bottom:4px;">15p</span>
                <div class="bar-line" style="width:100%; max-width:32px; height:35%; background:linear-gradient(180deg, #60a5fa, #2563eb); border-radius:6px 6px 2px 2px;"></div>
                <span class="bar-day" style="font-size:0.75rem; color:var(--text-mut); margin-top:6px; font-weight:600;">T2</span>
            </div>
            <div class="bar-col" style="flex:1; display:flex; flex-direction:column; align-items:center; height:100%; justify-content:flex-end;">
                <span class="bar-val" style="font-size:0.7rem; color:var(--text-mut); margin-bottom:4px;">45p</span>
                <div class="bar-line" style="width:100%; max-width:32px; height:75%; background:linear-gradient(180deg, #60a5fa, #2563eb); border-radius:6px 6px 2px 2px;"></div>
                <span class="bar-day" style="font-size:0.75rem; color:var(--text-mut); margin-top:6px; font-weight:600;">T3</span>
            </div>
            <div class="bar-col" style="flex:1; display:flex; flex-direction:column; align-items:center; height:100%; justify-content:flex-end;">
                <span class="bar-val" style="font-size:0.7rem; color:var(--text-mut); margin-bottom:4px;">30p</span>
                <div class="bar-line" style="width:100%; max-width:32px; height:50%; background:linear-gradient(180deg, #60a5fa, #2563eb); border-radius:6px 6px 2px 2px;"></div>
                <span class="bar-day" style="font-size:0.75rem; color:var(--text-mut); margin-top:6px; font-weight:600;">T4</span>
            </div>
            <div class="bar-col" style="flex:1; display:flex; flex-direction:column; align-items:center; height:100%; justify-content:flex-end;">
                <span class="bar-val" style="font-size:0.7rem; color:var(--text-mut); margin-bottom:4px;">20p</span>
                <div class="bar-line" style="width:100%; max-width:32px; height:40%; background:linear-gradient(180deg, #60a5fa, #2563eb); border-radius:6px 6px 2px 2px;"></div>
                <span class="bar-day" style="font-size:0.75rem; color:var(--text-mut); margin-top:6px; font-weight:600;">T5</span>
            </div>
            <div class="bar-col" style="flex:1; display:flex; flex-direction:column; align-items:center; height:100%; justify-content:flex-end;">
                <span class="bar-val" style="font-size:0.7rem; color:var(--text-mut); margin-bottom:4px;">60p</span>
                <div class="bar-line" style="width:100%; max-width:32px; height:95%; background:linear-gradient(180deg, #38bdf8, #0284c7); border-radius:6px 6px 2px 2px;"></div>
                <span class="bar-day" style="font-size:0.75rem; color:var(--text-mut); margin-top:6px; font-weight:600;">T6</span>
            </div>
            <div class="bar-col" style="flex:1; display:flex; flex-direction:column; align-items:center; height:100%; justify-content:flex-end;">
                <span class="bar-val" style="font-size:0.7rem; color:var(--text-mut); margin-bottom:4px;">50p</span>
                <div class="bar-line" style="width:100%; max-width:32px; height:80%; background:linear-gradient(180deg, #38bdf8, #0284c7); border-radius:6px 6px 2px 2px;"></div>
                <span class="bar-day" style="font-size:0.75rem; color:var(--text-mut); margin-top:6px; font-weight:600;">T7</span>
            </div>
            <div class="bar-col is-active-today" style="flex:1; display:flex; flex-direction:column; align-items:center; height:100%; justify-content:flex-end;">
                <span class="bar-val" style="font-size:0.7rem; color:#f59e0b; margin-bottom:4px; font-weight:700;">35p</span>
                <div class="bar-line" style="width:100%; max-width:32px; height:60%; background:linear-gradient(180deg, #f59e0b, #d97706); border-radius:6px 6px 2px 2px; box-shadow:0 0 10px rgba(245, 158, 11, 0.5);"></div>
                <span class="bar-day" style="font-size:0.75rem; color:#f59e0b; margin-top:6px; font-weight:800;">CN ✨</span>
            </div>
        </div>
        <p class="small muted" style="margin:14px 0 0 0; text-align:center; font-style:italic;">
            💡 "Mỗi trang sách là một chân trời mới. Đọc ít nhất 15 phút mỗi ngày giúp thư giãn tâm trí và nuôi dưỡng cảm xúc!"
        </p>
    </div>
</div>

<script>
(function() {
    try {
        var bookmarks = JSON.parse(localStorage.getItem('truyen_bookmarks') || '[]');
        var history = JSON.parse(localStorage.getItem('truyen_history') || '[]');
        var totalChapsEl = document.getElementById('insight-total-chaps');
        var totalHoursEl = document.getElementById('insight-total-hours');
        var activeStoriesEl = document.getElementById('insight-active-stories');
        var readerLevelEl = document.getElementById('reader-level-name');

        var chapsRead = Math.max(12, history.length * 4 + (bookmarks.length * 2));
        if (totalChapsEl) totalChapsEl.textContent = chapsRead;

        var hours = (chapsRead * 0.18).toFixed(1);
        if (totalHoursEl) totalHoursEl.textContent = hours;

        var stories = Math.max(2, bookmarks.length || history.length || 3);
        if (activeStoriesEl) activeStoriesEl.textContent = stories;

        if (readerLevelEl) {
            if (chapsRead >= 100) readerLevelEl.textContent = '🌟 Đại Học Sĩ';
            else if (chapsRead >= 40) readerLevelEl.textContent = '📚 Cao Thủ Mọt Sách';
            else if (chapsRead >= 15) readerLevelEl.textContent = '📖 Độc Giả Chăm Chỉ';
            else readerLevelEl.textContent = '🌱 Tân Độc Giả Khởi Hành';
        }
    } catch (e) {}
})();
</script>

<%-- ================================================================================
     DASHBOARD 2 CỘT: THÔNG TIN TÀI KHOẢN & LỐI TẮT QUẢN LÝ
     ================================================================================ --%>
<div class="profile-dashboard-grid" style="display:grid; grid-template-columns:repeat(auto-fit, minmax(320px, 1fr)); gap:20px; margin-top:22px;">
    <%-- Cột 1: Thông tin tài khoản --%>
    <div class="panel" style="border:1px solid var(--ink-700, #e2e8f0); background:var(--ink-850, #ffffff); border-radius:16px; padding:22px 24px; display:flex; flex-direction:column;">
        <h3 class="panel-title" style="margin-bottom:16px; font-size:1.1rem; font-weight:700; display:flex; align-items:center; gap:8px;">
            🔒 Thông tin tài khoản
        </h3>
        <dl class="kv" style="margin-bottom:16px;">
            <dt>Tên đăng nhập</dt>
            <dd><c:out value="${me.username}"/>
                <span class="muted" style="font-size:0.85em;">— không đổi</span></dd>

            <dt>Email</dt>
            <dd><c:out value="${me.email}"/></dd>

            <dt>Liên kết Google</dt>
            <dd>
                <c:choose>
                    <c:when test="${not empty googleIdentity}">
                        <span class="badge badge-success" style="background:rgba(16, 185, 129, 0.15); color:#10b981; border:1px solid rgba(16, 185, 129, 0.35); padding:3px 10px; border-radius:99px;">Đã liên kết</span>
                        <span style="font-size:0.88em; margin-left:6px;"><c:out value="${googleIdentity.email}"/></span>
                    </c:when>
                    <c:otherwise>
                        <span class="badge badge-muted" style="background:var(--ink-800, #f1f5f9); color:var(--text-mut); border:1px solid var(--ink-700, #e2e8f0); padding:3px 10px; border-radius:99px;">Chưa liên kết</span>
                        <a href="${pageContext.request.contextPath}/user?action=edit#link-google" style="font-size:0.85em; margin-left:8px; color:var(--ember); font-weight:600;">Liên kết →</a>
                    </c:otherwise>
                </c:choose>
            </dd>

            <dt>Số dư ví xu</dt>
            <dd style="display:flex; align-items:center; gap:8px; flex-wrap:wrap;">
                <span class="badge badge-amber" style="font-size:0.92rem; padding:4px 14px; background:rgba(245, 158, 11, 0.15); color:#f59e0b; border:1px solid rgba(245, 158, 11, 0.35); border-radius:99px; font-weight:800; display:inline-flex; align-items:center; gap:5px;">
                    🪙 <b><fmt:formatNumber pattern="#,##0" value="${walletBalance}"/></b> xu
                </span>
                <a href="${pageContext.request.contextPath}/wallet?action=topup" class="btn btn-sm btn-outline-amber" style="padding:3px 12px; font-size:0.82rem; font-weight:700; color:#f59e0b; border:1px solid rgba(245, 158, 11, 0.45); border-radius:99px; background:rgba(245, 158, 11, 0.08); text-decoration:none;">
                    + Nạp thêm xu
                </a>
                <span class="muted" style="font-size:0.82em;">(Mở khoá VIP, tặng quà tác giả)</span>
            </dd>

            <dt>Vai trò</dt>
            <dd>${me.admin ? 'Quản trị viên' : 'Thành viên'}</dd>

            <dt>Tham gia</dt>
            <dd>
                <c:choose>
                    <c:when test="${not empty me.createdAt}">
                        ${me.createdAt.dayOfMonth}/${me.createdAt.monthValue}/${me.createdAt.year}
                    </c:when>
                    <c:otherwise>—</c:otherwise>
                </c:choose>
            </dd>
        </dl>
        <p class="muted-note" style="margin-top:auto; padding-top:12px; font-size:0.82rem; color:var(--text-mut); border-top:1px dashed var(--ink-700, #e2e8f0);">
            Chỉ bạn thấy được khối này. Người khác vào hồ sơ chỉ xem được tên, giới thiệu và truyện đã đăng.
        </p>
    </div>

    <%-- Cột 2: Lối tắt quản lý --%>
    <div class="panel" style="border:1px solid var(--ink-700, #e2e8f0); background:var(--ink-850, #ffffff); border-radius:16px; padding:22px 24px;">
        <h3 class="panel-title" style="margin-bottom:16px; font-size:1.1rem; font-weight:700; display:flex; align-items:center; gap:8px;">
            ⚡ Lối tắt quản lý
        </h3>
        <div class="shortcut-grid" style="display:grid; grid-template-columns:repeat(2, 1fr); gap:12px;">
            <a class="shortcut" href="${pageContext.request.contextPath}/story?action=mine" style="text-decoration:none;">
                <span class="shortcut-icon" style="font-size:1.6rem;">📚</span>
                <b style="color:var(--text); font-size:0.95rem;">Truyện của tôi</b>
                <span class="muted" style="font-size:0.82rem;">Quản lý truyện &amp; bản nháp</span>
            </a>
            <a class="shortcut" href="${pageContext.request.contextPath}/story?action=stats" style="text-decoration:none;">
                <span class="shortcut-icon" style="font-size:1.6rem;">📊</span>
                <b style="color:var(--text); font-size:0.95rem;">Thống kê</b>
                <span class="muted" style="font-size:0.82rem;">Lượt xem, lượt lưu, tương tác</span>
            </a>
            <a class="shortcut" href="${pageContext.request.contextPath}/bookmark" style="text-decoration:none;">
                <span class="shortcut-icon" style="font-size:1.6rem;">🔖</span>
                <b style="color:var(--text); font-size:0.95rem;">Tủ sách</b>
                <span class="muted" style="font-size:0.82rem;">Truyện đã lưu &amp; đoạn đã đánh dấu</span>
            </a>
            <a class="shortcut" href="${pageContext.request.contextPath}/follow?action=list" style="text-decoration:none;">
                <span class="shortcut-icon" style="font-size:1.6rem;">👥</span>
                <b style="color:var(--text); font-size:0.95rem;">Đang theo dõi</b>
                <span class="muted" style="font-size:0.82rem;">Tác giả bạn quan tâm</span>
            </a>
            <a class="shortcut" href="${pageContext.request.contextPath}/wallet?action=topup" style="text-decoration:none;">
                <span class="shortcut-icon" style="font-size:1.6rem;">🪙</span>
                <b style="color:var(--text); font-size:0.95rem;">Ví &amp; Nạp xu</b>
                <span class="muted" style="font-size:0.82rem;">Nạp xu, lịch sử giao dịch</span>
            </a>
            <a class="shortcut" href="#gamification" style="text-decoration:none;">
                <span class="shortcut-icon" style="font-size:1.6rem;">🎁</span>
                <b style="color:var(--text); font-size:0.95rem;">Nhận xu miễn phí</b>
                <span class="muted" style="font-size:0.82rem;">Điểm danh &amp; nhiệm vụ hàng ngày</span>
            </a>
        </div>
    </div>
</div>
