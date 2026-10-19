// Chạy hoàn toàn local: node --test scripts/audit-web.test.cjs
const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const root = path.resolve(__dirname, '..', 'src/main/webapp');

function modalFixture() {
    const events = {};
    const elements = new Map();
    const body = { style: {}, appendChild() {} };
    for (const id of ['appConfirmModalBackdrop', 'confirmModalTitle', 'confirmModalMessage',
        'confirmModalOkBtn', 'confirmModalCancelBtn', 'confirmModalIconWrap', 'confirmModalIcon']) {
        const classes = new Set();
        elements.set(id, { parentElement: body, classList: {
            add: c => classes.add(c), remove: c => classes.delete(c), contains: c => classes.has(c)
        } });
    }
    const document = { body, getElementById: id => elements.get(id),
        addEventListener: (type, fn) => { events[type] = fn; }, createElement: () => ({}) };
    const window = { confirm: () => true, location: {} };
    const source = fs.readFileSync(path.join(root, 'WEB-INF/views/_partials/_confirm-modal.jsp'), 'utf8');
    vm.runInNewContext(source.match(/<script>([\s\S]*?)<\/script>/)[1], { document, window, Promise });
    return { events, elements, window };
}

test('Logout confirms once, sends POST form and requires confirmation on next attempt', async () => {
    const f = modalFixture();
    let submits = 0;
    const form = { dataset: {}, checkValidity: () => true, hasAttribute: () => false,
        requestSubmit(button) {
            assert.equal(button, target);
            f.events.submit({ target: form });
            submits++;
        }
    };
    const attrs = { 'data-confirm': 'Đăng xuất?', 'data-confirm-title': 'Xác nhận đăng xuất',
        'data-confirm-ok': 'Đăng xuất' };
    const target = { tagName: 'BUTTON', type: 'submit', dataset: {},
        closest: selector => selector === 'form' ? form : target,
        getAttribute: name => attrs[name] || null };
    for (const ok of [true, false, true]) {
        let prevented = false;
        f.events.click({ target, preventDefault() { prevented = true; }, stopPropagation() {} });
        assert.ok(prevented);
        assert.equal(f.elements.get('confirmModalMessage').textContent, 'Đăng xuất?');
        f.window.closeConfirmModal(ok);
        await Promise.resolve();
    }
    assert.equal(submits, 2);
});

test('A second modal cancels the previous pending action', async () => {
    const f = modalFixture();
    const first = f.window.showConfirmModal({ message: 'First' });
    const second = f.window.showConfirmModal({ message: 'Second' });
    assert.equal(await first, false);
    f.window.closeConfirmModal(true);
    assert.equal(await second, true);
});

test('Forms retain submitter and submit event after confirmation', async () => {
    const f = modalFixture();
    const button = { name: 'action', value: 'delete' };
    let submitted = false;
    const form = { dataset: {}, hasAttribute: () => true, checkValidity: () => true,
        getAttribute: () => 'Xoá?', requestSubmit(submitter) {
            assert.equal(submitter, button);
            f.events.submit({ target: form, submitter });
            submitted = true;
        } };
    f.events.submit({ target: form, submitter: button, preventDefault() {}, stopPropagation() {} });
    f.window.closeConfirmModal(true);
    await Promise.resolve();
    assert.ok(submitted);
    assert.equal(form.dataset.confirmed, 'false');
});

function workerFixture() {
    const events = {};
    const deleted = [];
    const cached = [];
    const self = { registration: { scope: 'http://localhost/app/' },
        addEventListener: (name, fn) => { events[name] = fn; }, skipWaiting() {}, clients: { claim() {} } };
    const caches = { keys: async () => ['doctruyen-pwa-v3', 'another-application'],
        delete: async name => { deleted.push(name); },
        open: async () => ({ put: (request) => cached.push(request.url), addAll: async () => {} }),
        match: async () => undefined };
    const network = { status: 200, type: 'basic', redirected: false,
        headers: { get: () => '' }, clone() { return this; } };
    vm.runInNewContext(fs.readFileSync(path.join(root, 'sw.js'), 'utf8'),
        { self, caches, URL, Response, fetch: async () => network });
    return { events, deleted, cached, network };
}

test('Service worker never intercepts pages, API, uploads or cross-origin requests', () => {
    const f = workerFixture();
    for (const url of ['http://localhost/app/', 'http://localhost/app/auth', 'http://localhost/app/chapter?id=1',
        'http://localhost/app/api/chapter/1', 'http://localhost/app/uploads/private.png', 'https://example.test/assets/a.js']) {
        f.events.fetch({ request: { method: 'GET', url }, respondWith() { assert.fail(url); } });
    }
    f.events.fetch({ request: { method: 'POST', url: 'http://localhost/app/assets/a.js' },
        respondWith() { assert.fail('POST intercepted'); } });
});

test('Service worker caches only successful static responses and respects no-store', async () => {
    const f = workerFixture();
    const request = { method: 'GET', url: 'http://localhost/app/assets/main.css' };
    let response;
    f.events.fetch({ request, respondWith(promise) { response = promise; } });
    await response; await Promise.resolve();
    assert.equal(f.cached.length, 1);
    f.network.headers.get = () => 'private, no-store';
    f.events.fetch({ request, respondWith(promise) { response = promise; } });
    await response; await Promise.resolve();
    assert.equal(f.cached.length, 1);
});

test('Service worker update deletes old account caches without deleting other apps', async () => {
    const f = workerFixture();
    let activation;
    f.events.activate({ waitUntil(promise) { activation = promise; } });
    await activation;
    assert.deepEqual(f.deleted, ['doctruyen-pwa-v3']);
});

test('Static JavaScript parses', () => {
    for (const name of fs.readdirSync(path.join(root, 'assets/js'))) {
        if (!name.endsWith('.js')) continue;
        const source = fs.readFileSync(path.join(root, 'assets/js', name), 'utf8');
        // ES modules are checked separately by node --check; normal scripts parse here.
        if (!/^\s*(?:import|export)\s/m.test(source)) new vm.Script(source, { filename: name });
    }
});

if (process.env.AUDIT_RENDERED_DIR) {
    test('Inline scripts in actual rendered JSP pages parse', () => {
        let scripts = 0;
        for (const name of fs.readdirSync(process.env.AUDIT_RENDERED_DIR)) {
            const html = fs.readFileSync(path.join(process.env.AUDIT_RENDERED_DIR, name), 'utf8');
            for (const match of html.matchAll(/<script\b([^>]*)>([\s\S]*?)<\/script>/gi)) {
                if (/\bsrc\s*=|\btype\s*=\s*['"]module['"]/i.test(match[1])) continue;
                if (/application\/ld\+json/i.test(match[1])) JSON.parse(match[2]);
                else new vm.Script(match[2], { filename: name + '-inline-' + scripts });
                scripts++;
            }
        }
        assert.ok(scripts > 0, 'No rendered scripts checked');
    });
}
