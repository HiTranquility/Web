<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--
================================================================================
  user/me.jsp — MẢNH NỘI DUNG: Hồ sơ của tôi                   TRANG 14
================================================================================
  TẦNG: views/ — chỉ hiển thị. Dữ liệu do UserServlet.me() chuẩn bị.

  Nhận: me (User) · totalViews

  KHÁC TRANG 4 (hồ sơ công khai) Ở CHỖ NÀO
    Trang này hiện những thứ CHỈ CHỦ TÀI KHOẢN mới được thấy: email, ngày
    tham gia, và các lối đi vào khu riêng (sửa hồ sơ, truyện của tôi, thống
    kê, tủ truyện). Trang 4 không có gì trong số đó.
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

<%--
  Thông tin riêng tư. Đặt trong khung có viền khác màu để phân biệt rõ với
  phần trên — phần trên ai cũng xem được, phần này thì không.
--%>
<div class="panel">
    <h3 class="panel-title">🔒 Thông tin tài khoản</h3>
    <dl class="kv">
        <dt>Tên đăng nhập</dt>
        <dd><c:out value="${me.username}"/>
            <span class="muted">— không đổi được</span></dd>

        <dt>Email</dt>
        <dd><c:out value="${me.email}"/></dd>

        <dt>Liên kết Google</dt>
        <dd>
            <c:choose>
                <c:when test="${not empty googleIdentity}">
                    <span class="pill pill-ok">Đã liên kết</span>
                    <span><c:out value="${googleIdentity.email}"/></span>
                </c:when>
                <c:otherwise>
                    <span class="muted">Chưa liên kết</span>
                    <a href="${pageContext.request.contextPath}/user?action=edit#link-google" style="font-size:0.85em; margin-left:8px;">Liên kết ngay →</a>
                </c:otherwise>
            </c:choose>
        </dd>

        <dt>Ví xu ảo</dt>
        <dd>
            <span class="pill pill-ok" style="font-size: 0.9em;">🪙 <b><fmt:formatNumber pattern="#,##0" value="${walletBalance}"/></b> xu</span>
            <span class="muted" style="font-size: 0.85em; margin-left: 8px;">(Dùng tặng hoa và ủng hộ tác giả)</span>
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
    <p class="muted-note">
        Chỉ mình bạn thấy được khối này. Người khác vào hồ sơ của bạn chỉ thấy
        tên hiển thị, giới thiệu và danh sách truyện.
    </p>
</div>

<%-- ================================================================================
     GAMIFICATION: ĐIỂM DANH 7 NGÀY & NHIỆM VỤ HÀNG NGÀY NHẬN XU
     ================================================================================ --%>
<div class="panel gamification-panel" id="gamification" style="margin-top:20px; border:1px solid rgba(245, 158, 11, 0.3); background: linear-gradient(180deg, rgba(245, 158, 11, 0.04) 0%, rgba(0,0,0,0) 100%);">
    <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:10px; margin-bottom:16px;">
        <h3 class="panel-title" style="margin:0; color:#f59e0b; display:flex; align-items:center; gap:8px;">
            🎁 Trạm Nhận Xu: Điểm danh &amp; Nhiệm vụ
        </h3>
        <span class="badge" style="background:rgba(245, 158, 11, 0.15); color:#f59e0b; border:1px solid rgba(245, 158, 11, 0.4); padding:4px 10px; border-radius:20px; font-weight:600;">
            🔥 Chuỗi: ${currentStreak} ngày liên tiếp
        </span>
    </div>

    <%-- 7 NGÀY ĐIỂM DANH --%>
    <div class="checkin-streak-container" style="display:grid; grid-template-columns:repeat(auto-fit, minmax(80px, 1fr)); gap:10px; margin-bottom:20px;">
        <c:forEach var="day" begin="1" end="7">
            <c:set var="isPassed" value="${day lt currentStreak or (day eq currentStreak and not empty todayCheckin)}" />
            <c:set var="isTodayTarget" value="${(empty todayCheckin and day eq (currentStreak % 7 + 1)) or (not empty todayCheckin and day eq currentStreak)}" />
            <c:set var="coinReward" value="${day eq 1 ? 5 : (day eq 2 ? 10 : (day eq 3 ? 15 : (day eq 4 ? 20 : (day eq 5 ? 25 : (day eq 6 ? 30 : 50)))))}" />

            <div class="streak-day-card ${isPassed ? 'day-claimed' : (isTodayTarget ? 'day-today' : '')}"
                 style="padding:10px 6px; text-align:center; border-radius:8px; border:1px solid ${isPassed ? '#10b981' : (isTodayTarget ? '#f59e0b' : 'var(--border)')}; background:${isPassed ? 'rgba(16, 185, 129, 0.08)' : (isTodayTarget ? 'rgba(245, 158, 11, 0.12)' : 'var(--card-bg, rgba(255,255,255,0.02))')};">
                <div style="font-size:0.8rem; font-weight:600; color:var(--text-mut);">Ngày ${day}</div>
                <div style="font-size:1.4rem; margin:4px 0;">${day eq 7 ? '👑' : (isPassed ? '✅' : '🪙')}</div>
                <div style="font-size:0.85rem; font-weight:700; color:${isPassed ? '#10b981' : '#f59e0b'};">+${coinReward} xu</div>
                <div style="font-size:0.75rem; margin-top:4px;">
                    <c:choose>
                        <c:when test="${isPassed}"><span style="color:#10b981; font-weight:600;">Đã nhận</span></c:when>
                        <c:when test="${isTodayTarget}"><span style="color:#f59e0b; font-weight:600;">Hôm nay</span></c:when>
                        <c:otherwise><span class="muted">Chờ</span></c:otherwise>
                    </c:choose>
                </div>
            </div>
        </c:forEach>
    </div>

    <%-- Nút bấm điểm danh --%>
    <div style="text-align:center; margin-bottom:24px;">
        <c:choose>
            <c:when test="${empty todayCheckin}">
                <form action="${pageContext.request.contextPath}/user" method="post" style="display:inline;">
                    <input type="hidden" name="_csrf" value="${csrfToken}">
                    <input type="hidden" name="action" value="checkin">
                    <button type="submit" class="btn btn-primary btn-lg" style="background:linear-gradient(135deg, #f59e0b, #d97706); border:none; padding:10px 28px; font-weight:700; box-shadow:0 4px 14px rgba(245, 158, 11, 0.4);">
                        ✨ Điểm Danh Ngay (+${(currentStreak % 7 + 1) eq 1 ? 5 : ((currentStreak % 7 + 1) eq 2 ? 10 : ((currentStreak % 7 + 1) eq 3 ? 15 : ((currentStreak % 7 + 1) eq 4 ? 20 : ((currentStreak % 7 + 1) eq 5 ? 25 : ((currentStreak % 7 + 1) eq 6 ? 30 : 50)))))} xu)
                    </button>
                </form>
            </c:when>
            <c:otherwise>
                <button type="button" class="btn btn-secondary btn-lg" disabled style="opacity:0.85;">
                    ✅ Bạn đã hoàn thành điểm danh hôm nay (+${todayCheckin.rewardCoins} xu)
                </button>
                <p class="muted small" style="margin-top:6px;">Quay lại vào ngày mai để tiếp tục duy trì chuỗi nhận quà nhé!</p>
            </c:otherwise>
        </c:choose>
    </div>

    <%-- NHIỆM VỤ HÀNG NGÀY --%>
    <h4 style="margin:0 0 12px 0; font-size:1.05rem; display:flex; align-items:center; gap:6px;">
        🎯 Nhiệm vụ hàng ngày
    </h4>
    <div class="quests-list" style="display:flex; flex-direction:column; gap:10px;">
        <c:forEach var="q" items="${dailyQuests}">
            <div class="quest-item-card" style="display:flex; align-items:center; justify-content:space-between; flex-wrap:wrap; gap:12px; padding:12px 16px; border:1px solid var(--border); border-radius:8px; background:var(--card-bg, rgba(255,255,255,0.02));">
                <div style="flex:1; min-width:200px;">
                    <div style="display:flex; align-items:center; gap:8px;">
                        <strong style="color:var(--text);"><c:out value="${q.title}"/></strong>
                        <span class="badge" style="background:rgba(245, 158, 11, 0.15); color:#f59e0b; font-size:0.75rem;">+${q.rewardCoins} xu</span>
                    </div>
                    <p class="muted small" style="margin:3px 0 0 0;"><c:out value="${q.description}"/></p>
                </div>
                <div>
                    <c:choose>
                        <c:when test="${q.claimed}">
                            <span class="badge badge-success" style="padding:6px 12px; font-weight:600;">✓ Đã nhận</span>
                        </c:when>
                        <c:when test="${q.completed}">
                            <form action="${pageContext.request.contextPath}/user" method="post" style="display:inline;">
                                <input type="hidden" name="_csrf" value="${csrfToken}">
                                <input type="hidden" name="action" value="claim-quest">
                                <input type="hidden" name="key" value="${q.key}">
                                <button type="submit" class="btn btn-sm btn-primary" style="background:#10b981; border:none; font-weight:600;">
                                    🎁 Nhận thưởng
                                </button>
                            </form>
                        </c:when>
                        <c:otherwise>
                            <span class="muted small" style="padding:4px 8px; border:1px dashed var(--border); border-radius:4px;">Chưa hoàn thành</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </c:forEach>
    </div>
</div>

<div class="section-head">
    <h2>Lối tắt</h2>
</div>

<div class="shortcut-grid">
    <a class="shortcut" href="${pageContext.request.contextPath}/story?action=mine">
        <span class="shortcut-icon">📚</span>
        <b>Truyện của tôi</b>
        <span class="muted">Quản lý truyện đã đăng, cả bản nháp</span>
    </a>
    <a class="shortcut" href="${pageContext.request.contextPath}/story?action=stats">
        <span class="shortcut-icon">📊</span>
        <b>Thống kê</b>
        <span class="muted">Lượt xem, lượt lưu, bình luận</span>
    </a>
    <a class="shortcut" href="${pageContext.request.contextPath}/bookmark">
        <span class="shortcut-icon">🔖</span>
        <b>Truyện đã lưu</b>
        <span class="muted">Đọc tiếp từ chỗ đang dở</span>
    </a>
    <a class="shortcut" href="${pageContext.request.contextPath}/follow?action=list">
        <span class="shortcut-icon">👥</span>
        <b>Đang theo dõi</b>
        <span class="muted">Tác giả bạn đang theo</span>
    </a>
</div>
