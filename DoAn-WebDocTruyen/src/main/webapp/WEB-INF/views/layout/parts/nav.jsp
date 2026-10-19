<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--
  parts/nav.jsp — thanh menu trên cùng.

  DÙNG CHUNG cho layout main và layout admin — vì vậy CSS của nó nằm trong
  components.css chứ không phải layout-main.css.
--%>
<header class="site-header">
    <div class="shell">
        <div class="header-left">
            <a href="${pageContext.request.contextPath}/" class="brand">
                <span class="brand-mark">📖</span>
                <span>Đọc<em>Truyện</em></span>
            </a>

            <input type="checkbox" id="nav-open" class="nav-check" aria-label="Mở menu">
            <label for="nav-open" class="nav-toggle" aria-hidden="true">☰</label>

            <nav class="nav">
                <a href="${pageContext.request.contextPath}/"
                   class="${activeNav eq 'home' ? 'is-active' : ''}">Trang chủ</a>
                <a href="${pageContext.request.contextPath}/story?action=list"
                   class="${activeNav eq 'browse' ? 'is-active' : ''}">Kho truyện</a>
                <a href="${pageContext.request.contextPath}/rank"
                   class="${activeNav eq 'rank' ? 'is-active' : ''}">Xếp hạng</a>
                <a href="${pageContext.request.contextPath}/page?name=rules"
                   class="${activeNav eq 'rules' ? 'is-active' : ''}">Nội quy</a>
            </nav>
        </div>

        <div class="header-actions">
            <%-- Thanh tìm kiếm trên Header kèm Live Search Autocomplete --%>
            <div class="nav-search-wrap">
                <form action="${pageContext.request.contextPath}/story" method="get" class="nav-search-bar" id="nav-search-form" role="search">
                    <input type="hidden" name="action" value="list">
                    <span class="nav-search-icon">🔍</span>
                    <input type="search" name="q" id="nav-search-input" class="nav-search-input" placeholder="Tìm truyện, tác giả…"
                           value="<c:out value='${param.q}'/>" autocomplete="off" aria-label="Tìm kiếm truyện" aria-expanded="false" aria-haspopup="listbox">
                </form>
                <div class="search-suggest-panel" id="nav-search-suggest" style="display:none;" role="listbox"></div>
            </div>

            <%-- Nút chuyển đổi giao diện Sáng / Tối toàn trang --%>
            <button type="button" class="action-icon-btn theme-toggle" id="site-theme-toggle"
                    title="Chuyển đổi giao diện Sáng / Tối" aria-label="Chuyển đổi giao diện Sáng / Tối">
                <span id="theme-toggle-icon">🌙</span>
            </button>

            <c:choose>
                <c:when test="${not empty currentUser}">
                    <%-- Huy hiệu Ví Xu trên Header --%>
                    <a class="header-coin-badge" href="${pageContext.request.contextPath}/wallet?action=topup"
                       onclick="if(window.openQuickTopupModal){ window.openQuickTopupModal(); return false; }"
                       title="Ví xu: ${walletBalance != null ? walletBalance : 0} xu. Bấm để nạp nhanh tại chỗ">
                        <span class="coin-badge-icon">🪙</span>
                        <span class="coin-badge-val"><fmt:formatNumber pattern="#,##0" value="${walletBalance != null ? walletBalance : 0}"/></span>
                        <span class="coin-badge-unit">xu</span>
                        <span class="coin-badge-plus" title="Nạp nhanh">+</span>
                    </a>

                    <%-- Huy hiệu Chuỗi Điểm Danh / Đọc Liên Tục (Reading Streak 🔥) --%>
                    <c:choose>
                        <c:when test="${currentStreak != null and currentStreak gt 0}">
                            <a class="header-streak-badge" href="${pageContext.request.contextPath}/user?action=me#streak"
                               title="Chuỗi điểm danh đọc truyện: ${currentStreak} ngày liên tục 🔥">
                                <span class="streak-badge-icon">🔥</span>
                                <span class="streak-badge-val">${currentStreak}</span>
                                <span class="streak-badge-unit">ngày</span>
                            </a>
                        </c:when>
                        <c:otherwise>
                            <a class="header-streak-badge is-zero" href="${pageContext.request.contextPath}/user?action=me#streak"
                               title="Điểm danh nhận xu & bắt đầu chuỗi ngày đọc truyện 🔥">
                                <span class="streak-badge-icon">🔥</span>
                                <span class="streak-badge-unit">Điểm danh</span>
                            </a>
                        </c:otherwise>
                    </c:choose>

                    <%-- Chuông thông báo & Popover Thông báo trực tiếp --%>
                    <div class="nav-bell-wrap" id="nav-bell-wrap">
                        <button type="button" class="action-icon-btn bell" id="nav-bell-btn" title="Thông báo"
                                aria-haspopup="true" aria-expanded="false">
                            <span class="bell-icon">🔔</span>
                            <c:if test="${unreadCount gt 0}">
                                <span class="bell-badge" id="nav-bell-badge">${unreadCount gt 9 ? '9+' : unreadCount}</span>
                            </c:if>
                        </button>
                        <div class="dropdown-panel nav-bell-dropdown" id="nav-bell-dropdown" role="menu">
                            <div class="bell-dropdown-header">
                                <div class="bell-dropdown-title">
                                    <span>Thông báo</span>
                                    <span class="bell-unread-count" id="bell-unread-count"><c:out value="${unreadCount gt 0 ? unreadCount : 0}"/> mới</span>
                                </div>
                                <button type="button" class="bell-mark-all-btn" id="bell-mark-all-btn" title="Đánh dấu tất cả đã đọc">
                                    ✓ Đã đọc hết
                                </button>
                            </div>
                            <div class="bell-dropdown-body" id="bell-dropdown-body">
                                <div class="bell-loading">Đang tải thông báo…</div>
                            </div>
                            <div class="bell-dropdown-footer">
                                <a href="${pageContext.request.contextPath}/notification" class="bell-view-all">Xem tất cả thông báo &rarr;</a>
                            </div>
                        </div>
                    </div>

                    <%-- Menu Người Dùng Thả Xuống (User Dropdown Menu) --%>
                    <div class="user-menu" id="user-menu-wrap">
                        <button type="button" class="user-chip" id="user-chip-btn" aria-haspopup="true" aria-expanded="false" title="<c:out value='${currentUser.name}'/>">
                            <c:set var="avUrl" value="${currentUser.avatarUrl}"/>
                            <c:set var="avAlt" value="${currentUser.name}"/>
                            <c:set var="avInitial" value="${currentUser.initial}"/>
                            <c:set var="avClass" value="user-avatar"/>
                            <%@ include file="/WEB-INF/views/_partials/_avatar.jsp" %>
                            <span class="user-name"><c:out value="${currentUser.name}"/></span>
                            <span class="dropdown-caret">▾</span>
                        </button>
                        <div class="dropdown-panel user-dropdown-panel" id="user-dropdown-panel">
                            <div class="user-dropdown-header">
                                <c:set var="avUrl" value="${currentUser.avatarUrl}"/>
                                <c:set var="avAlt" value="${currentUser.name}"/>
                                <c:set var="avInitial" value="${currentUser.initial}"/>
                                <c:set var="avClass" value="user-avatar-lg"/>
                                <%@ include file="/WEB-INF/views/_partials/_avatar.jsp" %>
                                <div class="user-info-text">
                                    <div class="user-fullname"><c:out value="${currentUser.name}"/></div>
                                    <div class="user-username">@<c:out value="${currentUser.username}"/></div>
                                </div>
                            </div>
                            <div class="dropdown-sep"></div>

                            <%-- Mục 1: Tủ sách & Đọc truyện --%>
                            <div class="dropdown-section-title">Tủ sách &amp; Đọc truyện</div>
                            <a href="${pageContext.request.contextPath}/bookmark" class="dropdown-item ${activeNav eq 'bookmark' ? 'is-active' : ''}">
                                <span class="dropdown-item-icon">🔖</span>
                                <span>Truyện đã lưu</span>
                            </a>
                            <a href="${pageContext.request.contextPath}/history" class="dropdown-item ${activeNav eq 'history' ? 'is-active' : ''}">
                                <span class="dropdown-item-icon">🕒</span>
                                <span>Lịch sử đọc</span>
                            </a>
                            <a href="${pageContext.request.contextPath}/follow?action=list" class="dropdown-item ${activeNav eq 'follow' ? 'is-active' : ''}">
                                <span class="dropdown-item-icon">💖</span>
                                <span>Đang theo dõi</span>
                            </a>

                            <div class="dropdown-sep"></div>

                            <%-- Mục 2: Sáng tác & Quản lý truyện --%>
                            <div class="dropdown-section-title">Khu vực Tác giả</div>
                            <a href="${pageContext.request.contextPath}/story?action=mine" class="dropdown-item ${activeNav eq 'mine' ? 'is-active' : ''}">
                                <span class="dropdown-item-icon">✍️</span>
                                <span>Truyện của tôi</span>
                            </a>
                            <a href="${pageContext.request.contextPath}/story?action=create" class="dropdown-item">
                                <span class="dropdown-item-icon">➕</span>
                                <span>Đăng truyện mới</span>
                            </a>

                            <div class="dropdown-sep"></div>

                            <%-- Mục 3: Tài khoản & Ví xu --%>
                            <div class="dropdown-section-title">Tài khoản &amp; Ví xu</div>
                            <a href="${pageContext.request.contextPath}/wallet?action=topup" class="dropdown-item ${activeNav eq 'wallet' ? 'is-active' : ''}">
                                <span class="dropdown-item-icon">🪙</span>
                                <span class="dropdown-coin-flex">
                                    <span>Ví &amp; Nạp xu</span>
                                    <span class="dropdown-coin-tag"><fmt:formatNumber pattern="#,##0" value="${walletBalance != null ? walletBalance : 0}"/> xu</span>
                                </span>
                            </a>
                            <a href="${pageContext.request.contextPath}/user?action=me" class="dropdown-item ${activeNav eq 'me' ? 'is-active' : ''}">
                                <span class="dropdown-item-icon">👤</span>
                                <span>Hồ sơ cá nhân</span>
                            </a>
                            <a href="${pageContext.request.contextPath}/user?action=edit" class="dropdown-item">
                                <span class="dropdown-item-icon">⚙️</span>
                                <span>Cài đặt &amp; Sửa hồ sơ</span>
                            </a>

                            <c:if test="${currentUser.admin or currentUser.moderator or currentUser.editor}">
                                <div class="dropdown-sep"></div>
                                <div class="dropdown-section-title">Hệ thống</div>
                                <a href="${pageContext.request.contextPath}/admin/dashboard" class="dropdown-item admin-item">
                                    <span class="dropdown-item-icon">🛡️</span>
                                    <span>Khu vực Quản trị</span>
                                </a>
                            </c:if>

                            <div class="dropdown-sep"></div>
                            <form id="navLogoutForm" action="${pageContext.request.contextPath}/auth" method="post" style="display:none;">
                                <input type="hidden" name="action" value="logout">
                                <input type="hidden" name="_csrf" value="${csrfToken}">
                            </form>
                            <a href="${pageContext.request.contextPath}/auth?action=logout"
                               class="dropdown-item logout-item"
                               data-confirm="Bạn có chắc chắn muốn đăng xuất khỏi tài khoản?"
                               data-confirm-title="Xác nhận đăng xuất"
                               data-confirm-ok="Đăng xuất"
                               onclick="event.preventDefault(); var form=document.getElementById('navLogoutForm'); if(window.showConfirmModal){ window.showConfirmModal('Bạn có chắc chắn muốn đăng xuất khỏi tài khoản?', { title: 'Xác nhận đăng xuất', confirmText: 'Đăng xuất', cancelText: 'Ở lại', isDanger: true, onConfirm: function(){ form.submit(); } }); } else if(confirm('Bạn có chắc chắn muốn đăng xuất?')) { form.submit(); }">
                                <span class="dropdown-item-icon">🚪</span>
                                <span>Đăng xuất</span>
                            </a>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <a class="btn btn-ghost btn-sm"
                       href="${pageContext.request.contextPath}/auth?action=login">Đăng nhập</a>
                    <a class="btn btn-primary btn-sm"
                       href="${pageContext.request.contextPath}/auth?action=register">Đăng ký</a>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</header>
<%-- Logic tương tác header (theme, dropdowns, live search) đã được tách sang assets/js/nav.js (ISSUE-024) --%>

