(function (global) {
    'use strict';
    function fold(value) { return String(value).normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/[đĐ]/g, 'd').toLowerCase().trim(); }
    function select(items, state) {
        var words = fold(state.search || '').split(/\s+/).filter(Boolean);
        return items.filter(function (a) {
            return (state.tab !== 'milestones' || a.coins > 0) && (!state.category || a.category === state.category)
                && words.every(function (word) { return a.search.indexOf(word) !== -1; })
                && (!state.status || (state.status === 'earned' && a.earned) || (state.status === 'locked' && !a.earned)
                    || (state.status === 'claimable' && a.earned && !a.claimed && a.coins > 0)
                    || (state.status === 'claimed' && a.claimed));
        }).sort(function (a, b) {
            if (state.sort === 'progress') return Number(a.earned) - Number(b.earned) || b.progress - a.progress || a.index - b.index;
            if (state.sort === 'coins') return b.coins - a.coins || a.index - b.index;
            if (state.sort === 'name') return a.name.localeCompare(b.name, 'vi') || a.index - b.index;
            return a.index - b.index;
        });
    }
    function page(items, requested, size) {
        var pages = Math.max(1, Math.ceil(items.length / size)), current = Math.max(1, Math.min(requested, pages));
        return { current: current, pages: pages, items: items.slice((current - 1) * size, current * size) };
    }
    function init(root) {
        var q = function (s) { return root.querySelector(s); };
        var state = { tab: global.location.hash === '#milestones' ? 'milestones' : 'perks', search: '', category: '', status: '', sort: 'catalog', page: 1, size: 12 };
        var items = Array.from(root.querySelectorAll('[data-achievement]')).map(function (el, index) {
            return { el: el, index: index, key: el.dataset.achievement, category: el.dataset.category,
                earned: el.dataset.earned === 'true', claimed: el.dataset.claimed === 'true', coins: Number(el.dataset.coins),
                progress: Number(el.dataset.progress), name: el.querySelector('h3').textContent,
                search: fold(el.querySelector('h3').textContent + ' ' + el.querySelector('.achievement-description').textContent + ' ' + el.dataset.category) };
        });
        Array.from(new Set(items.map(function (a) { return a.category; }))).forEach(function (category) {
            var option = document.createElement('option'); option.value = category; option.textContent = category; q('[data-category]').appendChild(option);
        });
        function render() {
            root.dataset.tab = state.tab;
            q('#achievement-results').setAttribute('role', 'tabpanel');
            q('#achievement-results').setAttribute('aria-labelledby', state.tab === 'perks' ? 'badge-tab' : 'milestones-heading');
            var filtered = select(items, state), result = page(filtered, state.page, state.size); state.page = result.current;
            items.forEach(function (a) { a.el.hidden = true; });
            result.items.forEach(function (a) { a.el.hidden = false; q('#achievement-results').appendChild(a.el); });
            q('[data-result-count]').textContent = filtered.length + ' / ' + (state.tab === 'milestones' ? items.filter(function (a) { return a.coins > 0; }).length : items.length) + ' thành tựu';
            q('[data-page]').textContent = 'Trang ' + result.current + ' / ' + result.pages;
            q('[data-prev]').disabled = result.current === 1; q('[data-next]').disabled = result.current === result.pages;
            q('[data-empty]').hidden = filtered.length !== 0;
            root.querySelectorAll('[role="tab"]').forEach(function (tab) { var active = tab.dataset.tab === state.tab; tab.setAttribute('aria-selected', String(active)); tab.tabIndex = active ? 0 : -1; });
        }
        root.querySelectorAll('[role="tab"]').forEach(function (tab) {
            tab.addEventListener('click', function () { state.tab = tab.dataset.tab; state.page = 1; global.history.replaceState(null, '', '#' + state.tab); render(); });
            tab.addEventListener('keydown', function (event) {
                if (['ArrowLeft', 'ArrowRight', 'Home', 'End'].indexOf(event.key) < 0) return;
                event.preventDefault(); var tabs = Array.from(root.querySelectorAll('[role="tab"]'));
                var next = event.key === 'Home' ? tabs[0] : event.key === 'End' ? tabs[1] : tabs[1 - tabs.indexOf(tab)]; next.click(); next.focus();
            });
        });
        ['search', 'category', 'status', 'sort'].forEach(function (key) { q('[data-' + key + ']').addEventListener(key === 'search' ? 'input' : 'change', function (e) { state[key] = e.target.value; state.page = 1; render(); }); });
        q('[data-page-size]').addEventListener('change', function (e) { state.size = Number(e.target.value); state.page = 1; render(); });
        q('[data-prev]').addEventListener('click', function () { state.page--; render(); });
        q('[data-next]').addEventListener('click', function () { state.page++; render(); });
        q('[data-reset]').addEventListener('click', function () { ['search', 'category', 'status'].forEach(function (key) { state[key] = ''; q('[data-' + key + ']').value = ''; }); state.page = 1; render(); });
        function view(value) {
            root.dataset.view = value === 'list' ? 'list' : 'grid';
            root.querySelectorAll('button[data-view]').forEach(function (button) { button.setAttribute('aria-pressed', String(button.dataset.view === root.dataset.view)); });
        }
        try { view(global.localStorage.getItem('achievement-view')); } catch (_) { view('grid'); }
        root.querySelectorAll('button[data-view]').forEach(function (button) { button.addEventListener('click', function () { view(button.dataset.view); try { global.localStorage.setItem('achievement-view', root.dataset.view); } catch (_) {} }); });
        var queue = Promise.resolve();
        root.addEventListener('submit', function (event) {
            var form = event.target.closest('[data-claim-achievement]'); if (!form) return;
            event.preventDefault();
            if (form.dataset.busy === 'true') return;
            form.dataset.busy = 'true'; var button = form.querySelector('button'), label = button.textContent;
            var body = new URLSearchParams(new FormData(form)); button.disabled = true; button.textContent = 'Đang nhận…';
            queue = queue.then(async function () {
                var message = q('[data-claim-message]');
                try {
                    var response = await global.fetch(form.action, { method: 'POST', credentials: 'same-origin', headers: { Accept: 'application/json', 'X-Requested-With': 'XMLHttpRequest' }, body: body });
                    if (response.redirected) throw new Error('Phiên đăng nhập có thể đã hết hạn. Vui lòng đăng nhập lại.');
                    var data = await response.json();
                    if (!response.ok || !data.claimed) throw new Error(data.message || data.error || 'Chưa thể nhận thưởng. Vui lòng thử lại.');
                    var item = items.find(function (a) { return a.key === body.get('key'); });
                    item.claimed = true; item.el.dataset.claimed = 'true';
                    var pending = items.filter(function (a) { return a.earned && !a.claimed && a.coins > 0; });
                    root.querySelectorAll('[data-claimable-count]').forEach(function (el) { el.textContent = pending.length; });
                    root.querySelectorAll('[data-claimable-coins]').forEach(function (el) { el.textContent = pending.reduce(function (sum, a) { return sum + a.coins; }, 0); });
                    var paidList = items.filter(function (a) { return a.claimed && a.coins > 0; });
                    var paidCoins = Number.isFinite(data.claimedCoins) ? data.claimedCoins : paidList.reduce(function (sum, a) { return sum + a.coins; }, 0);
                    var paidCount = Number.isFinite(data.claimedCount) ? data.claimedCount : paidList.length;
                    document.querySelectorAll('[data-claimed-coins], [data-claimed-coins-sub]').forEach(function (el) { el.textContent = paidCoins.toLocaleString('vi-VN'); });
                    document.querySelectorAll('[data-claimed-count]').forEach(function (el) { el.textContent = String(paidCount); });
                    if (Number.isFinite(data.todayEarned)) document.querySelectorAll('[data-today-earned]').forEach(function (el) { el.textContent = data.todayEarned.toLocaleString('vi-VN'); });
                    if (Number.isFinite(data.totalEarned)) document.querySelectorAll('[data-total-earned]').forEach(function (el) { el.textContent = data.totalEarned.toLocaleString('vi-VN'); });
                    if (data.rewardDate) document.querySelectorAll('[data-reward-date]').forEach(function (el) { el.textContent = data.rewardDate; });
                    var paid = document.createElement('span'); paid.className = 'achievement-claimed'; paid.textContent = '✓ Đã nhận'; form.replaceWith(paid);
                    paid.tabIndex = -1; paid.focus({ preventScroll: true });
                    if (Number.isFinite(data.balance)) {
                        document.querySelectorAll('[data-wallet-balance], .coin-badge-val').forEach(function (el) { el.textContent = data.balance.toLocaleString('vi-VN'); });
                        document.querySelectorAll('.dropdown-coin-tag').forEach(function (el) { el.textContent = data.balance.toLocaleString('vi-VN') + ' xu'; });
                    }
                    message.textContent = data.message;
                    // Keep the paid card in place until the next filter/page action to preserve reading position.
                } catch (error) { message.textContent = error.message || 'Mất kết nối. Bạn có thể thử lại; phần thưởng chỉ được cộng một lần.'; }
                finally { form.dataset.busy = 'false'; button.disabled = false; button.textContent = label; }
            });
        });
        q('[data-browser-controls]').hidden = false; q('[data-pagination]').hidden = false; render();
    }
    if (typeof module !== 'undefined' && module.exports) module.exports = { fold: fold, select: select, page: page, init: init };
    if (typeof document !== 'undefined') document.querySelectorAll('[data-achievement-browser]').forEach(init);
})(typeof window !== 'undefined' ? window : globalThis);
