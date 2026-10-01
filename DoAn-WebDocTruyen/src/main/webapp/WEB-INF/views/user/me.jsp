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
            <c:if test="${me.admin}">
                <span class="pill pill-warn">Quản trị viên</span>
            </c:if>
        </div>

        <div class="profile-actions">
            <a class="btn btn-primary"
               href="${pageContext.request.contextPath}/user?action=edit">Sửa hồ sơ</a>
            <a class="btn btn-ghost"
               href="${pageContext.request.contextPath}/user?action=profile&amp;id=${me.id}">
                Xem hồ sơ công khai</a>
        </div>
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
                <form action="${pageContext.request.contextPath}/user" method="post" style="display:inline;">
                    <input type="hidden" name="_csrf" value="${csrfToken}">
                    <input type="hidden" name="action" value="checkin">
                    <button type="submit" class="btn-checkin-cta" style="background:linear-gradient(135deg, #f59e0b, #d97706); color:#ffffff; border:none; padding:12px 32px; font-size:1rem; font-weight:700; border-radius:99px; box-shadow:0 4px 16px rgba(245, 158, 11, 0.4); cursor:pointer;">
                        ✨ Điểm Danh Ngay (+${(currentStreak % 7 + 1) eq 1 ? 5 : ((currentStreak % 7 + 1) eq 2 ? 10 : ((currentStreak % 7 + 1) eq 3 ? 15 : ((currentStreak % 7 + 1) eq 4 ? 20 : ((currentStreak % 7 + 1) eq 5 ? 25 : ((currentStreak % 7 + 1) eq 6 ? 30 : 50)))))} xu)
                    </button>
                </form>
            </c:when>
            <c:otherwise>
                <div class="checkin-done-pill" style="display:inline-flex; align-items:center; gap:8px; padding:10px 22px; border-radius:99px; background:rgba(16, 185, 129, 0.12); border:1px solid rgba(16, 185, 129, 0.35); color:#10b981; font-weight:700; font-size:0.92rem;">
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
                                <button type="submit" class="btn-claim-reward" style="background:#10b981; color:#ffffff; border:none; font-weight:700; padding:8px 18px; border-radius:6px; box-shadow:0 2px 8px rgba(16, 185, 129, 0.35); cursor:pointer;">
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
            <dd>
                <span class="badge badge-amber" style="font-size:0.9rem; padding:4px 14px; background:rgba(245, 158, 11, 0.15); color:#f59e0b; border:1px solid rgba(245, 158, 11, 0.35); border-radius:99px;">
                    🪙 <b><fmt:formatNumber pattern="#,##0" value="${walletBalance}"/></b> xu
                </span>
                <span class="muted" style="font-size:0.82em; margin-left:6px;">(Mở khoá VIP, tặng quà)</span>
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
                <b style="color:var(--text); font-size:0.95rem;">Truyện đã lưu</b>
                <span class="muted" style="font-size:0.82rem;">Đọc tiếp từ chương dở</span>
            </a>
            <a class="shortcut" href="${pageContext.request.contextPath}/follow?action=list" style="text-decoration:none;">
                <span class="shortcut-icon" style="font-size:1.6rem;">👥</span>
                <b style="color:var(--text); font-size:0.95rem;">Đang theo dõi</b>
                <span class="muted" style="font-size:0.82rem;">Tác giả bạn quan tâm</span>
            </a>
        </div>
    </div>
</div>
