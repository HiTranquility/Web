<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- admin/users.jsp — MẢNH. Quản trị tài khoản.   CASE 10 --%>
<h1>Quản trị tài khoản</h1>
<p>Khoá tài khoản vi phạm. <b>Truyện của họ vẫn giữ nguyên</b> trên trang —
   độc giả đang đọc dở không bị mất.</p>

<div class="admin-table-wrap">
<table class="admin-table">
    <tr>
        <th>Tài khoản</th><th>Email</th><th>Vai trò</th>
        <th>Trạng thái</th><th>Truyện</th><th class="col-actions">Thao tác</th>
    </tr>
    <c:forEach var="u" items="${users}">
        <tr>
            <td>
                <span class="user-avatar">${u.initial}</span>
                <c:out value="${u.name}"/>
            </td>
            <td><c:out value="${u.email}"/></td>
            <td>
                <span class="pill ${u.admin ? 'pill-warn' : 'pill-muted'}">
                    ${u.admin ? 'Admin' : 'Thành viên'}</span>
            </td>
            <td>
                <c:choose>
                    <c:when test="${u.banned}">
                        <span class="pill pill-danger">Đã khoá</span>
                        <c:if test="${not empty u.banReason}">
                            <br><small><c:out value="${u.banReason}"/></small>
                        </c:if>
                    </c:when>
                    <c:otherwise><span class="pill pill-ok">Hoạt động</span></c:otherwise>
                </c:choose>
            </td>
            <%-- bio đang chở story_count — xem ghi chú nợ kỹ thuật trong
                 AdminUserServlet.findAllUsers() --%>
            <td>${u.bio}</td>
            <td class="col-actions">
                <c:choose>
                    <c:when test="${currentUser.id eq u.id}">
                        <small class="muted-note">(bạn)</small>
                    </c:when>
                    <c:when test="${u.banned}">
                        <form action="${pageContext.request.contextPath}/admin/user"
                              method="post" style="display:inline">
                            <input type="hidden" name="action" value="unban">
                            <input type="hidden" name="id" value="${u.id}">
                            <button type="submit" class="btn btn-ghost btn-sm">Bỏ khoá</button>
                        </form>
                    </c:when>
                    <c:otherwise>
                        <form action="${pageContext.request.contextPath}/admin/user"
                              method="post" style="display:inline">
                            <input type="hidden" name="action" value="ban">
                            <input type="hidden" name="id" value="${u.id}">
                            <input type="text" name="reason" placeholder="Lý do…"
                                   class="inline-input">
                            <button type="submit" class="btn btn-danger btn-sm">Khoá</button>
                        </form>
                    </c:otherwise>
                </c:choose>
            </td>
        </tr>
    </c:forEach>
</table>
</div>
