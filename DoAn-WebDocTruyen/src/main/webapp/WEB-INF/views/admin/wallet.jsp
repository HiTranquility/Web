<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--
================================================================================
  admin/wallet.jsp — MẢNH NỘI DUNG: Quản lý Ví xu & Dòng tiền (ISSUE-023)
================================================================================
  TẦNG: views/ · layout: admin
  Nhận: wallets, total, page, totalPages, q, totalCoins, totalTxs, totalTipped, recentTxs
================================================================================
--%>
<div class="admin-header-flex" style="display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:1.5rem; flex-wrap:wrap; gap:1rem;">
    <div>
        <h1 style="margin-bottom:.3rem;">💰 Quản lý Ví xu Toàn hệ thống</h1>
        <p style="color:var(--text-mut); margin:0;">
            Kiểm soát lưu thông dòng tiền xu ảo, tra cứu giao dịch và xử lý khiếu nại cấp/thu hồi xu (ISSUE-023).
        </p>
    </div>
</div>

<%-- Thống kê dòng tiền ở đầu trang --%>
<div class="stat-grid" style="margin-bottom:24px;">
    <div class="stat-tile" style="background:var(--surface); border:1px solid var(--ink-700); padding:16px 20px; border-radius:var(--r-md);">
        <div style="font-size:1.8rem; margin-bottom:4px;">🪙</div>
        <div style="font-size:1.6rem; font-weight:700; color:var(--text);">
            <fmt:formatNumber pattern="#,##0" value="${totalCoins}"/>
        </div>
        <div style="font-size:.85rem; color:var(--text-mut);">Tổng xu đang lưu hành</div>
    </div>

    <div class="stat-tile" style="background:var(--surface); border:1px solid var(--ink-700); padding:16px 20px; border-radius:var(--r-md);">
        <div style="font-size:1.8rem; margin-bottom:4px;">🔄</div>
        <div style="font-size:1.6rem; font-weight:700; color:var(--text);">
            <fmt:formatNumber pattern="#,##0" value="${totalTxs}"/>
        </div>
        <div style="font-size:.85rem; color:var(--text-mut);">Tổng lượt giao dịch</div>
    </div>

    <div class="stat-tile" style="background:var(--surface); border:1px solid var(--ink-700); padding:16px 20px; border-radius:var(--r-md);">
        <div style="font-size:1.8rem; margin-bottom:4px;">🎁</div>
        <div style="font-size:1.6rem; font-weight:700; color:var(--text);">
            <fmt:formatNumber pattern="#,##0" value="${totalTipped}"/>
        </div>
        <div style="font-size:.85rem; color:var(--text-mut);">Xu đã tặng &amp; chi tiêu</div>
    </div>

    <div class="stat-tile" style="background:var(--surface); border:1px solid var(--ink-700); padding:16px 20px; border-radius:var(--r-md);">
        <div style="font-size:1.8rem; margin-bottom:4px;">👥</div>
        <div style="font-size:1.6rem; font-weight:700; color:var(--text);">
            <fmt:formatNumber pattern="#,##0" value="${total}"/>
        </div>
        <div style="font-size:.85rem; color:var(--text-mut);">Ví tài khoản hoạt động</div>
    </div>
</div>

<%-- Thanh tìm kiếm tài khoản ví --%>
<div style="margin-bottom:20px; display:flex; gap:12px; flex-wrap:wrap; align-items:center;">
    <form action="${pageContext.request.contextPath}/admin/wallet" method="get" style="display:flex; gap:8px; flex:1; max-width:480px;">
        <input type="text" name="q" value="<c:out value='${q}'/>" placeholder="🔍 Tìm theo username, tên hoặc email..."
               style="flex:1; padding:8px 14px; border-radius:var(--r-sm); border:1px solid var(--ink-700); background:var(--ink-900); color:var(--text); font-size:.9rem;">
        <button type="submit" class="btn btn-sm btn-primary">Tìm kiếm</button>
        <c:if test="${not empty q}">
            <a href="${pageContext.request.contextPath}/admin/wallet" class="btn btn-sm btn-ghost">Xoá lọc</a>
        </c:if>
    </form>
</div>

<%-- Bảng danh sách ví tài khoản --%>
<div style="background:var(--surface); border-radius:var(--r-md); border:1px solid var(--ink-700); overflow-x:auto; margin-bottom:32px;">
    <div style="padding:14px 16px; border-bottom:1px solid var(--ink-700); font-weight:600;">
        Danh sách Ví thành viên
    </div>
    <table class="table" style="width:100%; border-collapse:collapse; text-align:left; font-size:.92rem;">
        <thead>
            <tr style="background:var(--ink-800); border-bottom:1px solid var(--ink-700); color:var(--text-mut);">
                <th style="padding:12px 16px;">Tài khoản</th>
                <th style="padding:12px 16px;">Email</th>
                <th style="padding:12px 16px; text-align:right;">Số dư hiện tại</th>
                <th style="padding:12px 16px; text-align:right;">Đã nhận</th>
                <th style="padding:12px 16px; text-align:right;">Đã tiêu</th>
                <th style="padding:12px 16px; text-align:center;">Thao tác Admin</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="w" items="${wallets}">
                <tr style="border-bottom:1px solid var(--ink-800);">
                    <td style="padding:12px 16px;">
                        <b><c:out value="${w.displayName}"/></b>
                        <span style="display:block; color:var(--text-mut); font-size:.82rem;">@<c:out value="${w.username}"/> (#<c:out value="${w.userId}"/>)</span>
                    </td>
                    <td style="padding:12px 16px; color:var(--text-mut); font-size:.85rem;">
                        <c:out value="${w.email}"/>
                    </td>
                    <td style="padding:12px 16px; text-align:right; font-weight:700; color:#fbbf24;">
                        <fmt:formatNumber pattern="#,##0" value="${w.balance}"/> xu
                    </td>
                    <td style="padding:12px 16px; text-align:right; color:var(--success, #10b981);">
                        +<fmt:formatNumber pattern="#,##0" value="${w.totalReceived}"/> xu
                    </td>
                    <td style="padding:12px 16px; text-align:right; color:var(--text-mut);">
                        <fmt:formatNumber pattern="#,##0" value="${w.totalSpent}"/> xu
                    </td>
                    <td style="padding:12px 16px; text-align:center;">
                        <button type="button" class="btn btn-sm btn-ghost"
                                onclick="openAdjustModal('${w.userId}', '<c:out value="${w.username}"/>', ${w.balance})"
                                style="font-size:.82rem; padding:4px 10px;">
                            ⚙️ Cấp / Thu xu
                        </button>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</div>

<%-- Phân trang danh sách ví --%>
<c:if test="${totalPages gt 1}">
    <div style="display:flex; justify-content:center; gap:8px; margin-bottom:32px;">
        <c:if test="${page gt 1}">
            <a class="btn btn-sm btn-ghost" href="${pageContext.request.contextPath}/admin/wallet?q=${q}&amp;page=${page - 1}">← Trang trước</a>
        </c:if>
        <span style="align-self:center; font-size:.9rem; color:var(--text-mut);">Trang ${page} / ${totalPages}</span>
        <c:if test="${page lt totalPages}">
            <a class="btn btn-sm btn-ghost" href="${pageContext.request.contextPath}/admin/wallet?q=${q}&amp;page=${page + 1}">Trang sau →</a>
        </c:if>
    </div>
</c:if>

<%-- Lịch sử giao dịch gần đây trên toàn hệ thống --%>
<div style="background:var(--surface); border-radius:var(--r-md); border:1px solid var(--ink-700); overflow-x:auto;">
    <div style="padding:14px 16px; border-bottom:1px solid var(--ink-700); font-weight:600;">
        Giao dịch gần nhất toàn hệ thống
    </div>
    <table class="table" style="width:100%; border-collapse:collapse; text-align:left; font-size:.88rem;">
        <thead>
            <tr style="background:var(--ink-800); border-bottom:1px solid var(--ink-700); color:var(--text-mut);">
                <th style="padding:10px 14px;">Thời gian</th>
                <th style="padding:10px 14px;">Người chuyển</th>
                <th style="padding:10px 14px;">Người nhận</th>
                <th style="padding:10px 14px; text-align:right;">Số xu</th>
                <th style="padding:10px 14px;">Lời nhắn / Tác phẩm</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="tx" items="${recentTxs}">
                <tr style="border-bottom:1px solid var(--ink-800);">
                    <td style="padding:10px 14px; color:var(--text-mut); font-size:.82rem; white-space:nowrap;">
                        <c:out value="${tx.createdAt}"/>
                    </td>
                    <td style="padding:10px 14px;">
                        <c:choose>
                            <c:when test="${not empty tx.fromUsername}">
                                <b><c:out value="${tx.fromUsername}"/></b>
                            </c:when>
                            <c:otherwise><span style="color:var(--text-mut);">Hệ thống nạp</span></c:otherwise>
                        </c:choose>
                    </td>
                    <td style="padding:10px 14px;">
                        <b><c:out value="${tx.toUsername}"/></b>
                    </td>
                    <td style="padding:10px 14px; text-align:right; font-weight:700; color:#fbbf24;">
                        <fmt:formatNumber pattern="#,##0" value="${tx.amount}"/> xu
                    </td>
                    <td style="padding:10px 14px; color:var(--text-mut);">
                        <c:out value="${tx.message}"/>
                        <c:if test="${not empty tx.storyTitle}">
                            <span style="display:block; color:var(--primary); font-size:.8rem;">📖 <c:out value="${tx.storyTitle}"/></span>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</div>

<%-- Modal điều chỉnh xu của Admin --%>
<div class="app-modal-backdrop" id="adjustModal" onclick="if(event.target===this) closeAdjustModal();">
    <div class="app-modal-card" style="max-width:440px;" role="dialog" aria-modal="true" aria-labelledby="adjustModalTitle">
        <div class="app-modal-head">
            <div class="modal-title-wrap">
                <span class="modal-title-icon">⚙️</span>
                <div>
                    <h3 id="adjustModalTitle">Điều chỉnh Ví xu</h3>
                    <p class="modal-subtitle">
                        Tài khoản: <b id="modalUsername" style="color:var(--text);"></b> (Số dư hiện tại: <b id="modalBalance"></b> xu)
                    </p>
                </div>
            </div>
            <button type="button" class="app-modal-close" onclick="closeAdjustModal()" aria-label="Đóng">&times;</button>
        </div>

        <form action="${pageContext.request.contextPath}/admin/wallet" method="post">
            <input type="hidden" name="_csrf" value="${csrfToken}">
            <input type="hidden" name="action" value="adjust">
            <input type="hidden" name="userId" id="modalUserId">

            <div class="app-modal-body" style="padding:16px 20px;">
                <div style="margin-bottom:12px;">
                    <label style="display:block; margin-bottom:6px; font-size:.85rem; color:var(--text-mut);">Hành động:</label>
                    <select name="type" style="width:100%; padding:8px 12px; border-radius:var(--r-sm); border:1px solid var(--ink-700); background:var(--ink-900); color:var(--text);">
                        <option value="grant">➕ Cấp thêm xu (Cộng)</option>
                        <option value="revoke">➖ Thu hồi xu (Trừ)</option>
                    </select>
                </div>

                <div style="margin-bottom:12px;">
                    <label style="display:block; margin-bottom:6px; font-size:.85rem; color:var(--text-mut);">Số xu:</label>
                    <input type="number" name="amount" min="1" max="100000" required placeholder="Nhập số xu (VD: 500)"
                           style="width:100%; padding:8px 12px; border-radius:var(--r-sm); border:1px solid var(--ink-700); background:var(--ink-900); color:var(--text);">
                </div>

                <div style="margin-bottom:12px;">
                    <label style="display:block; margin-bottom:6px; font-size:.85rem; color:var(--text-mut);">Lý do điều chỉnh (Bắt buộc):</label>
                    <textarea name="reason" rows="3" required placeholder="Ghi rõ lý do (VD: Tặng thưởng sự kiện / Xử lý khiếu nại tặng nhầm...)"
                              style="width:100%; padding:8px 12px; border-radius:var(--r-sm); border:1px solid var(--ink-700); background:var(--ink-900); color:var(--text); font-size:.9rem;"></textarea>
                </div>
            </div>

            <div class="app-modal-foot" style="display:flex; justify-content:flex-end; gap:8px;">
                <button type="button" class="btn btn-sm btn-ghost" onclick="closeAdjustModal()">Huỷ</button>
                <button type="submit" class="btn btn-sm btn-primary"
                        data-confirm="Bạn có chắc chắn muốn thực hiện điều chỉnh số dư ví của thành viên này không?"
                        data-confirm-title="Xác nhận điều chỉnh ví"
                        data-confirm-ok="Đồng ý thực hiện">Xác nhận thực hiện</button>
            </div>
        </form>
    </div>
</div>

<script>
function openAdjustModal(userId, username, balance) {
    document.getElementById('modalUserId').value = userId;
    document.getElementById('modalUsername').innerText = '@' + username;
    document.getElementById('modalBalance').innerText = Number(balance).toLocaleString();
    var modal = document.getElementById('adjustModal');
    if (modal) {
        modal.classList.add('is-open');
        document.body.style.overflow = 'hidden';
    }
}

function closeAdjustModal() {
    var modal = document.getElementById('adjustModal');
    if (modal) {
        modal.classList.remove('is-open');
        document.body.style.overflow = '';
    }
}

document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape' || e.keyCode === 27) {
        var modal = document.getElementById('adjustModal');
        if (modal && modal.classList.contains('is-open')) {
            closeAdjustModal();
        }
    }
});
</script>
