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
    <span class="profile-avatar">${me.initial}</span>

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
