<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
================================================================================
  admin/audit.jsp — MẢNH NỘI DUNG: Nhật ký Thao tác Quản trị (ISSUE-023)
================================================================================
  TẦNG: views/ · layout: admin
  Nhận: auditLogs (List<AuditLog>), total, page, totalPages, actionFilter
================================================================================
--%>
<div class="admin-header-flex" style="display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:1.5rem; flex-wrap:wrap; gap:1rem;">
    <div>
        <h1 style="margin-bottom:.3rem;">📜 Nhật ký Thao tác Quản trị</h1>
        <p style="color:var(--text-mut); margin:0;">
            Hồ sơ kiểm duyệt bất biến ghi lại mọi tác vụ quản trị (ISSUE-023). Sổ ghi nhận <b>chỉ đọc</b> để bảo đảm minh bạch và chống lạm quyền.
        </p>
    </div>
    <div style="background:var(--ink-800); padding:8px 16px; border-radius:var(--r-sm); border:1px solid var(--ink-700); font-size:.9rem;">
        Tổng số bản ghi: <b><c:out value="${total}"/></b>
    </div>
</div>

<div class="tag-row" style="margin-bottom:20px; display:flex; flex-wrap:wrap; gap:8px;">
    <a class="tag ${empty actionFilter or actionFilter eq 'ALL' ? 'is-on' : ''}"
       href="${pageContext.request.contextPath}/admin/audit">Tất cả</a>
    <a class="tag ${actionFilter eq 'DELETE_STORY' ? 'is-on' : ''}"
       href="${pageContext.request.contextPath}/admin/audit?action=DELETE_STORY">🗑️ Gỡ truyện</a>
    <a class="tag ${actionFilter eq 'RESTORE_STORY' ? 'is-on' : ''}"
       href="${pageContext.request.contextPath}/admin/audit?action=RESTORE_STORY">♻️ Khôi phục truyện</a>
    <a class="tag ${actionFilter eq 'BAN_USER' ? 'is-on' : ''}"
       href="${pageContext.request.contextPath}/admin/audit?action=BAN_USER">🚫 Khoá tài khoản</a>
    <a class="tag ${actionFilter eq 'UNBAN_USER' ? 'is-on' : ''}"
       href="${pageContext.request.contextPath}/admin/audit?action=UNBAN_USER">🔓 Mở khoá nick</a>
    <a class="tag ${actionFilter eq 'HIDE_COMMENT' ? 'is-on' : ''}"
       href="${pageContext.request.contextPath}/admin/audit?action=HIDE_COMMENT">💬 Ẩn bình luận</a>
    <a class="tag ${actionFilter eq 'GRANT_COIN' ? 'is-on' : ''}"
       href="${pageContext.request.contextPath}/admin/audit?action=GRANT_COIN">🪙 Cấp xu</a>
    <a class="tag ${actionFilter eq 'REVOKE_COIN' ? 'is-on' : ''}"
       href="${pageContext.request.contextPath}/admin/audit?action=REVOKE_COIN">💸 Thu hồi xu</a>
</div>

<c:choose>
    <c:when test="${not empty auditLogs}">
        <div style="background:var(--surface); border-radius:var(--r-md); border:1px solid var(--ink-700); overflow-x:auto;">
            <table class="table" style="width:100%; border-collapse:collapse; text-align:left; font-size:.92rem;">
                <thead>
                    <tr style="background:var(--ink-800); border-bottom:1px solid var(--ink-700); color:var(--text-mut);">
                        <th style="padding:12px 16px;">Thời gian</th>
                        <th style="padding:12px 16px;">Quản trị viên</th>
                        <th style="padding:12px 16px;">Hành động</th>
                        <th style="padding:12px 16px;">Đối tượng tác động</th>
                        <th style="padding:12px 16px;">Lý do / Ghi chú</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="log" items="${auditLogs}">
                        <tr style="border-bottom:1px solid var(--ink-800);">
                            <td style="padding:12px 16px; white-space:nowrap; color:var(--text-mut); font-family:var(--font-mono, monospace); font-size:.85rem;">
                                <c:out value="${log.formattedTime}"/>
                            </td>
                            <td style="padding:12px 16px; white-space:nowrap;">
                                <c:choose>
                                    <c:when test="${not empty log.adminUsername}">
                                        <b><c:out value="${log.adminName}"/></b>
                                        <span style="color:var(--text-mut); font-size:.82rem;">(@<c:out value="${log.adminUsername}"/>)</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span style="color:var(--text-mut); font-style:italic;">Admin hệ thống</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td style="padding:12px 16px; white-space:nowrap;">
                                <span class="badge ${log.actionBadgeClass}" style="padding:4px 8px; border-radius:4px; font-weight:600; font-size:.82rem;">
                                    <c:out value="${log.actionLabel}"/>
                                </span>
                            </td>
                            <td style="padding:12px 16px;">
                                <span style="font-weight:500;"><c:out value="${log.targetLabel}"/></span>
                                <span style="color:var(--text-mut); font-size:.8rem; display:block;">Loại: <c:out value="${log.targetType}"/> (#<c:out value="${log.targetId}"/>)</span>
                            </td>
                            <td style="padding:12px 16px; color:var(--text); max-width:320px;">
                                <c:choose>
                                    <c:when test="${not empty log.reason}">
                                        <c:out value="${log.reason}"/>
                                    </c:when>
                                    <c:otherwise>
                                        <span style="color:var(--text-mut); font-style:italic;">Không ghi lý do</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>

        <%-- Phân trang --%>
        <c:if test="${totalPages gt 1}">
            <div style="display:flex; justify-content:center; gap:8px; margin-top:24px;">
                <c:if test="${page gt 1}">
                    <a class="btn btn-sm btn-ghost" href="${pageContext.request.contextPath}/admin/audit?action=${actionFilter}&amp;page=${page - 1}">← Trang trước</a>
                </c:if>
                <span style="align-self:center; font-size:.9rem; color:var(--text-mut);">Trang ${page} / ${totalPages}</span>
                <c:if test="${page lt totalPages}">
                    <a class="btn btn-sm btn-ghost" href="${pageContext.request.contextPath}/admin/audit?action=${actionFilter}&amp;page=${page + 1}">Trang sau →</a>
                </c:if>
            </div>
        </c:if>
    </c:when>
    <c:otherwise>
        <div class="panel" style="padding:40px 20px; text-align:center; color:var(--text-mut);">
            <div style="font-size:2.5rem; margin-bottom:12px;">📜</div>
            <b>Chưa có ghi nhận thao tác quản trị nào trong mục này.</b>
            <p style="margin-top:6px; font-size:.9rem;">Mọi hành động gỡ truyện, khoá nick hay cấp/thu xu của Admin sẽ tự động lưu lại ở đây.</p>
        </div>
    </c:otherwise>
</c:choose>
