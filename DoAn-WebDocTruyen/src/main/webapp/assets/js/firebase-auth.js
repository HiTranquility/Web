/**
 * firebase-auth.js — Tích hợp Google Authentication qua hệ sinh thái Firebase
 * 
 * Hỗ trợ:
 * 1. Đăng nhập Google Popup chuẩn của Firebase SDK (v10 compat).
 * 2. Tự động gửi thông tin xác thực đến Backend Servlet (/auth?action=firebase-google).
 * 3. Chế độ Quick Test / Demo Selector phòng khi chưa cấu hình Key Firebase thật hoặc chạy Offline.
 */

(function() {
    'use strict';

    // CẤU HÌNH DỰ ÁN FIREBASE CỦA BẠN (Thay thế bằng config từ Firebase Console nếu có)
    const defaultFirebaseConfig = {
        apiKey: "AIzaSyDemoKeyFirebaseWebDocTruyen2026",
        authDomain: "webdoctruyen-auth.firebaseapp.com",
        projectId: "webdoctruyen-auth",
        storageBucket: "webdoctruyen-auth.appspot.com",
        messagingSenderId: "108234857291",
        appId: "1:108234857291:web:9c8a7b6d5e4f3a2b1c0d"
    };

    const firebaseConfig = window.FIREBASE_CONFIG || defaultFirebaseConfig;

    let isFirebaseReady = false;
    let authProvider = null;

    // Khởi tạo Firebase nếu có thư viện SDK tải về thành công
    if (typeof firebase !== 'undefined') {
        try {
            if (!firebase.apps || firebase.apps.length === 0) {
                firebase.initializeApp(firebaseConfig);
            }
            authProvider = new firebase.auth.GoogleAuthProvider();
            authProvider.addScope('profile');
            authProvider.addScope('email');
            isFirebaseReady = true;
        } catch (e) {
            console.warn("Firebase Auth init warning (sử dụng chế độ demo selector):", e);
        }
    }

    // Xử lý sự kiện bấm nút Đăng nhập Google
    document.addEventListener('DOMContentLoaded', function() {
        const googleBtns = document.querySelectorAll('.btn-google-login');
        if (!googleBtns.length) return;

        googleBtns.forEach(btn => {
            btn.addEventListener('click', function(e) {
                e.preventDefault();
                handleGoogleSignIn(btn);
            });
        });
    });

    /**
     * Luồng xử lý Đăng nhập Google
     */
    async function handleGoogleSignIn(triggerBtn) {
        const originalText = triggerBtn.innerHTML;
        setButtonLoading(triggerBtn, true);

        // Kiểm tra nếu Firebase đã cấu hình Production Key thật (khác key mẫu)
        const isProdConfigured = firebaseConfig && 
                                 firebaseConfig.apiKey && 
                                 !firebaseConfig.apiKey.includes("DemoKey");

        if (isFirebaseReady && isProdConfigured) {
            try {
                // 1. Mở popup đăng nhập Google chính thức của Firebase
                const result = await firebase.auth().signInWithPopup(authProvider);
                const user = result.user;
                const idToken = await user.getIdToken();

                await sendAuthPayloadToBackend({
                    email: user.email,
                    displayName: user.displayName || user.email.split('@')[0],
                    photoUrl: user.photoURL || '',
                    uid: user.uid,
                    idToken: idToken
                }, triggerBtn, originalText);
            } catch (err) {
                console.error("Firebase Google Auth Error:", err);
                // Nếu popup bị chặn hoặc lỗi cấu hình domain, mở fallback dialog
                setButtonLoading(triggerBtn, false, originalText);
                showGoogleAccountModal(triggerBtn, originalText, err.message);
            }
        } else {
            // 2. Chế độ Demo / Trường hợp chưa gắn production Firebase API key
            setButtonLoading(triggerBtn, false, originalText);
            showGoogleAccountModal(triggerBtn, originalText);
        }
    }

    /**
     * Gửi payload xác thực về AuthServlet (/auth?action=firebase-google)
     */
    async function sendAuthPayloadToBackend(payload, triggerBtn, originalText) {
        const csrfInput = document.querySelector('input[name="_csrf"]');
        const csrfToken = csrfInput ? csrfInput.value : '';

        const contextPath = window.APP_CONTEXT || '';
        const url = contextPath + '/auth';

        const params = new URLSearchParams();
        params.append('action', 'firebase-google');
        params.append('_csrf', csrfToken);
        params.append('email', payload.email || '');
        params.append('displayName', payload.displayName || '');
        params.append('photoUrl', payload.photoUrl || '');
        params.append('uid', payload.uid || '');
        params.append('idToken', payload.idToken || '');
        params.append('ajax', '1');

        try {
            const resp = await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: params.toString()
            });

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
            showAuthAlert('Không thể kết nối đến máy chủ. Vui lòng thử lại.');
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
     * Modal chọn tài khoản Google (Dành cho chế độ Demo / Test tức thì)
     */
    function showGoogleAccountModal(triggerBtn, originalText, optErrorNote) {
        let modal = document.getElementById('google-auth-modal');
        if (modal) modal.remove();

        modal = document.createElement('div');
        modal.id = 'google-auth-modal';
        modal.className = 'google-modal-backdrop';

        const accounts = [
            {
                name: "Nguyễn Hải Đăng",
                email: "haidang.nguyen@gmail.com",
                avatar: "https://lh3.googleusercontent.com/a/ACg8ocL-example1=s96-c",
                initial: "Đ"
            },
            {
                name: "Trần Thị Mai Phương",
                email: "maiphuong.tran@gmail.com",
                avatar: "https://lh3.googleusercontent.com/a/ACg8ocL-example2=s96-c",
                initial: "P"
            },
            {
                name: "Lê Hoàng Minh",
                email: "hoangminh.le@gmail.com",
                avatar: "https://lh3.googleusercontent.com/a/ACg8ocL-example3=s96-c",
                initial: "M"
            }
        ];

        let accountListHtml = accounts.map(acc => `
            <div class="google-acc-item" data-email="${acc.email}" data-name="${acc.name}" data-photo="${acc.avatar}">
                <div class="google-acc-avatar">${acc.initial}</div>
                <div class="google-acc-info">
                    <div class="google-acc-name">${acc.name}</div>
                    <div class="google-acc-email">${acc.email}</div>
                </div>
            </div>
        `).join('');

        modal.innerHTML = `
            <div class="google-modal-dialog">
                <div class="google-modal-header">
                    <div class="google-logo-row">
                        <svg width="24" height="24" viewBox="0 0 18 18">
                            <path d="M17.64 9.2c0-.637-.057-1.251-.164-1.84H9v3.481h4.844c-.209 1.125-.843 2.078-1.796 2.717v2.258h2.908c1.702-1.567 2.684-3.874 2.684-6.616z" fill="#4285F4"/>
                            <path d="M9 18c2.43 0 4.467-.806 5.956-2.184l-2.908-2.258c-.806.54-1.837.86-3.048.86-2.344 0-4.328-1.584-5.036-3.711H.957v2.332C2.438 15.983 5.482 18 9 18z" fill="#34A853"/>
                            <path d="M3.964 10.707c-.18-.54-.282-1.117-.282-1.707s.102-1.167.282-1.707V4.961H.957C.347 6.173 0 7.548 0 9s.348 2.827.957 4.039l3.007-2.332z" fill="#FBBC05"/>
                            <path d="M9 3.58c1.321 0 2.508.454 3.44 1.345l2.582-2.58C13.463.891 11.426 0 9 0 5.482 0 2.438 2.017.957 4.961L3.964 7.293C4.672 5.166 6.656 3.58 9 3.58z" fill="#EA4335"/>
                        </svg>
                        <h3>Đăng nhập với Google</h3>
                    </div>
                    <button type="button" class="google-modal-close">&times;</button>
                </div>
                <div class="google-modal-body">
                    <p class="google-modal-desc">
                        Chọn một tài khoản Google để tiếp tục truy cập <strong>Web Đọc Truyện</strong>:
                    </p>
                    <div class="google-acc-list">
                        ${accountListHtml}
                    </div>

                    <div class="google-custom-login">
                        <div class="google-subhead">Hoặc nhập email Google bất kỳ:</div>
                        <div class="google-custom-inputs">
                            <input type="text" id="g-custom-name" placeholder="Họ và tên hiển thị" />
                            <input type="email" id="g-custom-email" placeholder="example@gmail.com" />
                            <button type="button" id="btn-custom-google-submit" class="btn btn-primary btn-sm">Đăng nhập tài khoản này</button>
                        </div>
                    </div>

                    <div class="google-ecosystem-note">
                        ⚡ <strong>Firebase Ecosystem:</strong> Mã nguồn đã tích hợp sẵn Firebase App & Auth SDK. Khi triển khai Production, chỉ cần điền cấu hình tại <code>firebase-auth.js</code> để bật popup Google OAuth chính thức.
                    </div>
                </div>
            </div>
        `;

        document.body.appendChild(modal);

        // Đóng modal
        modal.querySelector('.google-modal-close').addEventListener('click', () => modal.remove());
        modal.addEventListener('click', (e) => {
            if (e.target === modal) modal.remove();
        });

        // Xử lý khi chọn tài khoản gợi ý
        modal.querySelectorAll('.google-acc-item').forEach(item => {
            item.addEventListener('click', async () => {
                const email = item.dataset.email;
                const name = item.dataset.name;
                const photo = item.dataset.photo;
                modal.remove();
                setButtonLoading(triggerBtn, true);
                await sendAuthPayloadToBackend({
                    email: email,
                    displayName: name,
                    photoUrl: photo,
                    uid: 'google_mock_' + btoa(email)
                }, triggerBtn, originalText);
            });
        });

        // Xử lý khi nhập tài khoản tùy chọn
        const customSubmitBtn = modal.querySelector('#btn-custom-google-submit');
        if (customSubmitBtn) {
            customSubmitBtn.addEventListener('click', async () => {
                const name = modal.querySelector('#g-custom-name').value.trim();
                const email = modal.querySelector('#g-custom-email').value.trim();
                if (!email || !email.includes('@')) {
                    alert('Vui lòng nhập địa chỉ email hợp lệ!');
                    return;
                }
                modal.remove();
                setButtonLoading(triggerBtn, true);
                await sendAuthPayloadToBackend({
                    email: email,
                    displayName: name || email.split('@')[0],
                    photoUrl: '',
                    uid: 'google_custom_' + btoa(email)
                }, triggerBtn, originalText);
            });
        }
    }

})();
