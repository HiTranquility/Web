/**
 * ============================================================================
 *  drive-backup.js — Sao lưu truyện lên Google Drive cho tác giả (ISSUE-001 Phase 5)
 * ============================================================================
 *  Quy tắc:
 *  1. Scope drive.file: Chỉ xin quyền trên các file do ứng dụng này tạo ra.
 *  2. Hiển thị tiến độ từng chương (ví dụ: đang tải 7/29 chương...) để tác giả
 *     không phải chờ đợi trong hoang mang và không bị browser timeout.
 *  3. Bấm lại lần sau sẽ tự động ghi đè, không sinh file trùng lặp (1), (2).
 */
(function () {
    'use strict';

    var tokenClient = null;
    var activeStoryId = 0;
    var activeStoryTitle = '';
    var isRunning = false;
    var modalEl = null;

    function getContextPath() {
        return window.APP_CONTEXT || '';
    }

    function getCsrfToken() {
        var csrfInput = document.querySelector('input[name="_csrf"]');
        return csrfInput ? csrfInput.value : (window.CSRF_TOKEN || '');
    }

    function getClientId() {
        return window.GOOGLE_CLIENT_ID || '';
    }

    function initModal() {
        if (modalEl) return;

        modalEl = document.createElement('div');
        modalEl.className = 'drive-modal-backdrop';
        modalEl.style.display = 'none';
        modalEl.innerHTML = [
            '<div class="drive-modal-card" role="dialog" aria-modal="true">',
            '  <div class="drive-modal-head">',
            '    <h3>☁️ Sao lưu Google Drive</h3>',
            '    <button type="button" class="drive-modal-close-btn" id="drive-modal-close" aria-label="Đóng">&times;</button>',
            '  </div>',
            '  <div class="drive-modal-sub" id="drive-modal-sub">Thư mục: DocTruyen/...</div>',
            '  <div class="drive-progress-track">',
            '    <div class="drive-progress-fill" id="drive-progress-fill" style="width: 0%;"></div>',
            '  </div>',
            '  <div class="drive-progress-meta">',
            '    <span id="drive-progress-status">Sẵn sàng...</span>',
            '    <span id="drive-progress-pct">0%</span>',
            '  </div>',
            '  <div class="drive-log-box" id="drive-log-box"></div>',
            '  <div class="drive-modal-foot">',
            '    <button type="button" class="btn btn-ghost btn-sm" id="drive-modal-cancel">Đóng</button>',
            '  </div>',
            '</div>'
        ].join('\n');

        document.body.appendChild(modalEl);

        var closeBtn = document.getElementById('drive-modal-close');
        var cancelBtn = document.getElementById('drive-modal-cancel');

        function hideModal() {
            if (isRunning) {
                if (!confirm('Quá trình sao lưu đang diễn ra. Bạn có chắc muốn đóng cửa sổ?')) {
                    return;
                }
                isRunning = false;
            }
            modalEl.style.display = 'none';
        }

        closeBtn.addEventListener('click', hideModal);
        cancelBtn.addEventListener('click', hideModal);
    }

    function logMessage(msg) {
        var box = document.getElementById('drive-log-box');
        if (!box) return;
        var line = document.createElement('div');
        line.textContent = msg;
        box.appendChild(line);
        box.scrollTop = box.scrollHeight;
    }

    function updateProgress(current, total, detailText) {
        var fill = document.getElementById('drive-progress-fill');
        var status = document.getElementById('drive-progress-status');
        var pct = document.getElementById('drive-progress-pct');

        var percent = total > 0 ? Math.round((current / total) * 100) : 0;
        if (fill) fill.style.width = percent + '%';
        if (pct) pct.textContent = percent + '%';
        if (status) {
            status.textContent = total > 0
                ? 'Đang tải ' + current + '/' + total + ' chương…'
                : 'Đang chuẩn bị…';
        }
        if (detailText) {
            logMessage('-> ' + detailText);
        }
    }

    function ensureGsiScript(callback) {
        if (window.google && window.google.accounts && window.google.accounts.oauth2) {
            callback();
            return;
        }

        var existing = document.getElementById('google-gsi-script');
        if (existing) {
            existing.addEventListener('load', callback);
            return;
        }

        var script = document.createElement('script');
        script.id = 'google-gsi-script';
        script.src = 'https://accounts.google.com/gsi/client';
        script.async = true;
        script.defer = true;
        script.onload = callback;
        script.onerror = function () {
            alert('Không thể tải Google Identity Services. Vui lòng kiểm tra kết nối mạng.');
        };
        document.head.appendChild(script);
    }

    function triggerBackup(storyId, storyTitle) {
        var clientId = getClientId();
        if (!clientId) {
            alert('Chưa cấu hình Google Client ID (google.client_id trong google.properties). Vui lòng cấu hình trước khi dùng tính năng Google Drive.');
            return;
        }

        activeStoryId = storyId;
        activeStoryTitle = storyTitle;

        initModal();
        var sub = document.getElementById('drive-modal-sub');
        if (sub) sub.textContent = 'Thư mục: DocTruyen/' + storyTitle + '/';
        document.getElementById('drive-log-box').innerHTML = '';
        updateProgress(0, 0, 'Đang mở màn hình xin quyền Google Drive (scope drive.file)...');
        modalEl.style.display = 'flex';

        ensureGsiScript(function () {
            tokenClient = google.accounts.oauth2.initTokenClient({
                client_id: clientId,
                scope: 'https://www.googleapis.com/auth/drive.file',
                callback: function (response) {
                    if (response.error) {
                        // Ca 2: Người dùng bấm Huỷ ở màn hình xin quyền
                        logMessage('⚠️ Bạn đã huỷ cấp quyền hoặc phiên làm việc đã hết.');
                        document.getElementById('drive-progress-status').textContent = 'Đã huỷ cấp quyền.';
                        return;
                    }
                    if (response.access_token) {
                        startUploadProcess(storyId, storyTitle, response.access_token);
                    }
                },
                error_callback: function (err) {
                    logMessage('⚠️ Huỷ hoặc lỗi mở cửa sổ Google: ' + (err.message || 'Huỷ'));
                    document.getElementById('drive-progress-status').textContent = 'Đã dừng.';
                }
            });

            tokenClient.requestAccessToken({ prompt: '' });
        });
    }

    function startUploadProcess(storyId, storyTitle, accessToken) {
        isRunning = true;
        var ctx = getContextPath();

        logMessage('✓ Đã nhận quyền truy cập Google Drive.');
        logMessage('Đang kiểm tra danh sách chương của truyện...');

        // Bước 1: Lấy danh sách chương
        fetch(ctx + '/drive?action=chapters&storyId=' + encodeURIComponent(storyId), {
            headers: { 'Accept': 'application/json' }
        })
        .then(function (res) {
            if (res.status === 401) {
                window.location.href = ctx + '/auth?action=login';
                throw new Error('Chưa đăng nhập');
            }
            if (res.status === 403) {
                throw new Error('Bạn không có quyền sao lưu truyện này (403 Forbidden).');
            }
            return res.json();
        })
        .then(function (data) {
            if (!isRunning) return;

            if (!data.success) {
                throw new Error(data.message || 'Lỗi nạp dữ liệu chương.');
            }

            var chapters = data.chapters || [];
            if (chapters.length === 0) {
                // Ca 3: Truyện 0 chương
                logMessage('⚠️ Truyện chưa có chương nào để sao lưu.');
                document.getElementById('drive-progress-status').textContent = 'Truyện chưa có chương nào.';
                isRunning = false;
                return;
            }

            logMessage('Tìm thấy ' + chapters.length + ' chương. Đang chuẩn bị thư mục Drive...');

            // Bước 2: Khởi tạo thư mục DocTruyen/<tên truyện>
            var initBody = 'action=init&storyId=' + encodeURIComponent(storyId)
                + '&accessToken=' + encodeURIComponent(accessToken)
                + '&_csrf=' + encodeURIComponent(getCsrfToken());

            return fetch(ctx + '/drive', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: initBody
            })
            .then(function (initRes) { return initRes.json(); })
            .then(function (initData) {
                if (!initData.success) {
                    throw new Error(initData.message || 'Không thể tạo thư mục trên Drive.');
                }
                var folderId = initData.folderId;
                logMessage('✓ Thư mục sẵn sàng. Bắt đầu tải các chương lên...');

                // Bước 3: Tải từng chương một theo tiến độ
                return uploadChaptersSequentially(storyId, chapters, folderId, accessToken, 0);
            });
        })
        .catch(function (err) {
            isRunning = false;
            logMessage('❌ ' + err.message);
            var status = document.getElementById('drive-progress-status');
            if (status) status.textContent = 'Gặp lỗi khi sao lưu.';
        });
    }

    function uploadChaptersSequentially(storyId, chapters, folderId, accessToken, index) {
        if (!isRunning) return;

        if (index >= chapters.length) {
            // Hoàn thành 100%
            isRunning = false;
            updateProgress(chapters.length, chapters.length, null);
            logMessage('🎉 Hoàn thành! Toàn bộ ' + chapters.length + ' chương đã được sao lưu vào thư mục: DocTruyen/' + activeStoryTitle + '/');
            document.getElementById('drive-progress-status').textContent = 'Sao lưu thành công!';
            if (window.showToast) {
                window.showToast('Đã sao lưu ' + chapters.length + ' chương lên Google Drive thành công!');
            }
            return;
        }

        var c = chapters[index];
        var curNum = index + 1;
        var total = chapters.length;

        updateProgress(curNum, total, 'Đang tải chương ' + curNum + '/' + total + ': ' + c.fileName);

        var body = 'action=backup&storyId=' + encodeURIComponent(storyId)
            + '&chapterId=' + encodeURIComponent(c.id)
            + '&folderId=' + encodeURIComponent(folderId)
            + '&accessToken=' + encodeURIComponent(accessToken)
            + '&_csrf=' + encodeURIComponent(getCsrfToken());

        fetch(getContextPath() + '/drive', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: body
        })
        .then(function (res) {
            if (!res.ok) {
                throw new Error('Lỗi HTTP ' + res.status + ' ở chương ' + curNum);
            }
            return res.json();
        })
        .then(function (resData) {
            if (!resData.success) {
                throw new Error(resData.message || ('Lỗi lưu chương ' + curNum));
            }
            // Đệ quy tiếp tục chương kế tiếp
            uploadChaptersSequentially(storyId, chapters, folderId, accessToken, index + 1);
        })
        .catch(function (err) {
            // Ca 4: Rút mạng giữa chừng hoặc lỗi tải chương
            isRunning = false;
            logMessage('❌ ' + err.message);
            logMessage('⚠️ Đã dừng ở chương ' + curNum + '/' + total + '. Bạn có thể bấm "Sao lưu Drive" lại để chạy tiếp mà không bị trùng lặp file.');
            document.getElementById('drive-progress-status').textContent = 'Đã dừng ở chương ' + curNum + '/' + total;
        });
    }

    // Đăng ký sự kiện click cho các nút .btn-drive-backup
    document.addEventListener('DOMContentLoaded', function () {
        document.body.addEventListener('click', function (e) {
            var btn = e.target.closest('.btn-drive-backup');
            if (!btn) return;
            e.preventDefault();

            var storyId = btn.getAttribute('data-story-id');
            var storyTitle = btn.getAttribute('data-story-title') || 'Truyện';
            if (storyId) {
                triggerBackup(storyId, storyTitle);
            }
        });
    });

    window.triggerDriveBackup = triggerBackup;
})();
