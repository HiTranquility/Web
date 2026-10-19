<%@ page pageEncoding="UTF-8" %>
<%--
================================================================================
  _confirm-modal.jsp — HỘP THOẠI XÁC NHẬN HIỆN ĐẠI (MODERN CONFIRM DIALOG)
================================================================================
  Thay thế toàn bộ window.confirm() trình duyệt mặc định bằng giao diện Modal cao cấp.
  Tự động bắt sự kiện cho các form hoặc nút có thuộc tính `data-confirm="..."`.
================================================================================
--%>
<div class="app-modal-backdrop confirm-dialog-backdrop" id="appConfirmModalBackdrop" onclick="if(event.target===this) closeConfirmModal(false);">
    <div class="app-modal-card confirm-dialog-card" role="dialog" aria-modal="true" aria-labelledby="confirmModalTitle">
        <div class="confirm-dialog-content">
            <div class="confirm-dialog-icon-wrap" id="confirmModalIconWrap">
                <span class="confirm-dialog-icon" id="confirmModalIcon">⚠️</span>
            </div>
            <div class="confirm-dialog-text">
                <h3 class="confirm-dialog-title" id="confirmModalTitle">Xác nhận thao tác</h3>
                <p class="confirm-dialog-desc" id="confirmModalMessage">Bạn có chắc chắn muốn thực hiện hành động này không?</p>
            </div>
        </div>

        <div class="confirm-dialog-foot">
            <button type="button" class="btn btn-ghost" id="confirmModalCancelBtn" onclick="closeConfirmModal(false)">
                Huỷ bỏ
            </button>
            <button type="button" class="btn btn-danger" id="confirmModalOkBtn" onclick="closeConfirmModal(true)">
                Xác nhận
            </button>
        </div>
    </div>
</div>

<script>
(function() {
    var confirmResolver = null;

    window.showConfirmModal = function(opts) {
        opts = opts || {};
        var title = opts.title || 'Xác nhận thao tác';
        var message = opts.message || 'Bạn có chắc chắn muốn thực hiện hành động này không?';
        var confirmText = opts.confirmText || 'Xác nhận';
        var cancelText = opts.cancelText || 'Huỷ bỏ';
        var type = opts.type || 'danger'; // 'danger' | 'warning' | 'info'

        var modal = document.getElementById('appConfirmModalBackdrop');
        if (!modal) {
            // Fallback nếu modal chưa được nạp
            return Promise.resolve(window.confirm(message));
        }
        if (modal.parentElement !== document.body) {
            document.body.appendChild(modal);
        }

        document.getElementById('confirmModalTitle').textContent = title;
        document.getElementById('confirmModalMessage').textContent = message;

        var okBtn = document.getElementById('confirmModalOkBtn');
        var cancelBtn = document.getElementById('confirmModalCancelBtn');
        var iconWrap = document.getElementById('confirmModalIconWrap');
        var iconEl = document.getElementById('confirmModalIcon');

        okBtn.textContent = confirmText;
        cancelBtn.textContent = cancelText;

        // Cập nhật kiểu màu sắc và icon
        okBtn.className = 'btn ' + (type === 'danger' ? 'btn-danger' : (type === 'warning' ? 'btn-warning' : 'btn-primary'));
        iconWrap.className = 'confirm-dialog-icon-wrap type-' + type;

        if (type === 'danger') {
            iconEl.textContent = '🗑️';
        } else if (type === 'warning') {
            iconEl.textContent = '⚠️';
        } else {
            iconEl.textContent = 'ℹ️';
        }

        modal.classList.add('is-open');
        document.body.style.overflow = 'hidden';

        // Hủy yêu cầu trước nếu người dùng bấm nhiều thao tác liên tiếp.
        if (confirmResolver) confirmResolver(false);
        return new Promise(function(resolve) {
            confirmResolver = resolve;
        });
    };

    window.closeConfirmModal = function(result) {
        var modal = document.getElementById('appConfirmModalBackdrop');
        if (!modal) return;

        modal.classList.remove('is-open');
        document.body.style.overflow = '';

        if (typeof confirmResolver === 'function') {
            confirmResolver(result);
            confirmResolver = null;
        }
    };

    // Tự động chặn và hiện modal cho các form và button có thuộc tính data-confirm
    document.addEventListener('click', function(e) {
        var target = e.target.closest('[data-confirm]');
        if (!target) return;

        // Nếu là submit button trong form hoặc trỏ tới form qua thuộc tính form="..." (HTML5)
        var formId = target.getAttribute('form') || target.getAttribute('data-form');
        var form = formId ? document.getElementById(formId) : target.closest('form');
        var confirmMsg = target.getAttribute('data-confirm');

        if (target.tagName === 'BUTTON' || target.tagName === 'A' || target.tagName === 'INPUT') {
            // Kiểm tra tính hợp lệ của form (required, min, max, pattern) trước khi hiện modal
            if (form && (target.type === 'submit' || !target.type) && typeof form.checkValidity === 'function') {
                if (!form.checkValidity()) {
                    if (typeof form.reportValidity === 'function') {
                        form.reportValidity();
                    }
                    return;
                }
            }

            e.preventDefault();
            e.stopPropagation();

            var title = target.getAttribute('data-confirm-title') || 'Xác nhận thao tác';
            var lower = (confirmMsg || '').toLowerCase();
            var isDanger = lower.indexOf('xoá') !== -1 || lower.indexOf('xóa') !== -1 || lower.indexOf('gỡ') !== -1 || lower.indexOf('hủy') !== -1 || lower.indexOf('khoá') !== -1 || lower.indexOf('hạ') !== -1;
            var confirmText = target.getAttribute('data-confirm-ok');
            if (!confirmText) {
                if (lower.indexOf('xoá') !== -1 || lower.indexOf('xóa') !== -1) confirmText = 'Đồng ý xoá';
                else if (lower.indexOf('gỡ') !== -1) confirmText = 'Đồng ý gỡ';
                else if (lower.indexOf('hủy') !== -1) confirmText = 'Đồng ý hủy';
                else if (lower.indexOf('khoá') !== -1) confirmText = 'Đồng ý khoá';
                else if (lower.indexOf('hạ') !== -1) confirmText = 'Đồng ý hạ';
                else if (lower.indexOf('nâng') !== -1) confirmText = 'Đồng ý nâng';
                else confirmText = isDanger ? 'Đồng ý' : 'Xác nhận';
            }
            var type = isDanger ? 'danger' : (lower.indexOf('nâng') !== -1 || lower.indexOf('duyệt') !== -1 ? 'info' : 'warning');

            window.showConfirmModal({
                title: title,
                message: confirmMsg,
                confirmText: confirmText,
                type: type
            }).then(function(ok) {
                if (ok) {
                    if (form) {
                        form.dataset.confirmed = 'true';
                        if (formId) {
                            form.submit();
                        } else if (typeof form.requestSubmit === 'function' && target.type === 'submit') {
                            form.requestSubmit(target);
                        } else {
                            // Trình duyệt cũ: giữ name/value của nút submit.
                            if (target.name) {
                                var hidden = document.createElement('input');
                                hidden.type = 'hidden';
                                hidden.name = target.name;
                                hidden.value = target.value;
                                form.appendChild(hidden);
                            }
                            form.submit();
                        }
                    } else if (target.tagName === 'A' && target.href) {
                        window.location.href = target.href;
                    }
                }
            });
        }
    }, true);

    // Chặn submit form trực tiếp nếu form có data-confirm
    document.addEventListener('submit', function(e) {
        var form = e.target;
        if (!form) return;
        if (form.dataset.confirmed === 'true') {
            form.dataset.confirmed = 'false';
            return;
        }
        if (!form.hasAttribute('data-confirm')) return;

        // Kiểm tra tính hợp lệ của form trước khi hiện modal xác nhận
        if (typeof form.checkValidity === 'function' && !form.checkValidity()) {
            return;
        }

        e.preventDefault();
        e.stopPropagation();

        var confirmMsg = form.getAttribute('data-confirm');
        var title = form.getAttribute('data-confirm-title') || 'Xác nhận hành động';
        var lower = (confirmMsg || '').toLowerCase();
        var isDanger = lower.indexOf('xoá') !== -1 || lower.indexOf('xóa') !== -1 || lower.indexOf('gỡ') !== -1 || lower.indexOf('hủy') !== -1 || lower.indexOf('khoá') !== -1 || lower.indexOf('hạ') !== -1;
        var confirmText = form.getAttribute('data-confirm-ok');
        if (!confirmText) {
            if (lower.indexOf('xoá') !== -1 || lower.indexOf('xóa') !== -1) confirmText = 'Đồng ý xoá';
            else if (lower.indexOf('gỡ') !== -1) confirmText = 'Đồng ý gỡ';
            else if (lower.indexOf('hủy') !== -1) confirmText = 'Đồng ý hủy';
            else if (lower.indexOf('khoá') !== -1) confirmText = 'Đồng ý khoá';
            else if (lower.indexOf('hạ') !== -1) confirmText = 'Đồng ý hạ';
            else if (lower.indexOf('nâng') !== -1) confirmText = 'Đồng ý nâng';
            else confirmText = isDanger ? 'Đồng ý' : 'Xác nhận';
        }
        var type = isDanger ? 'danger' : (lower.indexOf('nâng') !== -1 || lower.indexOf('duyệt') !== -1 ? 'info' : 'warning');

        window.showConfirmModal({
            title: title,
            message: confirmMsg,
            confirmText: confirmText,
            type: type
        }).then(function(ok) {
            if (ok) {
                form.dataset.confirmed = 'true';
                if (typeof form.requestSubmit === 'function') {
                    form.requestSubmit(e.submitter || undefined);
                } else {
                    form.submit();
                }
            }
        });
    }, true);

    // Đóng modal khi bấm phím Escape
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' || e.keyCode === 27) {
            var modal = document.getElementById('appConfirmModalBackdrop');
            if (modal && modal.classList.contains('is-open')) {
                window.closeConfirmModal(false);
            }
        }
    });
})();
</script>

