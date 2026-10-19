<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--
================================================================================
  _quick-topup-modal.jsp — HỘP THOẠI NẠP XU NHANH TẠI CHỖ (IN-PAGE QUICK TOP-UP)
================================================================================
  Cho phép độc giả nạp xu mô phỏng tức thời mà không cần rời trang đang đọc.
  Tự động cập nhật số dư toàn bộ trang bằng AJAX.
================================================================================
--%>
<div class="app-modal-backdrop" id="quickTopupModalBackdrop" onclick="if(event.target===this) closeQuickTopupModal();">
    <div class="app-modal-card quick-topup-card" role="dialog" aria-modal="true" aria-labelledby="quickTopupTitle">
        <div class="app-modal-head">
            <div class="modal-title-wrap">
                <span class="modal-title-icon">🪙</span>
                <div>
                    <h3 id="quickTopupTitle">Nạp Xu Nhanh (Mô Phỏng)</h3>
                    <p class="modal-subtitle">Nạp xu ngay để tiếp tục đọc chương VIP và ủng hộ tác giả</p>
                </div>
            </div>
            <button type="button" class="app-modal-close" onclick="closeQuickTopupModal()" aria-label="Đóng">&times;</button>
        </div>

        <c:choose>
        <c:when test="${empty sessionScope.currentUser}">
            <div class="app-modal-body" style="padding: 28px 24px; text-align: center;">
                <div style="font-size: 2.8rem; margin-bottom: 12px;">🔑</div>
                <h4 style="margin: 0 0 8px; font-size: 1.15rem; font-weight: 700;">Vui lòng đăng nhập</h4>
                <p style="color: var(--text-dim, #cbd5e1); font-size: 0.88rem; line-height: 1.5; margin-bottom: 20px;">
                    Bạn cần đăng nhập tài khoản để nạp xu và mở khoá chương VIP. Đăng ký thành viên nhận ngay 100 xu miễn phí!
                </p>
                <a href="${pageContext.request.contextPath}/auth?action=login" class="btn btn-primary btn-lg" style="width: 100%;">
                    🔑 Đăng nhập ngay &rarr;
                </a>
            </div>
            <div class="app-modal-foot">
                <button type="button" class="btn btn-ghost" onclick="closeQuickTopupModal()">Đóng</button>
            </div>
        </c:when>
        <c:otherwise>
        <form id="quickTopupForm" onsubmit="submitQuickTopup(event)">
            <input type="hidden" name="_csrf" id="quickTopupCsrf" value="${csrfToken}">
            <input type="hidden" name="action" value="buy">
            <input type="hidden" name="packageId" id="quickTopupPackageId" value="pkg_50k">
            <input type="hidden" name="paymentMethod" id="quickTopupPaymentMethod" value="momo">

            <div class="app-modal-body">
                <%-- Cảnh báo thiếu xu nếu mở từ luồng mua VIP hoặc tip --%>
                <div id="quickTopupNeededAlert" class="topup-needed-banner" style="display:none;">
                    <span class="banner-icon">⚠️</span>
                    <span id="quickTopupNeededText">Bạn đang thiếu <b>5 xu</b> để hoàn tất giao dịch này.</span>
                </div>

                <%-- Số dư hiện tại --%>
                <div class="topup-balance-bar">
                    <span>Số dư ví hiện tại:</span>
                    <span class="topup-balance-value">
                        <b id="quickTopupCurrentBalance"><fmt:formatNumber pattern="#,##0" value="${walletBalance != null ? walletBalance : 100}"/></b> xu
                    </span>
                </div>

                <%-- Lưới chọn gói nạp xu --%>
                <label class="modal-field-label">Chọn gói nạp xu ưu đãi:</label>
                <div class="quick-topup-grid">
                    <div class="quick-pkg-card" data-pkg="pkg_20k" onclick="selectQuickPackage('pkg_20k', this)">
                        <div class="quick-pkg-top">
                            <span class="quick-pkg-coins">🪙 55 <small>xu</small></span>
                            <span class="quick-pkg-bonus">+5 xu thưởng</span>
                        </div>
                        <div class="quick-pkg-price">20.000 đ</div>
                    </div>

                    <div class="quick-pkg-card is-selected" data-pkg="pkg_50k" onclick="selectQuickPackage('pkg_50k', this)">
                        <span class="quick-pkg-badge">Phổ biến</span>
                        <div class="quick-pkg-top">
                            <span class="quick-pkg-coins">🪙 135 <small>xu</small></span>
                            <span class="quick-pkg-bonus">+15 xu thưởng</span>
                        </div>
                        <div class="quick-pkg-price">50.000 đ</div>
                    </div>

                    <div class="quick-pkg-card" data-pkg="pkg_100k" onclick="selectQuickPackage('pkg_100k', this)">
                        <span class="quick-pkg-badge badge-green">Tiết kiệm</span>
                        <div class="quick-pkg-top">
                            <span class="quick-pkg-coins">🪙 300 <small>xu</small></span>
                            <span class="quick-pkg-bonus">+40 xu thưởng</span>
                        </div>
                        <div class="quick-pkg-price">100.000 đ</div>
                    </div>

                    <div class="quick-pkg-card" data-pkg="pkg_200k" onclick="selectQuickPackage('pkg_200k', this)">
                        <span class="quick-pkg-badge badge-purple">VIP Deal</span>
                        <div class="quick-pkg-top">
                            <span class="quick-pkg-coins">🪙 650 <small>xu</small></span>
                            <span class="quick-pkg-bonus">+100 xu thưởng</span>
                        </div>
                        <div class="quick-pkg-price">200.000 đ</div>
                    </div>
                </div>

                <%-- Phương thức thanh toán --%>
                <label class="modal-field-label">Phương thức thanh toán mô phỏng:</label>
                <div class="quick-payment-methods">
                    <button type="button" class="quick-method-btn is-active" data-method="momo" onclick="selectPaymentMethod('momo', this)">
                        <span class="method-icon">🟣</span> Ví MoMo
                    </button>
                    <button type="button" class="quick-method-btn" data-method="bank_transfer" onclick="selectPaymentMethod('bank_transfer', this)">
                        <span class="method-icon">🏦</span> VietQR
                    </button>
                    <button type="button" class="quick-method-btn" data-method="vnpay" onclick="selectPaymentMethod('vnpay', this)">
                        <span class="method-icon">🔴</span> VNPAY QR
                    </button>
                </div>

                <%-- Thông báo trạng thái --%>
                <div id="quickTopupStatusAlert" class="modal-status-alert" style="display:none;"></div>
            </div>

            <div class="app-modal-foot">
                <button type="button" class="btn btn-ghost" onclick="closeQuickTopupModal()">Để sau</button>
                <button type="submit" class="btn btn-warning topup-submit-btn" id="quickTopupSubmitBtn">
                    ⚡ Nạp Ngay (Mô phỏng tức thì)
                </button>
            </div>
        </form>
        </c:otherwise>
        </c:choose>
    </div>
</div>

<script>
(function() {
    var pendingCallback = null;

    function formatNumber(num) {
        return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".");
    }

    window.openQuickTopupModal = function(neededCoins, onSuccessCallback) {
        var modal = document.getElementById('quickTopupModalBackdrop');
        if (!modal) return;
        if (modal.parentElement !== document.body) {
            document.body.appendChild(modal);
        }

        pendingCallback = onSuccessCallback || null;
        var alertEl = document.getElementById('quickTopupNeededAlert');
        var alertText = document.getElementById('quickTopupNeededText');

        if (neededCoins && neededCoins > 0) {
            alertText.innerHTML = '⚠️ Bạn đang thiếu <b>' + formatNumber(neededCoins) + ' xu</b> để hoàn tất giao dịch này.';
            alertEl.style.display = 'flex';

            // Tự động chọn gói nạp phù hợp
            if (neededCoins > 300) {
                selectQuickPackage('pkg_200k');
            } else if (neededCoins > 135) {
                selectQuickPackage('pkg_100k');
            } else if (neededCoins > 55) {
                selectQuickPackage('pkg_50k');
            } else {
                selectQuickPackage('pkg_20k');
            }
        } else {
            alertEl.style.display = 'none';
            selectQuickPackage('pkg_50k');
        }

        hideQuickTopupStatus();
        var submitBtn = document.getElementById('quickTopupSubmitBtn');
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.innerHTML = '⚡ Nạp Ngay (Mô phỏng tức thì)';
        }

        modal.classList.add('is-open');
        document.body.style.overflow = 'hidden';
    };

    window.closeQuickTopupModal = function() {
        var modal = document.getElementById('quickTopupModalBackdrop');
        if (!modal) return;
        modal.classList.remove('is-open');
        document.body.style.overflow = '';
    };

    window.selectQuickPackage = function(pkgId, el) {
        var input = document.getElementById('quickTopupPackageId');
        if (input) input.value = pkgId;

        var cards = document.querySelectorAll('.quick-pkg-card');
        cards.forEach(function(c) {
            if (c.getAttribute('data-pkg') === pkgId) {
                c.classList.add('is-selected');
            } else {
                c.classList.remove('is-selected');
            }
        });
    };

    window.selectPaymentMethod = function(method, el) {
        var input = document.getElementById('quickTopupPaymentMethod');
        if (input) input.value = method;

        var btns = document.querySelectorAll('.quick-method-btn');
        btns.forEach(function(b) {
            if (b.getAttribute('data-method') === method) {
                b.classList.add('is-active');
            } else {
                b.classList.remove('is-active');
            }
        });
    };

    function showQuickTopupStatus(msg, type) {
        var el = document.getElementById('quickTopupStatusAlert');
        if (!el) return;
        el.className = 'modal-status-alert status-' + type;
        el.innerHTML = msg;
        el.style.display = 'block';
    }

    function hideQuickTopupStatus() {
        var el = document.getElementById('quickTopupStatusAlert');
        if (el) el.style.display = 'none';
    }

    window.submitQuickTopup = function(e) {
        e.preventDefault();
        var csrf = document.getElementById('quickTopupCsrf').value;
        var pkg = document.getElementById('quickTopupPackageId').value;
        var method = document.getElementById('quickTopupPaymentMethod').value;
        var submitBtn = document.getElementById('quickTopupSubmitBtn');

        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.innerHTML = '⏳ Đang nạp xu...';
        }

        var params = new URLSearchParams();
        params.append('_csrf', csrf);
        params.append('action', 'buy');
        params.append('packageId', pkg);
        params.append('paymentMethod', method);

        var ctx = document.body.getAttribute('data-ctx') || '${pageContext.request.contextPath}';

        fetch(ctx + '/wallet', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
                'X-Requested-With': 'XMLHttpRequest',
                'Accept': 'application/json'
            },
            body: params.toString()
        })
        .then(function(res) { return res.json(); })
        .then(function(data) {
            if (data.success) {
                var newBal = data.newBalance;

                // Cập nhật số dư ở mọi vị trí trên trang
                var curEl = document.getElementById('quickTopupCurrentBalance');
                if (curEl) curEl.textContent = formatNumber(newBal);

                var tipBal = document.getElementById('tipModalBalance');
                if (tipBal) tipBal.textContent = formatNumber(newBal);

                var vipBals = document.querySelectorAll('.vip-balance-tag, .coin-badge-val');
                vipBals.forEach(function(el) { el.textContent = formatNumber(newBal); });

                var dropTags = document.querySelectorAll('.dropdown-coin-tag');
                dropTags.forEach(function(el) { el.textContent = formatNumber(newBal) + ' xu'; });

                showQuickTopupStatus('🎉 ' + data.message, 'success');

                if (window.showToast) {
                    window.showToast(data.message);
                }
                if (window.launchCelebration) {
                    window.launchCelebration('coins');
                }

                if (submitBtn) {
                    submitBtn.innerHTML = '✓ Nạp Xu Thành Công!';
                }

                setTimeout(function() {
                    closeQuickTopupModal();
                    if (typeof pendingCallback === 'function') {
                        pendingCallback(newBal);
                    }
                }, 1200);
            } else {
                showQuickTopupStatus('❌ ' + (data.message || 'Không thể thực hiện nạp xu.'), 'error');
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = '⚡ Nạp Ngay (Mô phỏng tức thì)';
                }
            }
        })
        .catch(function(err) {
            showQuickTopupStatus('❌ Lỗi kết nối máy chủ, vui lòng thử lại sau.', 'error');
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.innerHTML = '⚡ Nạp Ngay (Mô phỏng tức thì)';
            }
        });
    };

    // Đóng modal khi bấm phím Escape
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' || e.keyCode === 27) {
            var modal = document.getElementById('quickTopupModalBackdrop');
            if (modal && modal.classList.contains('is-open')) {
                closeQuickTopupModal();
            }
        }
    });
})();
</script>

