<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--
================================================================================
  _tip-modal.jsp — HỘP THOẠI TẶNG XU CHO TÁC GIẢ (ISSUE-008 & ISSUE-020)
================================================================================
  Cho phép độc giả ủng hộ (tip) xu ảo cho tác giả kèm lời nhắn khích lệ.
  Tích hợp giao dịch phân tán tức thời qua WalletServlet (action=tip).
================================================================================
--%>
<div class="tip-modal-backdrop" id="tipModalBackdrop" style="display:none;" onclick="if(event.target===this) closeTipModal();">
    <div class="tip-modal-card" role="dialog" aria-modal="true" aria-labelledby="tipModalTitle">
        <div class="tip-modal-head">
            <h3 id="tipModalTitle">🪙 Tặng Xu Tác Giả</h3>
            <button type="button" class="tip-modal-close" onclick="closeTipModal()" aria-label="Đóng">&times;</button>
        </div>

        <form id="tipForm" onsubmit="submitTip(event)">
            <input type="hidden" name="_csrf" id="tipCsrf" value="${csrfToken}">
            <input type="hidden" name="action" value="tip">
            <input type="hidden" name="toUserId" id="tipToUserId" value="">
            <input type="hidden" name="storyId" id="tipStoryId" value="">

            <div class="tip-modal-body">
                <%-- Tác giả nhận --%>
                <div class="tip-recipient-box">
                    <div class="tip-recipient-icon">✍️</div>
                    <div class="tip-recipient-info">
                        <div class="tip-recipient-label">Gửi tặng đến:</div>
                        <div class="tip-recipient-name" id="tipAuthorName">Tác giả</div>
                        <div class="tip-recipient-story" id="tipStoryTitleWrap" style="display:none;">
                            Truyện: <em id="tipStoryTitle"></em>
                        </div>
                    </div>
                </div>

                <%-- Số dư hiện tại --%>
                <div class="tip-balance-row">
                    <span>Số dư ví của bạn:</span>
                    <span class="tip-current-balance">
                        <b id="tipModalBalance"><fmt:formatNumber pattern="#,##0" value="${walletBalance != null ? walletBalance : 0}"/></b> xu
                        <a href="${pageContext.request.contextPath}/wallet?action=topup" class="tip-topup-btn" target="_blank" title="Nạp thêm xu vào ví">+ Nạp xu</a>
                    </span>
                </div>

                <%-- Chọn nhanh số xu --%>
                <div class="tip-presets-wrap">
                    <label class="tip-field-label">Chọn mức tặng xu:</label>
                    <div class="tip-presets-grid">
                        <button type="button" class="tip-preset-btn" data-amt="10" onclick="setTipAmount(10)">10 xu</button>
                        <button type="button" class="tip-preset-btn" data-amt="20" onclick="setTipAmount(20)">20 xu</button>
                        <button type="button" class="tip-preset-btn is-active" data-amt="50" onclick="setTipAmount(50)">50 xu</button>
                        <button type="button" class="tip-preset-btn" data-amt="100" onclick="setTipAmount(100)">100 xu</button>
                        <button type="button" class="tip-preset-btn" data-amt="200" onclick="setTipAmount(200)">200 xu</button>
                        <button type="button" class="tip-preset-btn" data-amt="500" onclick="setTipAmount(500)">500 xu</button>
                    </div>
                </div>

                <%-- Nhập số xu tuỳ ý --%>
                <div class="tip-input-row">
                    <label for="tipAmountInput" class="tip-field-label">Số xu muốn tặng:</label>
                    <div class="tip-input-wrap">
                        <input type="number" id="tipAmountInput" name="amount" min="1" max="1000" value="50" required oninput="onTipAmountChange(this.value)">
                        <span class="tip-input-unit">xu</span>
                    </div>
                </div>

                <%-- Lời nhắn --%>
                <div class="tip-msg-row">
                    <label for="tipMessageInput" class="tip-field-label">Lời nhắn khích lệ (tuỳ chọn):</label>
                    <textarea id="tipMessageInput" name="message" rows="2" maxlength="255" placeholder="Truyện hay quá tác giả ơi, tặng bạn ly cà phê tiếp thêm động lực! ❤️"></textarea>
                </div>

                <%-- Thông báo trạng thái --%>
                <div id="tipStatusAlert" class="tip-status-alert" style="display:none;"></div>
            </div>

            <div class="tip-modal-foot">
                <button type="button" class="btn btn-ghost" onclick="closeTipModal()">Huỷ</button>
                <button type="submit" class="btn btn-warning tip-submit-btn" id="tipSubmitBtn">
                    🎁 Gửi Tặng <span id="tipBtnAmount">50</span> Xu
                </button>
            </div>
        </form>
    </div>
</div>

<script>
(function() {
    var userBalance = ${walletBalance != null ? walletBalance : 0};

    function formatNumber(num) {
        return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".");
    }

    window.triggerTipModal = function(btn) {
        if (!btn) return;
        var aId = btn.getAttribute('data-author-id');
        var aName = btn.getAttribute('data-author-name');
        var sId = btn.getAttribute('data-story-id');
        var sTitle = btn.getAttribute('data-story-title');
        window.openTipModal(aId, aName, sId, sTitle);
    };

    window.openTipModal = function(authorId, authorName, storyId, storyTitle) {
        var modal = document.getElementById('tipModalBackdrop');
        if (!modal) return;

        document.getElementById('tipToUserId').value = authorId || '';
        document.getElementById('tipAuthorName').textContent = authorName || 'Tác giả';

        var storyWrap = document.getElementById('tipStoryTitleWrap');
        var storyEl = document.getElementById('tipStoryTitle');
        var storyInput = document.getElementById('tipStoryId');

        if (storyId && storyTitle) {
            storyInput.value = storyId;
            storyEl.textContent = storyTitle;
            storyWrap.style.display = 'block';
        } else {
            storyInput.value = '';
            storyWrap.style.display = 'none';
        }

        // Đặt mặc định 50 xu
        setTipAmount(50);
        document.getElementById('tipMessageInput').value = '';
        hideTipStatus();

        var submitBtn = document.getElementById('tipSubmitBtn');
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.innerHTML = '🎁 Gửi Tặng <span id="tipBtnAmount">50</span> Xu';
        }

        modal.style.display = 'flex';
        document.body.style.overflow = 'hidden';
    };

    window.closeTipModal = function() {
        var modal = document.getElementById('tipModalBackdrop');
        if (modal) modal.style.display = 'none';
        document.body.style.overflow = '';
    };

    window.setTipAmount = function(amt) {
        var input = document.getElementById('tipAmountInput');
        if (input) input.value = amt;

        var btnAmt = document.getElementById('tipBtnAmount');
        if (btnAmt) btnAmt.textContent = formatNumber(amt);

        var presets = document.querySelectorAll('.tip-preset-btn');
        presets.forEach(function(btn) {
            if (parseInt(btn.getAttribute('data-amt'), 10) === amt) {
                btn.classList.add('is-active');
            } else {
                btn.classList.remove('is-active');
            }
        });
        checkBalanceSufficiency(amt);
    };

    window.onTipAmountChange = function(val) {
        var amt = parseInt(val, 10) || 0;
        var btnAmt = document.getElementById('tipBtnAmount');
        if (btnAmt) btnAmt.textContent = formatNumber(amt);

        var presets = document.querySelectorAll('.tip-preset-btn');
        presets.forEach(function(btn) {
            if (parseInt(btn.getAttribute('data-amt'), 10) === amt) {
                btn.classList.add('is-active');
            } else {
                btn.classList.remove('is-active');
            }
        });
        checkBalanceSufficiency(amt);
    };

    function checkBalanceSufficiency(amt) {
        var alertEl = document.getElementById('tipStatusAlert');
        var submitBtn = document.getElementById('tipSubmitBtn');
        if (amt > userBalance) {
            showTipStatus('⚠️ Số dư ví của bạn không đủ (' + formatNumber(userBalance) + ' xu). Vui lòng nạp thêm xu nhé!', 'warn');
            if (submitBtn) submitBtn.disabled = true;
        } else {
            hideTipStatus();
            if (submitBtn) submitBtn.disabled = false;
        }
    }

    function showTipStatus(msg, type) {
        var el = document.getElementById('tipStatusAlert');
        if (!el) return;
        el.className = 'tip-status-alert tip-status-' + type;
        el.innerHTML = msg;
        el.style.display = 'block';
    }

    function hideTipStatus() {
        var el = document.getElementById('tipStatusAlert');
        if (el) el.style.display = 'none';
    }

    window.submitTip = function(event) {
        event.preventDefault();
        var toUserId = document.getElementById('tipToUserId').value;
        var storyId = document.getElementById('tipStoryId').value;
        var amount = parseInt(document.getElementById('tipAmountInput').value, 10);
        var message = document.getElementById('tipMessageInput').value;
        var csrf = document.getElementById('tipCsrf').value;
        var submitBtn = document.getElementById('tipSubmitBtn');

        if (!toUserId || !amount || amount <= 0) {
            showTipStatus('Vui lòng chọn số xu hợp lệ.', 'error');
            return;
        }

        if (amount > userBalance) {
            showTipStatus('Số dư không đủ để gửi tặng số xu này.', 'warn');
            return;
        }

        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.textContent = 'Đang chuyển xu...';
        }

        var params = new URLSearchParams();
        params.append('_csrf', csrf);
        params.append('action', 'tip');
        params.append('toUserId', toUserId);
        if (storyId) params.append('storyId', storyId);
        params.append('amount', amount);
        params.append('message', message);

        fetch('${pageContext.request.contextPath}/wallet', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: params.toString()
        })
        .then(function(res) { return res.json(); })
        .then(function(data) {
            if (data.success) {
                userBalance = data.newBalance;
                var balEl = document.getElementById('tipModalBalance');
                if (balEl) balEl.textContent = formatNumber(userBalance);

                // Cập nhật huy hiệu xu trên header nếu có
                var headerCoinVal = document.querySelector('.coin-badge-val');
                if (headerCoinVal) headerCoinVal.textContent = formatNumber(userBalance);
                var dropdownCoinTag = document.querySelector('.dropdown-coin-tag');
                if (dropdownCoinTag) dropdownCoinTag.textContent = formatNumber(userBalance) + ' xu';

                showTipStatus('🎉 ' + data.message, 'success');
                if (window.launchCelebration) {
                    window.launchCelebration('coins');
                }
                if (submitBtn) {
                    submitBtn.textContent = '✓ Đã Gửi Thành Công!';
                }
                setTimeout(function() {
                    closeTipModal();
                }, 1800);
            } else {
                showTipStatus('❌ ' + (data.message || 'Giao dịch không thành công.'), 'error');
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = '🎁 Gửi Tặng <span id="tipBtnAmount">' + amount + '</span> Xu';
                }
            }
        })
        .catch(function(err) {
            showTipStatus('❌ Lỗi kết nối mạng, vui lòng thử lại sau.', 'error');
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.innerHTML = '🎁 Gửi Tặng <span id="tipBtnAmount">' + amount + '</span> Xu';
            }
        });
    };
})();
</script>
