/**
 * recaptcha.js — Tích hợp Google reCAPTCHA v3 phía client (ISSUE-001 Phase 4).
 *
 * Cơ chế hoạt động:
 * 1. Chạy ngầm hoàn toàn, người thật KHÔNG PHẢI bấm ô tick hay chọn ảnh.
 * 2. Tự động gắn token reCAPTCHA vào 3 cửa nhạy cảm:
 *    - Đăng ký (/auth?action=register)
 *    - Đăng nhập (/auth?action=login)
 *    - Bình luận (/comment?action=add)
 * 3. Nếu reCAPTCHA không tải được hoặc mạng offline: tự động cho qua (fail-open),
 *    không bao giờ làm kẹt nút gửi của người dùng.
 */
(function () {
    'use strict';

    const siteKey = window.RECAPTCHA_SITE_KEY;
    if (!siteKey) return;

    document.addEventListener('submit', function (e) {
        const form = e.target;
        if (!form || form.tagName !== 'FORM') return;
        if ((form.method || '').toLowerCase() !== 'post') return;

        const actionInput = form.querySelector('input[name="action"]');
        const actionVal = actionInput ? actionInput.value : '';
        const formActionAttr = (form.getAttribute('action') || '');

        let recaptchaAction = null;
        if (formActionAttr.includes('/auth') || formActionAttr.endsWith('auth')) {
            if (actionVal === 'register') recaptchaAction = 'register';
            else if (actionVal === 'login') recaptchaAction = 'login';
        } else if (formActionAttr.includes('/comment') || formActionAttr.endsWith('comment')) {
            if (actionVal === 'add') recaptchaAction = 'comment';
        }

        // Không thuộc 3 cửa nhạy cảm -> Cho form gửi bình thường
        if (!recaptchaAction) return;

        // Nếu đã xác thực xong token -> Cho qua
        let tokenInput = form.querySelector('input[name="g-recaptcha-token"]');
        if (tokenInput && tokenInput.value && form.dataset.recaptchaPassed === 'true') {
            return;
        }

        // Chặn submit tạm thời để lấy token reCAPTCHA
        e.preventDefault();
        e.stopPropagation();

        // Nếu thư viện reCAPTCHA không tải được (ví dụ offline, chặn mạng) -> fail-open
        if (typeof grecaptcha === 'undefined') {
            form.dataset.recaptchaPassed = 'true';
            HTMLFormElement.prototype.submit.call(form);
            return;
        }

        try {
            grecaptcha.ready(function () {
                grecaptcha.execute(siteKey, { action: recaptchaAction }).then(function (token) {
                    if (!tokenInput) {
                        tokenInput = document.createElement('input');
                        tokenInput.type = 'hidden';
                        tokenInput.name = 'g-recaptcha-token';
                        form.appendChild(tokenInput);
                    }
                    tokenInput.value = token;
                    form.dataset.recaptchaPassed = 'true';
                    HTMLFormElement.prototype.submit.call(form);
                }).catch(function (err) {
                    console.warn('reCAPTCHA error, fail-open cho phep gui:', err);
                    form.dataset.recaptchaPassed = 'true';
                    HTMLFormElement.prototype.submit.call(form);
                });
            });
        } catch (err) {
            form.dataset.recaptchaPassed = 'true';
            HTMLFormElement.prototype.submit.call(form);
        }
    }, true);
})();
