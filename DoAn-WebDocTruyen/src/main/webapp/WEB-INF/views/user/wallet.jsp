<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--
================================================================================
  user/wallet.jsp — Giao diện Ví Xu & Nạp Xu
================================================================================
  Nhận từ WalletServlet:
    - walletBalance: số dư xu hiện tại
    - history: danh sách Transaction gần đây
    - currentUser: người dùng đang đăng nhập
================================================================================
--%>
<div class="wallet-page">

    <%-- HERO / SUMMARY CARD --%>
    <div class="wallet-hero-card">
        <div class="wallet-hero-content">
            <div class="wallet-hero-left">
                <div class="wallet-badge-pill">🪙 VÍ XU THÀNH VIÊN</div>
                <h1 class="wallet-hero-title">Ví Xu &amp; Nạp Xu</h1>
                <p class="wallet-hero-desc">
                    Sử dụng xu ảo để mở khoá các chương truyện VIP đặc sắc và tặng quà (tip) khích lệ tác giả sáng tác truyện.
                </p>
                <div class="wallet-hero-tags">
                    <span class="hero-tag">✨ Mở khoá vĩnh viễn</span>
                    <span class="hero-tag">💖 Ủng hộ tác giả 100%</span>
                    <span class="hero-tag">🎁 Thưởng chuỗi mỗi ngày</span>
                </div>
            </div>
            <div class="wallet-hero-right">
                <div class="wallet-balance-box">
                    <span class="balance-label">SỐ DƯ KHẢ DỤNG</span>
                    <div class="balance-val">
                        <span class="coin-spin">🪙</span>
                        <span id="current-user-balance"><fmt:formatNumber pattern="#,##0" value="${walletBalance != null ? walletBalance : 0}"/></span>
                        <span class="balance-unit">xu</span>
                    </div>
                    <div class="balance-actions">
                        <a href="#packages-section" class="btn btn-sm btn-primary">Nạp thêm ngay</a>
                        <a href="${pageContext.request.contextPath}/user?action=me#gamification" class="btn btn-sm btn-ghost" title="Điểm danh nhận xu miễn phí">
                            🎁 Nhận xu miễn phí
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <%-- MAIN GRID: PACKAGES & PAYMENT CHECKOUT --%>
    <div class="wallet-grid">
        
        <%-- LEFT COLUMN: PACKAGES & PAYMENT METHODS --%>
        <div class="wallet-main-col">
            
            <%-- SECTION 1: CHỌN GÓI NẠP --%>
            <div class="wallet-section" id="packages-section">
                <div class="section-head">
                    <h2 class="section-title">
                        <span class="sec-icon">📦</span> 1. Chọn gói nạp xu ưu đãi
                    </h2>
                    <span class="sec-subtitle">Nạp càng nhiều — Khuyến mãi càng lớn</span>
                </div>

                <div class="packages-grid">
                    <%-- Gói 1 --%>
                    <div class="package-card" data-pkg="pkg_20k" data-coins="55" data-base="50" data-bonus="5" data-price="20000" data-name="Gói Khởi Động">
                        <div class="pkg-top">
                            <span class="pkg-name">Khởi Động</span>
                        </div>
                        <div class="pkg-coin-amount">
                            <span class="coin-num">50</span>
                            <span class="coin-text">xu</span>
                        </div>
                        <div class="pkg-bonus">+5 xu thưởng</div>
                        <div class="pkg-price">20.000 ₫</div>
                        <div class="pkg-select-indicator">Đã chọn</div>
                    </div>

                    <%-- Gói 2 - Phổ biến nhất --%>
                    <div class="package-card is-popular is-selected" data-pkg="pkg_50k" data-coins="135" data-base="120" data-bonus="15" data-price="50000" data-name="Gói Phổ Thông">
                        <div class="pkg-badge-popular">⭐ Phổ biến nhất</div>
                        <div class="pkg-top">
                            <span class="pkg-name">Phổ Thông</span>
                        </div>
                        <div class="pkg-coin-amount">
                            <span class="coin-num">120</span>
                            <span class="coin-text">xu</span>
                        </div>
                        <div class="pkg-bonus hot">+15 xu thưởng (+12%)</div>
                        <div class="pkg-price">50.000 ₫</div>
                        <div class="pkg-select-indicator">Đã chọn</div>
                    </div>

                    <%-- Gói 3 --%>
                    <div class="package-card" data-pkg="pkg_100k" data-coins="300" data-base="260" data-bonus="40" data-price="100000" data-name="Gói Tiết Kiệm">
                        <div class="pkg-top">
                            <span class="pkg-name">Tiết Kiệm</span>
                        </div>
                        <div class="pkg-coin-amount">
                            <span class="coin-num">260</span>
                            <span class="coin-text">xu</span>
                        </div>
                        <div class="pkg-bonus hot">+40 xu thưởng (+15%)</div>
                        <div class="pkg-price">100.000 ₫</div>
                        <div class="pkg-select-indicator">Đã chọn</div>
                    </div>

                    <%-- Gói 4 --%>
                    <div class="package-card" data-pkg="pkg_200k" data-coins="650" data-base="550" data-bonus="100" data-price="200000" data-name="Gói Bạc">
                        <div class="pkg-top">
                            <span class="pkg-name">Gói Bạc</span>
                        </div>
                        <div class="pkg-coin-amount">
                            <span class="coin-num">550</span>
                            <span class="coin-text">xu</span>
                        </div>
                        <div class="pkg-bonus hot">+100 xu thưởng (+18%)</div>
                        <div class="pkg-price">200.000 ₫</div>
                        <div class="pkg-select-indicator">Đã chọn</div>
                    </div>

                    <%-- Gói 5 - VIP --%>
                    <div class="package-card is-vip" data-pkg="pkg_500k" data-coins="1850" data-base="1500" data-bonus="350" data-price="500000" data-name="Gói Kim Cương VIP">
                        <div class="pkg-badge-vip">🔥 Siêu ưu đãi +23%</div>
                        <div class="pkg-top">
                            <span class="pkg-name">Kim Cương VIP</span>
                        </div>
                        <div class="pkg-coin-amount">
                            <span class="coin-num">1.500</span>
                            <span class="coin-text">xu</span>
                        </div>
                        <div class="pkg-bonus jackpot">+350 xu thưởng khủng</div>
                        <div class="pkg-price">500.000 ₫</div>
                        <div class="pkg-select-indicator">Đã chọn</div>
                    </div>
                </div>
            </div>

            <%-- SECTION 2: CHỌN PHƯƠNG THỨC THANH TOÁN --%>
            <div class="wallet-section">
                <div class="section-head">
                    <h2 class="section-title">
                        <span class="sec-icon">💳</span> 2. Chọn phương thức thanh toán
                    </h2>
                    <span class="sec-subtitle">Hỗ trợ các cổng thanh toán điện tử phổ biến tại Việt Nam</span>
                </div>

                <div class="payment-methods-grid">
                    <%-- VNPAY --%>
                    <div class="payment-method-card is-selected" data-method="vnpay">
                        <div class="pm-radio-dot"></div>
                        <div class="pm-icon-wrap vnpay-icon">
                            <span class="pm-logo-text">VNPAY</span>
                        </div>
                        <div class="pm-info">
                            <div class="pm-title">Cổng VNPAY QR</div>
                            <div class="pm-desc">Quét mã QR bằng App ngân hàng (VCB, BIDV, MB, Techcombank...)</div>
                        </div>
                        <span class="pm-recommend-tag">Khuyên dùng</span>
                    </div>

                    <%-- MoMo --%>
                    <div class="payment-method-card" data-method="momo">
                        <div class="pm-radio-dot"></div>
                        <div class="pm-icon-wrap momo-icon">
                            <span class="pm-logo-text">MoMo</span>
                        </div>
                        <div class="pm-info">
                            <div class="pm-title">Ví Điện Tử MoMo</div>
                            <div class="pm-desc">Thanh toán tức thì qua ứng dụng ví MoMo trên điện thoại</div>
                        </div>
                    </div>

                    <%-- VietQR / Chuyển khoản --%>
                    <div class="payment-method-card" data-method="bank_transfer">
                        <div class="pm-radio-dot"></div>
                        <div class="pm-icon-wrap vietqr-icon">
                            <span class="pm-logo-text">VietQR</span>
                        </div>
                        <div class="pm-info">
                            <div class="pm-title">Chuyển khoản Ngân hàng (VietQR 24/7)</div>
                            <div class="pm-desc">Tự động nhận diện giao dịch qua mã QR chuyển khoản nhanh Napas 247</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <%-- RIGHT COLUMN: CHECKOUT & QR PREVIEW --%>
        <div class="wallet-side-col">
            <div class="checkout-box" id="checkout-panel">
                <div class="checkout-head">
                    <h3 class="checkout-title">🧾 Chi Tiết Đơn Nạp</h3>
                    <span class="checkout-order-code" id="order-code-display">ORD-9428</span>
                </div>

                <div class="checkout-body">
                    <div class="checkout-row">
                        <span class="cr-label">Tài khoản nhận:</span>
                        <span class="cr-val"><b><c:out value="${currentUser.name}"/></b> (@<c:out value="${currentUser.username}"/>)</span>
                    </div>
                    <div class="checkout-row">
                        <span class="cr-label">Gói nạp:</span>
                        <span class="cr-val highlight" id="checkout-pkg-name">Gói Phổ Thông</span>
                    </div>
                    <div class="checkout-row">
                        <span class="cr-label">Xu gốc:</span>
                        <span class="cr-val" id="checkout-base-coins">120 xu</span>
                    </div>
                    <div class="checkout-row">
                        <span class="cr-label">Khuyến mãi thưởng:</span>
                        <span class="cr-val bonus" id="checkout-bonus-coins">+15 xu</span>
                    </div>
                    <div class="checkout-sep"></div>
                    <div class="checkout-row total-coins">
                        <span class="cr-label">Tổng xu thực nhận:</span>
                        <span class="cr-val coin-total" id="checkout-total-coins">135 xu</span>
                    </div>
                    <div class="checkout-row total-price">
                        <span class="cr-label">Số tiền thanh toán:</span>
                        <span class="cr-val price-total" id="checkout-total-price">50.000 ₫</span>
                    </div>

                    <%-- QR Code Mockup Box --%>
                    <div class="qr-mockup-wrap">
                        <div class="qr-frame">
                            <svg class="qr-svg" viewBox="0 0 160 160" width="140" height="140">
                                <rect width="160" height="160" fill="#ffffff" rx="8"/>
                                <%-- Corner 1 --%>
                                <rect x="15" y="15" width="35" height="35" fill="#1e293b" rx="4"/>
                                <rect x="22" y="22" width="21" height="21" fill="#ffffff" rx="2"/>
                                <rect x="27" y="27" width="11" height="11" fill="#1e293b" rx="1"/>
                                <%-- Corner 2 --%>
                                <rect x="110" y="15" width="35" height="35" fill="#1e293b" rx="4"/>
                                <rect x="117" y="22" width="21" height="21" fill="#ffffff" rx="2"/>
                                <rect x="122" y="27" width="11" height="11" fill="#1e293b" rx="1"/>
                                <%-- Corner 3 --%>
                                <rect x="15" y="110" width="35" height="35" fill="#1e293b" rx="4"/>
                                <rect x="22" y="117" width="21" height="21" fill="#ffffff" rx="2"/>
                                <rect x="27" y="122" width="11" height="11" fill="#1e293b" rx="1"/>
                                <%-- QR Matrix Dots Pattern --%>
                                <rect x="60" y="20" width="10" height="10" fill="#1e293b"/>
                                <rect x="75" y="20" width="10" height="10" fill="#1e293b"/>
                                <rect x="90" y="20" width="10" height="10" fill="#1e293b"/>
                                <rect x="60" y="35" width="10" height="10" fill="#1e293b"/>
                                <rect x="75" y="45" width="10" height="10" fill="#1e293b"/>
                                <rect x="90" y="35" width="10" height="10" fill="#1e293b"/>
                                <rect x="20" y="60" width="10" height="10" fill="#1e293b"/>
                                <rect x="35" y="60" width="10" height="10" fill="#1e293b"/>
                                <rect x="50" y="60" width="10" height="10" fill="#1e293b"/>
                                <rect x="65" y="60" width="10" height="10" fill="#1e293b"/>
                                <rect x="80" y="60" width="10" height="10" fill="#1e293b"/>
                                <rect x="95" y="60" width="10" height="10" fill="#1e293b"/>
                                <rect x="110" y="60" width="10" height="10" fill="#1e293b"/>
                                <rect x="130" y="60" width="10" height="10" fill="#1e293b"/>
                                <rect x="20" y="75" width="10" height="10" fill="#1e293b"/>
                                <rect x="40" y="80" width="10" height="10" fill="#1e293b"/>
                                <rect x="60" y="75" width="10" height="10" fill="#1e293b"/>
                                <rect x="75" y="75" width="10" height="10" fill="#1e293b"/>
                                <rect x="90" y="75" width="10" height="10" fill="#1e293b"/>
                                <rect x="110" y="75" width="10" height="10" fill="#1e293b"/>
                                <rect x="130" y="80" width="10" height="10" fill="#1e293b"/>
                                <rect x="20" y="95" width="10" height="10" fill="#1e293b"/>
                                <rect x="35" y="95" width="10" height="10" fill="#1e293b"/>
                                <rect x="60" y="95" width="10" height="10" fill="#1e293b"/>
                                <rect x="80" y="95" width="10" height="10" fill="#1e293b"/>
                                <rect x="100" y="95" width="10" height="10" fill="#1e293b"/>
                                <rect x="120" y="95" width="10" height="10" fill="#1e293b"/>
                                <rect x="60" y="115" width="10" height="10" fill="#1e293b"/>
                                <rect x="75" y="125" width="10" height="10" fill="#1e293b"/>
                                <rect x="90" y="115" width="10" height="10" fill="#1e293b"/>
                                <rect x="105" y="125" width="10" height="10" fill="#1e293b"/>
                                <rect x="125" y="115" width="10" height="10" fill="#1e293b"/>
                                <rect x="60" y="135" width="10" height="10" fill="#1e293b"/>
                                <rect x="75" y="135" width="10" height="10" fill="#1e293b"/>
                                <rect x="90" y="135" width="10" height="10" fill="#1e293b"/>
                                <rect x="110" y="135" width="10" height="10" fill="#1e293b"/>
                                <rect x="130" y="135" width="10" height="10" fill="#1e293b"/>
                                <%-- Center logo icon --%>
                                <circle cx="80" cy="80" r="14" fill="#f59e0b"/>
                                <text x="80" y="85" font-size="12" font-weight="bold" fill="#ffffff" text-anchor="middle">🪙</text>
                            </svg>
                        </div>
                        <div class="qr-info">
                            <span class="qr-bank-name" id="qr-method-name">VNPAY QR</span>
                            <span class="qr-hint">Quét mã bằng ứng dụng hỗ trợ để thanh toán tức thì</span>
                            <span class="qr-memo" id="qr-memo-text">ND: NAPXU PKG50K ORD-9428</span>
                        </div>
                    </div>

                    <%-- Form Xác Nhận Thanh Toán (Simulated Instant Top-up) --%>
                    <form action="${pageContext.request.contextPath}/wallet" method="post" id="wallet-buy-form">
                        <input type="hidden" name="_csrf" value="${csrfToken}">
                        <input type="hidden" name="action" value="buy">
                        <input type="hidden" name="packageId" id="input-package-id" value="pkg_50k">
                        <input type="hidden" name="paymentMethod" id="input-payment-method" value="vnpay">

                        <button type="submit" class="btn-wallet-confirm" id="btn-submit-topup">
                            <span class="btn-text">🚀 Xác Nhận Nạp Xu (Mô Phỏng)</span>
                        </button>
                    </form>

                    <div class="checkout-disclaimer">
                        <span class="disclaimer-icon">💡</span>
                        <div class="disclaimer-text">
                            <b>Môi trường thử nghiệm đồ án:</b> Giao dịch thanh toán được mô phỏng an toàn, hệ thống tự động cộng xu ảo ngay lập tức vào ví mà không phát sinh chi phí tiền thật. Giới hạn 3 lần / 2.000 xu mỗi ngày.
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <%-- SECTION 3: LỊCH SỬ GIAO DỊCH XU --%>
    <div class="wallet-section history-section">
        <div class="section-head">
            <h2 class="section-title">
                <span class="sec-icon">📜</span> Lịch sử giao dịch ví xu
            </h2>
            <span class="sec-subtitle">Danh sách các biến động nạp xu, mở khoá chương và tặng thưởng gần nhất</span>
        </div>

        <c:choose>
            <c:when test="${not empty history}">
                <div class="tx-table-wrap">
                    <table class="tx-table">
                        <thead>
                            <tr>
                                <th>Thời gian</th>
                                <th>Loại giao dịch</th>
                                <th>Nội dung / Ghi chú</th>
                                <th>Đối tác</th>
                                <th style="text-align:right;">Biến động xu</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="tx" items="${history}">
                                <c:set var="isReceive" value="${tx.toUserId eq currentUser.id and empty tx.fromUserId}" />
                                <c:set var="isTipSent" value="${tx.fromUserId eq currentUser.id and tx.toUserId ne currentUser.id}" />
                                <c:set var="isTipReceived" value="${tx.toUserId eq currentUser.id and not empty tx.fromUserId and tx.fromUserId ne currentUser.id}" />
                                <c:set var="isUnlock" value="${tx.fromUserId eq currentUser.id and (empty tx.toUserId or tx.toUserId eq currentUser.id or tx.message.contains('Mở khoá'))}" />

                                <tr>
                                    <td class="tx-time">
                                        <c:if test="${not empty tx.createdAt}">
                                            <span class="tx-date">${tx.createdAt.dayOfMonth}/${tx.createdAt.monthValue}/${tx.createdAt.year}</span>
                                            <span class="tx-hour">${tx.createdAt.hour lt 10 ? '0' : ''}${tx.createdAt.hour}:${tx.createdAt.minute lt 10 ? '0' : ''}${tx.createdAt.minute}</span>
                                        </c:if>
                                    </td>
                                    <td class="tx-type">
                                        <c:choose>
                                            <c:when test="${isReceive}">
                                                <span class="tx-pill tx-pill-deposit">💳 Nạp xu</span>
                                            </c:when>
                                            <c:when test="${isTipSent}">
                                                <span class="tx-pill tx-pill-tip-out">🎁 Tặng tác giả</span>
                                            </c:when>
                                            <c:when test="${isTipReceived}">
                                                <span class="tx-pill tx-pill-tip-in">💖 Được ủng hộ</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="tx-pill tx-pill-unlock">🔓 Mở khoá VIP</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="tx-msg">
                                        <span class="tx-msg-text"><c:out value="${tx.message}"/></span>
                                        <c:if test="${not empty tx.storyTitle}">
                                            <div class="tx-story-link">Truyện: <b><c:out value="${tx.storyTitle}"/></b></div>
                                        </c:if>
                                    </td>
                                    <td class="tx-partner">
                                        <c:choose>
                                            <c:when test="${isReceive}">
                                                <span class="partner-system">Hệ thống</span>
                                            </c:when>
                                            <c:when test="${isTipSent}">
                                                <span class="partner-user">Tác giả: <b><c:out value="${tx.toUsername}"/></b></span>
                                            </c:when>
                                            <c:when test="${isTipReceived}">
                                                <span class="partner-user">Từ: <b><c:out value="${tx.fromUsername}"/></b></span>
                                            </c:when>
                                            <c:otherwise>
                                                <span>Tác giả / Hệ thống</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="tx-amount" style="text-align:right;">
                                        <c:choose>
                                            <c:when test="${tx.toUserId eq currentUser.id}">
                                                <span class="amount-plus">+<fmt:formatNumber pattern="#,##0" value="${tx.amount}"/> xu</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="amount-minus">-<fmt:formatNumber pattern="#,##0" value="${tx.amount}"/> xu</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:when>
            <c:otherwise>
                <div class="empty-tx-card">
                    <span class="empty-icon">📭</span>
                    <h3>Chưa có giao dịch nào</h3>
                    <p>Hãy chọn một gói nạp phía trên hoặc tham gia điểm danh hàng ngày để nhận xu bạn nhé!</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <%-- SECTION 4: HỎI ĐÁP / QUY ĐỊNH XU --%>
    <div class="wallet-faq-card">
        <h3 class="faq-head">❓ Câu hỏi thường gặp về Ví Xu</h3>
        <div class="faq-grid">
            <div class="faq-item">
                <b>Xu ảo trong web dùng để làm gì?</b>
                <p>Dùng để mở khoá các chương truyện gắn nhãn VIP của các tác giả độc quyền và ủng hộ (tặng quà / tip) khích lệ tác giả yêu thích.</p>
            </div>
            <div class="faq-item">
                <b>Sau khi mở khoá chương có đọc lại được không?</b>
                <p>Hoàn toàn vĩnh viễn! Bạn chỉ cần thanh toán xu 1 lần duy nhất cho mỗi chương và có thể đọc lại bất cứ lúc nào trên mọi thiết bị.</p>
            </div>
            <div class="faq-item">
                <b>Tác giả có nhận được xu khi tôi ủng hộ không?</b>
                <p>Có, 100% số xu bạn gửi tặng sẽ được chuyển trực tiếp vào ví của tác giả theo cơ chế giao dịch phân tán tức thời.</p>
            </div>
            <div class="faq-item">
                <b>Làm sao để nhận xu miễn phí?</b>
                <p>Bạn có thể vào mục Hồ sơ cá nhân &gt; Điểm danh hàng ngày và hoàn thành nhiệm vụ để nhận thêm từ 5 đến 50 xu mỗi ngày!</p>
            </div>
        </div>
    </div>

</div>

<%-- Script tương tác gói nạp, phương thức thanh toán & cập nhật hiển thị đơn hàng --%>
<script>
(function() {
    var packageCards = document.querySelectorAll('.package-card');
    var methodCards = document.querySelectorAll('.payment-method-card');
    var inputPkg = document.getElementById('input-package-id');
    var inputMethod = document.getElementById('input-payment-method');

    var textPkgName = document.getElementById('checkout-pkg-name');
    var textBaseCoins = document.getElementById('checkout-base-coins');
    var textBonusCoins = document.getElementById('checkout-bonus-coins');
    var textTotalCoins = document.getElementById('checkout-total-coins');
    var textTotalPrice = document.getElementById('checkout-total-price');
    var qrMethodName = document.getElementById('qr-method-name');
    var qrMemoText = document.getElementById('qr-memo-text');
    var orderCodeDisplay = document.getElementById('order-code-display');

    // Tạo mã đơn ngẫu nhiên
    var randCode = 'ORD-' + Math.floor(1000 + Math.random() * 9000);
    if (orderCodeDisplay) orderCodeDisplay.textContent = randCode;

    function formatNumber(num) {
        return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".");
    }

    // Xử lý chọn gói
    packageCards.forEach(function(card) {
        card.addEventListener('click', function() {
            packageCards.forEach(function(c) { c.classList.remove('is-selected'); });
            card.classList.add('is-selected');

            var pkgId = card.getAttribute('data-pkg');
            var pkgName = card.getAttribute('data-name');
            var baseCoins = parseInt(card.getAttribute('data-base'), 10);
            var bonusCoins = parseInt(card.getAttribute('data-bonus'), 10);
            var totalCoins = parseInt(card.getAttribute('data-coins'), 10);
            var price = parseInt(card.getAttribute('data-price'), 10);

            if (inputPkg) inputPkg.value = pkgId;
            if (textPkgName) textPkgName.textContent = pkgName;
            if (textBaseCoins) textBaseCoins.textContent = formatNumber(baseCoins) + ' xu';
            if (textBonusCoins) textBonusCoins.textContent = '+' + formatNumber(bonusCoins) + ' xu';
            if (textTotalCoins) textTotalCoins.textContent = formatNumber(totalCoins) + ' xu';
            if (textTotalPrice) textTotalPrice.textContent = formatNumber(price) + ' ₫';

            updateQrMemo();
        });
    });

    // Xử lý chọn phương thức thanh toán
    methodCards.forEach(function(card) {
        card.addEventListener('click', function() {
            methodCards.forEach(function(c) { c.classList.remove('is-selected'); });
            card.classList.add('is-selected');

            var method = card.getAttribute('data-method');
            if (inputMethod) inputMethod.value = method;

            var titleEl = card.querySelector('.pm-title');
            if (qrMethodName && titleEl) {
                qrMethodName.textContent = titleEl.textContent;
            }

            updateQrMemo();
        });
    });

    function updateQrMemo() {
        if (!qrMemoText) return;
        var curPkg = inputPkg ? inputPkg.value.toUpperCase().replace('_', '') : 'PKG50K';
        qrMemoText.textContent = 'ND: NAPXU ' + curPkg + ' ' + randCode;
    }

    updateQrMemo();
})();
</script>
