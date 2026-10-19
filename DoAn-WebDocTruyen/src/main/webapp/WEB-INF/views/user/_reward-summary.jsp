<%@ page pageEncoding="UTF-8" %>
<section aria-label="Xu tích lũy" style="border:1px solid var(--ink-700);border-radius:12px;padding:18px;margin:18px 0 24px;">
    <div style="display:flex;flex-wrap:wrap;gap:20px 36px;">
        <div><span>Xu hiện có trong ví</span><br><strong data-wallet-balance>${walletBalance}</strong> xu</div>
        <div><span>Thưởng đã nhận hôm nay</span><br><strong data-today-earned>${rewardSummary.todayEarned}</strong> xu</div>
        <div><span>Tổng thưởng tích lũy</span><br><strong data-total-earned>${rewardSummary.totalEarned}</strong> xu</div>
    </div>
    <p class="muted small" style="margin:12px 0 0;">Ngày ghi nhận: <time data-reward-date>${rewardSummary.date}</time> (theo ngày máy chủ).
        Tổng thưởng gồm điểm danh, nhiệm vụ và thành tựu đã nhận; chưa gồm 100 xu khởi đầu, tiền tặng hoặc nạp xu.
        Chi tiêu làm giảm ví, không làm giảm tổng thưởng.</p>
    <a href="${pageContext.request.contextPath}/wallet">Xem lịch sử giao dịch →</a>
</section>
