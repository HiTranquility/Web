<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
                    <%-- Chuông thông báo --%>
                    <a class="action-icon-btn bell" title="Thông báo"
                       href="${pageContext.request.contextPath}/notification">
                        <span class="bell-icon">🔔</span>
                        <c:if test="${unreadCount gt 0}">
                            <span class="bell-badge">${unreadCount gt 9 ? '9+' : unreadCount}</span>
                        </c:if>
                    </a>

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

                            <%-- Mục 3: Tài khoản --%>
                            <div class="dropdown-section-title">Tài khoản</div>
                            <a href="${pageContext.request.contextPath}/user?action=me" class="dropdown-item ${activeNav eq 'me' ? 'is-active' : ''}">
                                <span class="dropdown-item-icon">👤</span>
                                <span>Hồ sơ cá nhân</span>
                            </a>
                            <a href="${pageContext.request.contextPath}/user?action=edit" class="dropdown-item">
                                <span class="dropdown-item-icon">⚙️</span>
                                <span>Cài đặt &amp; Sửa hồ sơ</span>
                            </a>

                            <c:if test="${currentUser.admin}">
                                <div class="dropdown-sep"></div>
                                <div class="dropdown-section-title">Hệ thống</div>
                                <a href="${pageContext.request.contextPath}/admin/dashboard" class="dropdown-item admin-item">
                                    <span class="dropdown-item-icon">🛡️</span>
                                    <span>Khu vực Quản trị</span>
                                </a>
                            </c:if>

                            <div class="dropdown-sep"></div>
                            <a href="${pageContext.request.contextPath}/auth?action=logout" class="dropdown-item logout-item">
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

