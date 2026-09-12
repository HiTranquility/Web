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

                <c:if test="${not empty currentUser}">
                    <div class="nav-dropdown" id="nav-bookshelf-dropdown">
                        <button type="button" class="nav-dropdown-btn ${(activeNav eq 'mine' or activeNav eq 'bookmark' or activeNav eq 'history' or activeNav eq 'follow') ? 'is-active' : ''}" id="nav-bookshelf-btn" aria-haspopup="true">
                            <span>Tủ sách</span>
                            <span class="dropdown-caret">▾</span>
                        </button>
                        <div class="dropdown-panel nav-dropdown-panel" id="nav-bookshelf-panel">
                            <a href="${pageContext.request.contextPath}/bookmark" class="${activeNav eq 'bookmark' ? 'is-active' : ''}">
                                <span class="dropdown-item-icon">🔖</span>
                                <span>Truyện đã lưu</span>
                            </a>
                            <a href="${pageContext.request.contextPath}/history" class="${activeNav eq 'history' ? 'is-active' : ''}">
                                <span class="dropdown-item-icon">🕒</span>
                                <span>Lịch sử đọc</span>
                            </a>
                            <a href="${pageContext.request.contextPath}/follow?action=list" class="${activeNav eq 'follow' ? 'is-active' : ''}">
                                <span class="dropdown-item-icon">💖</span>
                                <span>Đang theo dõi</span>
                            </a>
                            <div class="dropdown-sep"></div>
                            <a href="${pageContext.request.contextPath}/story?action=mine" class="${activeNav eq 'mine' ? 'is-active' : ''}">
                                <span class="dropdown-item-icon">✍️</span>
                                <span>Truyện của tôi</span>
                            </a>
                        </div>
                    </div>
                </c:if>
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
                            <span class="user-avatar">${currentUser.initial}</span>
                            <span class="user-name"><c:out value="${currentUser.name}"/></span>
                            <span class="dropdown-caret">▾</span>
                        </button>
                        <div class="dropdown-panel user-dropdown-panel" id="user-dropdown-panel">
                            <div class="user-dropdown-header">
                                <span class="user-avatar-lg">${currentUser.initial}</span>
                                <div class="user-info-text">
                                    <div class="user-fullname"><c:out value="${currentUser.name}"/></div>
                                    <div class="user-username">@<c:out value="${currentUser.username}"/></div>
                                </div>
                            </div>
                            <div class="dropdown-sep"></div>
                            <a href="${pageContext.request.contextPath}/user?action=me" class="dropdown-item">
                                <span class="dropdown-item-icon">👤</span>
                                <span>Hồ sơ cá nhân</span>
                            </a>
                            <a href="${pageContext.request.contextPath}/bookmark" class="dropdown-item">
                                <span class="dropdown-item-icon">🔖</span>
                                <span>Tủ sách của tôi</span>
                            </a>
                            <a href="${pageContext.request.contextPath}/story?action=mine" class="dropdown-item">
                                <span class="dropdown-item-icon">✍️</span>
                                <span>Quản lý truyện</span>
                            </a>
                            <c:if test="${currentUser.admin}">
                                <div class="dropdown-sep"></div>
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

<script>
(function () {
    // Theme toggle
    var themeBtn = document.getElementById('site-theme-toggle');
    if (themeBtn) {
        var icon = document.getElementById('theme-toggle-icon');
        function syncTheme(isLight) {
            if (icon) icon.textContent = isLight ? '☀️' : '🌙';
            themeBtn.setAttribute('title', isLight ? 'Chế độ Sáng (bấm để đổi Tối)' : 'Chế độ Tối (bấm để đổi Sáng)');
            themeBtn.setAttribute('aria-label', isLight ? 'Chế độ Sáng' : 'Chế độ Tối');
        }
        syncTheme(document.documentElement.getAttribute('data-site-theme') === 'light');
        themeBtn.addEventListener('click', function () {
            var isLight = document.documentElement.getAttribute('data-site-theme') === 'light';
            if (isLight) {
                document.documentElement.removeAttribute('data-site-theme');
                try { localStorage.setItem('site_theme', 'dark'); } catch (e) {}
                syncTheme(false);
            } else {
                document.documentElement.setAttribute('data-site-theme', 'light');
                try { localStorage.setItem('site_theme', 'light'); } catch (e) {}
                syncTheme(true);
            }
        });
    }

    // Dropdowns (User menu & Bookshelf menu)
    function setupDropdown(btnId, wrapId) {
        var btn = document.getElementById(btnId);
        var wrap = document.getElementById(wrapId);
        if (!btn || !wrap) return;

        btn.addEventListener('click', function (e) {
            e.preventDefault();
            e.stopPropagation();
            var isOpen = wrap.classList.contains('is-open');
            document.querySelectorAll('.user-menu.is-open, .nav-dropdown.is-open').forEach(function (el) {
                if (el !== wrap) el.classList.remove('is-open');
            });
            wrap.classList.toggle('is-open', !isOpen);
            btn.setAttribute('aria-expanded', !isOpen ? 'true' : 'false');
        });
    }

    setupDropdown('user-chip-btn', 'user-menu-wrap');
    setupDropdown('nav-bookshelf-btn', 'nav-bookshelf-dropdown');

    document.addEventListener('click', function (e) {
        document.querySelectorAll('.user-menu.is-open, .nav-dropdown.is-open').forEach(function (el) {
            if (!el.contains(e.target)) {
                el.classList.remove('is-open');
                var toggleBtn = el.querySelector('button');
                if (toggleBtn) toggleBtn.setAttribute('aria-expanded', 'false');
            }
        });
    });

    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') {
            document.querySelectorAll('.user-menu.is-open, .nav-dropdown.is-open').forEach(function (el) {
                el.classList.remove('is-open');
                var toggleBtn = el.querySelector('button');
                if (toggleBtn) toggleBtn.setAttribute('aria-expanded', 'false');
            });
        }
    });
})();

/* Live Search Autocomplete */
(function () {
    var searchInput = document.getElementById('nav-search-input');
    var suggestPanel = document.getElementById('nav-search-suggest');
    var searchForm = document.getElementById('nav-search-form');
    if (!searchInput || !suggestPanel || !searchForm) return;

    var timer = null;
    var currentQuery = '';
    var activeIdx = -1;

    function hideSuggest() {
        suggestPanel.style.display = 'none';
        suggestPanel.innerHTML = '';
        searchInput.setAttribute('aria-expanded', 'false');
        activeIdx = -1;
    }

    function highlightText(text, q) {
        if (!q) return escapeHtml(text);
        var idx = text.toLowerCase().indexOf(q.toLowerCase());
        if (idx === -1) return escapeHtml(text);
        var before = text.substring(0, idx);
        var match = text.substring(idx, idx + q.length);
        var after = text.substring(idx + q.length);
        return escapeHtml(before) + '<mark>' + escapeHtml(match) + '</mark>' + escapeHtml(after);
    }

    function escapeHtml(s) {
        return (s || '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
    }

    searchInput.addEventListener('input', function () {
        var q = (this.value || '').trim();
        if (timer) clearTimeout(timer);
        if (q.length < 2) {
            hideSuggest();
            return;
        }

        timer = setTimeout(function () {
            currentQuery = q;
            fetch('${pageContext.request.contextPath}/story?action=suggest&q=' + encodeURIComponent(q))
                .then(function (res) { return res.json(); })
                .then(function (data) {
                    if (currentQuery !== q) return;
                    if (!data || !data.length) {
                        suggestPanel.innerHTML = '<div class="suggest-empty">Không tìm thấy truyện phù hợp</div>';
                        suggestPanel.style.display = 'block';
                        searchInput.setAttribute('aria-expanded', 'true');
                        return;
                    }

                    var html = '';
                    data.forEach(function (s, idx) {
                        var coverHtml = s.cover
                            ? '<img src="' + escapeHtml(s.cover) + '" alt="" class="suggest-thumb" onerror="this.outerHTML=\'<span class=\\\'suggest-thumb-icon\\\'>📖</span>\'">'
                            : '<span class="suggest-thumb-icon">📖</span>';
                        var statusLabel = s.completed ? '<span class="suggest-status is-completed">Hoàn thành</span>' : '<span class="suggest-status">Đang ra</span>';

                        html += '<a href="${pageContext.request.contextPath}/story?action=detail&id=' + s.id + '" class="suggest-item" data-index="' + idx + '" role="option">'
                              +   coverHtml
                              +   '<div class="suggest-info">'
                              +     '<div class="suggest-title">' + highlightText(s.title, q) + '</div>'
                              +     '<div class="suggest-meta">' + escapeHtml(s.author) + ' · ' + s.chapters + ' chương · ' + statusLabel + '</div>'
                              +   '</div>'
                              + '</a>';
                    });

                    html += '<a href="${pageContext.request.contextPath}/story?action=list&q=' + encodeURIComponent(q) + '" class="suggest-all-link">'
                          +   'Xem tất cả kết quả cho "<b>' + escapeHtml(q) + '</b>" &rarr;'
                          + '</a>';

                    suggestPanel.innerHTML = html;
                    suggestPanel.style.display = 'block';
                    searchInput.setAttribute('aria-expanded', 'true');
                    activeIdx = -1;
                })
                .catch(function () {
                    hideSuggest();
                });
        }, 200);
    });

    searchInput.addEventListener('keydown', function (e) {
        var items = suggestPanel.querySelectorAll('.suggest-item');
        if (!items.length || suggestPanel.style.display === 'none') return;

        if (e.key === 'ArrowDown') {
            e.preventDefault();
            activeIdx = (activeIdx + 1) % items.length;
            updateActiveItem(items);
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            activeIdx = (activeIdx - 1 + items.length) % items.length;
            updateActiveItem(items);
        } else if (e.key === 'Enter') {
            if (activeIdx >= 0 && items[activeIdx]) {
                e.preventDefault();
                items[activeIdx].click();
            }
        } else if (e.key === 'Escape') {
            hideSuggest();
        }
    });

    function updateActiveItem(items) {
        items.forEach(function (el, i) {
            el.classList.toggle('is-active', i === activeIdx);
            if (i === activeIdx) el.scrollIntoView({ block: 'nearest' });
        });
    }

    document.addEventListener('click', function (e) {
        if (!searchForm.contains(e.target) && !suggestPanel.contains(e.target)) {
            hideSuggest();
        }
    });
})();
</script>
