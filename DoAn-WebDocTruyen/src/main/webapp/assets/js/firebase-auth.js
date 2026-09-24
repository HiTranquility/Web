/**
 * firebase-auth.js — Tích hợp Google Authentication qua Firebase SDK (v10 compat)
 * 
 * BẢO MẬT (Chặn đứng bug-001):
 * 1. Mở popup đăng nhập Google thật qua Firebase SDK.
 * 2. Đọc idToken đã được Google ký số.
 * 3. Gửi DUY NHẤT idToken về máy chủ (/auth?action=firebase-google).
 * 4. Tuyệt đối KHÔNG gửi email/uid do client khai báo.
 * 5. Đã gỡ bỏ toàn bộ modal chọn tài khoản demo và ô nhập email tự do.
 */

(function() {
    'use strict';

    const firebaseConfig = window.FIREBASE_CONFIG || null;

    let isFirebaseReady = false;
    let authProvider = null;

    // Khởi tạo Firebase SDK khi có thư viện và cấu hình hợp lệ từ máy chủ
    if (typeof firebase !== 'undefined' && firebaseConfig && firebaseConfig.apiKey) {
        try {
            if (!firebase.apps || firebase.apps.length === 0) {
                firebase.initializeApp(firebaseConfig);
            }
            authProvider = new firebase.auth.GoogleAuthProvider();
            authProvider.addScope('profile');
            authProvider.addScope('email');
            isFirebaseReady = true;
        } catch (e) {
            console.error("Lỗi khởi tạo Firebase SDK:", e);
        }
    }

    document.addEventListener('DOMContentLoaded', function() {
        // Nút đăng nhập/đăng ký bằng Google (trang auth)
        const googleBtns = document.querySelectorAll('.btn-google-login');
        if (googleBtns.length) {
            googleBtns.forEach(btn => {
                btn.addEventListener('click', function(e) {
                    e.preventDefault();
                    handleGoogleSignIn(btn);
                });
            });
        }

        // Nút liên kết tài khoản Google (trang sửa hồ sơ /user?action=edit)
        const linkBtn = document.getElementById('btn-link-google');
        if (linkBtn) {
            linkBtn.addEventListener('click', function(e) {
                e.preventDefault();
                handleGoogleLink(linkBtn);
            });
        }
    });

    /**
     * Luồng mở popup đăng nhập Google chính thức
     */
    async function handleGoogleSignIn(triggerBtn) {
        const originalText = triggerBtn.innerHTML;

        if (!isFirebaseReady || !authProvider) {
            showAuthAlert('Chức năng đăng nhập Google hiện chưa được cấu hình đầy đủ trên hệ thống.');
            return;
        }

        setButtonLoading(triggerBtn, true);

        try {
            // 1. Mở popup Google Sign-In chính thức
            const result = await firebase.auth().signInWithPopup(authProvider);
            const user = result.user;
            if (!user) {
                throw new Error("Không nhận được thông tin xác thực từ Google.");
            }

            // 2. Lấy idToken chuẩn OIDC
            const idToken = await user.getIdToken();

            // 3. Gửi idToken về Backend để máy chủ tự kiểm tra chữ ký
            await sendAuthPayloadToBackend({ idToken: idToken }, triggerBtn, originalText);

        } catch (err) {
            console.error("Firebase Google Auth Error:", err);
            setButtonLoading(triggerBtn, false, originalText);

            if (err.code === 'auth/popup-closed-by-user') {
                showAuthAlert('Bạn đã đóng cửa sổ đăng nhập Google.');
            } else if (err.code === 'auth/configuration-not-found' || err.code === 'auth/operation-not-allowed') {
                showAuthAlert('Chưa bật tính năng đăng nhập Google trong Firebase Console. Hãy vào Firebase Console ➔ Authentication ➔ Sign-in method ➔ Bật (Enable) nhà cung cấp Google.');
            } else if (err.code === 'auth/unauthorized-domain') {
                showAuthAlert('Tên miền này chưa được cấp phép trong Firebase Console (Authorized Domains).');
            } else if (err.code === 'auth/popup-blocked') {
                showAuthAlert('Trình duyệt đã chặn cửa sổ bật lên (popup). Vui lòng cho phép popup để tiếp tục.');
            } else {
                showAuthAlert(err.message || 'Đăng nhập Google thất bại. Vui lòng thử lại.');
            }
        }
    }

    /**
     * Gửi idToken về AuthServlet (/auth?action=firebase-google)
     */
    async function sendAuthPayloadToBackend(payload, triggerBtn, originalText) {
        const csrfInput = document.querySelector('input[name="_csrf"]');
        const csrfToken = csrfInput ? csrfInput.value : '';

        const contextPath = window.APP_CONTEXT || '';
        const url = contextPath + '/auth';

        const params = new URLSearchParams();
        params.append('action', 'firebase-google');
        params.append('_csrf', csrfToken);
        params.append('idToken', payload.idToken || '');
        params.append('ajax', '1');

        try {
            const resp = await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
                    'X-Requested-With': 'XMLHttpRequest',
                    'X-CSRF-Token': csrfToken
                },
                body: params.toString()
            });

            if (!resp.ok) {
                let errMsg = 'Lỗi máy chủ (' + resp.status + ').';
                try {
                    const errJson = await resp.json();
                    if (errJson && errJson.message) errMsg = errJson.message;
                } catch (_) {}
                setButtonLoading(triggerBtn, false, originalText);
                showAuthAlert(errMsg);
                return;
            }

            const data = await resp.json();

            if (data.success) {
                triggerBtn.innerHTML = `<span>✓ Đăng nhập thành công!</span>`;
                triggerBtn.style.background = '#059669';
                triggerBtn.style.color = '#ffffff';
                triggerBtn.style.borderColor = '#059669';
                setTimeout(() => {
                    window.location.href = data.redirect || (contextPath + '/');
                }, 600);
            } else {
                setButtonLoading(triggerBtn, false, originalText);
                showAuthAlert(data.message || 'Đăng nhập Google thất bại.');
            }
        } catch (e) {
            console.error("Backend auth communication error:", e);
            setButtonLoading(triggerBtn, false, originalText);
            showAuthAlert('Không thể kết nối đến máy chủ xác thực. Vui lòng thử lại.');
        }
    }

    function setButtonLoading(btn, loading, originalText) {
        if (loading) {
            btn.disabled = true;
            btn.dataset.prevHtml = btn.innerHTML;
            btn.innerHTML = `<span class="google-spinner"></span> Đang kết nối Google...`;
        } else {
            btn.disabled = false;
            btn.innerHTML = originalText || btn.dataset.prevHtml || 'Đăng nhập với Google';
        }
    }

    function showAuthAlert(msg) {
        let errBox = document.querySelector('.form-error');
        if (!errBox) {
            errBox = document.createElement('p');
            errBox.className = 'form-error';
            const form = document.querySelector('form');
            if (form) form.parentNode.insertBefore(errBox, form);
        }
        errBox.textContent = msg;
    }

    /**
     * Luồng liên kết tài khoản Google ở trang hồ sơ cá nhân
     */
    async function handleGoogleLink(triggerBtn) {
        const originalText = triggerBtn.innerHTML;

        if (!isFirebaseReady || !authProvider) {
            showLinkAlert('Chức năng liên kết Google hiện chưa được cấu hình đầy đủ trên hệ thống.', true);
            return;
        }

        setButtonLoading(triggerBtn, true);

        try {
            const result = await firebase.auth().signInWithPopup(authProvider);
            const user = result.user;
            if (!user) {
                throw new Error("Không nhận được thông tin xác thực từ Google.");
            }

            const idToken = await user.getIdToken();
            await sendLinkPayloadToBackend({ idToken: idToken }, triggerBtn, originalText);

        } catch (err) {
            console.error("Firebase Link Google Error:", err);
            setButtonLoading(triggerBtn, false, originalText);

            if (err.code === 'auth/popup-closed-by-user') {
                showLinkAlert('Bạn đã đóng cửa sổ liên kết Google.', true);
            } else if (err.code === 'auth/configuration-not-found' || err.code === 'auth/operation-not-allowed') {
                showLinkAlert('Chưa bật tính năng đăng nhập Google trong Firebase Console. Hãy vào Firebase Console ➔ Authentication ➔ Sign-in method ➔ Bật (Enable) nhà cung cấp Google.', true);
            } else if (err.code === 'auth/unauthorized-domain') {
                showLinkAlert('Tên miền này chưa được cấp phép trong Firebase Console (Authorized Domains).', true);
            } else if (err.code === 'auth/popup-blocked') {
                showLinkAlert('Trình duyệt đã chặn cửa sổ popup. Vui lòng cấp quyền popup để tiếp tục.', true);
            } else {
                showLinkAlert(err.message || 'Liên kết Google thất bại. Vui lòng thử lại.', true);
            }
        }
    }

    /**
     * Gửi idToken lên UserServlet (/user?action=link-google)
     */
    async function sendLinkPayloadToBackend(payload, triggerBtn, originalText) {
        const csrfInput = document.querySelector('input[name="_csrf"]');
        const csrfToken = csrfInput ? csrfInput.value : '';

        const contextPath = window.APP_CONTEXT || '';
        const url = contextPath + '/user';

        const params = new URLSearchParams();
        params.append('action', 'link-google');
        params.append('_csrf', csrfToken);
        params.append('idToken', payload.idToken || '');
        params.append('ajax', '1');

        try {
            const resp = await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
                    'X-Requested-With': 'XMLHttpRequest',
                    'X-CSRF-Token': csrfToken
                },
                body: params.toString()
            });

            if (!resp.ok) {
                let errMsg = 'Lỗi máy chủ (' + resp.status + ').';
                try {
                    const errJson = await resp.json();
                    if (errJson && errJson.message) errMsg = errJson.message;
                } catch (_) {}
                setButtonLoading(triggerBtn, false, originalText);
                showLinkAlert(errMsg, true);
                return;
            }

            const data = await resp.json();

            if (data.success) {
                triggerBtn.innerHTML = `<span>✓ Đã liên kết thành công!</span>`;
                triggerBtn.style.background = '#059669';
                triggerBtn.style.color = '#ffffff';
                triggerBtn.style.borderColor = '#059669';
                showLinkAlert(data.message || 'Liên kết Google thành công!', false);
                setTimeout(() => {
                    window.location.reload();
                }, 800);
            } else {
                setButtonLoading(triggerBtn, false, originalText);
                showLinkAlert(data.message || 'Liên kết Google thất bại.', true);
            }
        } catch (e) {
            console.error("Backend link error:", e);
            setButtonLoading(triggerBtn, false, originalText);
            showLinkAlert('Không thể kết nối đến máy chủ. Vui lòng thử lại.', true);
        }
    }

    function showLinkAlert(msg, isError) {
        let box = document.getElementById('google-link-feedback');
        if (!box) {
            box = document.createElement('div');
            box.id = 'google-link-feedback';
            box.style.marginTop = '8px';
            const linkBtn = document.getElementById('btn-link-google');
            if (linkBtn && linkBtn.parentNode) {
                linkBtn.parentNode.appendChild(box);
            }
        }
        var escapeDiv = document.createElement('div');
        escapeDiv.textContent = msg;
        box.innerHTML = '<div class="panel ' + (isError ? 'panel-err' : 'panel-ok') + '" style="margin:6px 0; padding:8px 12px; font-size:0.9em;">' + escapeDiv.innerHTML + '</div>';
    }

})();
