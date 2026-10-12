/**
 * assets/js/nav.js — Logic cho thanh điều hướng, tìm kiếm tự động, chuyển theme và dropdown menu.
 * Tách từ views/layout/parts/nav.jsp (ISSUE-024).
 * Trình duyệt tải một lần và cache từ đĩa (HTTP 304 / disk cache).
 */
(function () {
    'use strict';

    // Đọc contextPath từ attribute trên <body> hoặc thẻ <meta>
    var CTX = (document.body && document.body.dataset && document.body.dataset.ctx)
        || (document.querySelector('meta[name="context-path"]') && document.querySelector('meta[name="context-path"]').getAttribute('content'))
        || '';

    // ========================================================================
    // 1. CHUYỂN ĐỔI GIAO DIỆN SÁNG / TỐI (THEME TOGGLE)
    // ========================================================================
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

    // ========================================================================
    // 2. DROPDOWN MENUS (MENU NGƯỜI DÙNG & TỦ SÁCH)
    // ========================================================================
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

    // ========================================================================
    // 3. TÌM KIẾM TỰ ĐỘNG GỢI Ý (LIVE SEARCH AUTOCOMPLETE & RECENT SEARCHES)
    // ========================================================================
    var searchInput = document.getElementById('nav-search-input');
    var suggestPanel = document.getElementById('nav-search-suggest');
    var searchForm = document.getElementById('nav-search-form');
    if (!searchInput || !suggestPanel || !searchForm) return;

    var timer = null;
    var currentQuery = '';
    var activeIdx = -1;

    // Quản lý Lịch sử tìm kiếm gần đây trong localStorage
    var STORAGE_KEY = 'truyen_recent_searches';
    function getRecentSearches() {
        try {
            var raw = localStorage.getItem(STORAGE_KEY);
            return raw ? JSON.parse(raw) : [];
        } catch (e) {
            return [];
        }
    }

    function saveRecentSearch(q) {
        if (!q || !q.trim()) return;
        q = q.trim();
        try {
            var list = getRecentSearches().filter(function (item) { return item.toLowerCase() !== q.toLowerCase(); });
            list.unshift(q);
            if (list.length > 6) list = list.slice(0, 6);
            localStorage.setItem(STORAGE_KEY, JSON.stringify(list));
        } catch (e) {}
    }

    function removeRecentSearch(q) {
        try {
            var list = getRecentSearches().filter(function (item) { return item.toLowerCase() !== q.toLowerCase(); });
            localStorage.setItem(STORAGE_KEY, JSON.stringify(list));
        } catch (e) {}
    }

    function clearRecentSearches() {
        try {
            localStorage.removeItem(STORAGE_KEY);
        } catch (e) {}
    }

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

    // Hiển thị Lịch sử tìm kiếm gần đây và các Thể loại xu hướng khi ô tìm kiếm rỗng
    function showDefaultSuggest() {
        var recents = getRecentSearches();
        var html = '';

        if (recents.length > 0) {
            html += '<div class="suggest-section-head">'
                  +   '<span>Tìm kiếm gần đây</span>'
                  +   '<button type="button" class="suggest-clear-btn" id="btn-clear-recent-searches">Xoá hết</button>'
                  + '</div>';
            recents.forEach(function (term) {
                html += '<div class="suggest-recent-item" data-term="' + escapeHtml(term) + '">'
                      +   '<div class="suggest-recent-left">'
                      +     '<span class="suggest-recent-icon">🕒</span>'
                      +     '<span class="suggest-recent-text">' + escapeHtml(term) + '</span>'
                      +   '</div>'
                      +   '<button type="button" class="suggest-recent-del" data-del="' + escapeHtml(term) + '" title="Xoá từ này" aria-label="Xoá">&times;</button>'
                      + '</div>';
            });
        }

        var trending = [
            { name: 'Tiên hiệp', q: 'Tiên hiệp' },
            { name: 'Kiếm hiệp', q: 'Kiếm hiệp' },
            { name: 'Đô thị', q: 'Đô thị' },
            { name: 'Huyền huyễn', q: 'Huyền huyễn' },
            { name: 'Trọng sinh', q: 'Trọng sinh' },
            { name: 'Truyện tranh', url: CTX + '/story?action=list&type=comic' },
            { name: 'Ngôn tình', q: 'Ngôn tình' }
        ];

        html += '<div class="suggest-section-head"><span>Chủ đề nổi bật</span></div>';
        html += '<div class="suggest-tags-wrap">';
        trending.forEach(function (t) {
            var href = t.url ? t.url : (CTX + '/story?action=list&q=' + encodeURIComponent(t.q));
            html += '<a href="' + href + '" class="suggest-tag-chip" data-term="' + (t.q ? escapeHtml(t.q) : '') + '">' + escapeHtml(t.name) + '</a>';
        });
        html += '</div>';

        suggestPanel.innerHTML = html;
        suggestPanel.style.display = 'block';
        searchInput.setAttribute('aria-expanded', 'true');
        activeIdx = -1;

        // Bắt sự kiện xoá lịch sử và click vào từ khoá gần đây
        var clearBtn = suggestPanel.querySelector('#btn-clear-recent-searches');
        if (clearBtn) {
            clearBtn.addEventListener('click', function (e) {
                e.preventDefault();
                e.stopPropagation();
                clearRecentSearches();
                showDefaultSuggest();
            });
        }

        suggestPanel.querySelectorAll('.suggest-recent-del').forEach(function (btn) {
            btn.addEventListener('click', function (e) {
                e.preventDefault();
                e.stopPropagation();
                var delTerm = this.getAttribute('data-del');
                removeRecentSearch(delTerm);
                showDefaultSuggest();
            });
        });

        suggestPanel.querySelectorAll('.suggest-recent-item').forEach(function (item) {
            item.addEventListener('click', function (e) {
                if (e.target.closest('.suggest-recent-del')) return;
                var term = this.getAttribute('data-term');
                if (term) {
                    saveRecentSearch(term);
                    window.location.href = CTX + '/story?action=list&q=' + encodeURIComponent(term);
                }
            });
        });

        suggestPanel.querySelectorAll('.suggest-tag-chip').forEach(function (chip) {
            chip.addEventListener('click', function () {
                var term = this.getAttribute('data-term');
                if (term) saveRecentSearch(term);
            });
        });
    }

    searchInput.addEventListener('focus', function () {
        var q = (this.value || '').trim();
        if (q.length < 2) {
            showDefaultSuggest();
        }
    });

    searchInput.addEventListener('click', function () {
        var q = (this.value || '').trim();
        if (q.length < 2 && suggestPanel.style.display === 'none') {
            showDefaultSuggest();
        }
    });

    searchForm.addEventListener('submit', function () {
        var q = (searchInput.value || '').trim();
        if (q) saveRecentSearch(q);
    });

    searchInput.addEventListener('input', function () {
        var q = (this.value || '').trim();
        if (timer) clearTimeout(timer);
        if (q.length < 2) {
            showDefaultSuggest();
            return;
        }

        timer = setTimeout(function () {
            currentQuery = q;
            fetch(CTX + '/story?action=suggest&q=' + encodeURIComponent(q))
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

                        var viewsLabel = (typeof s.views === 'number' && s.views > 0)
                            ? ' · 👁️ ' + (s.views >= 1000 ? (s.views / 1000).toFixed(1).replace(/\.0$/, '') + 'k' : s.views)
                            : '';

                        html += '<a href="' + CTX + '/story?action=detail&id=' + s.id + '" class="suggest-item" data-index="' + idx + '" role="option">'
                              +   coverHtml
                              +   '<div class="suggest-info">'
                              +     '<div class="suggest-title">' + highlightText(s.title, q) + '</div>'
                              +     '<div class="suggest-meta">' + escapeHtml(s.author) + ' · ' + s.chapters + ' chương' + viewsLabel + ' · ' + statusLabel + '</div>'
                              +   '</div>'
                              + '</a>';
                    });

                    html += '<a href="' + CTX + '/story?action=list&q=' + encodeURIComponent(q) + '" class="suggest-all-link">'
                          +   'Xem tất cả kết quả cho "<b>' + escapeHtml(q) + '</b>" &rarr;'
                          + '</a>';

                    suggestPanel.innerHTML = html;
                    suggestPanel.style.display = 'block';
                    searchInput.setAttribute('aria-expanded', 'true');
                    activeIdx = -1;

                    // Lưu từ khoá khi click vào kết quả
                    suggestPanel.querySelectorAll('.suggest-item, .suggest-all-link').forEach(function (el) {
                        el.addEventListener('click', function () {
                            saveRecentSearch(q);
                        });
                    });
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
