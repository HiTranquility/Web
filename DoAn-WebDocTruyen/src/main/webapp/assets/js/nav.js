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
    // 3. TÌM KIẾM TỰ ĐỘNG GỢI Ý (LIVE SEARCH AUTOCOMPLETE)
    // ========================================================================
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

                        html += '<a href="' + CTX + '/story?action=detail&id=' + s.id + '" class="suggest-item" data-index="' + idx + '" role="option">'
                              +   coverHtml
                              +   '<div class="suggest-info">'
                              +     '<div class="suggest-title">' + highlightText(s.title, q) + '</div>'
                              +     '<div class="suggest-meta">' + escapeHtml(s.author) + ' · ' + s.chapters + ' chương · ' + statusLabel + '</div>'
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
