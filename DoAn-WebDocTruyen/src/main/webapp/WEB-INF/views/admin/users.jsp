<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- admin/users.jsp — MẢNH NỘI DUNG. Quản trị tài khoản.   TRANG 27
     TẦNG: views/  ·  Nhận: users (List<User> có storyCount) · q · statusFilter --%>
<h1>Quản trị tài khoản</h1>
<p>Khoá tài khoản vi phạm. <b>Truyện của họ vẫn giữ nguyên</b> trên trang —
   độc giả đang đọc dở không bị mất.</p>

<%--
  Ô TÌM + LỌC TRẠNG THÁI.

  method="get" chứ không phải post: đây là hành động ĐỌC, không đổi gì cả.
  Được lợi thêm là kết quả tìm nằm trên URL — admin gửi link
  "?q=spam&status=BANNED" cho đồng nghiệp là họ thấy đúng danh sách đó, và
  F5 không hỏi "gửi lại biểu mẫu?".
--%>
<form class="search-bar" method="get"
      action="${pageContext.request.contextPath}/admin/user">
    <input type="text" name="q" value="<c:out value='${q}'/>"
           placeholder="Tìm theo tên đăng nhập, tên hiển thị hoặc email…">

    <select name="status" class="inline-input">
        <option value=""       ${empty statusFilter        ? 'selected' : ''}>Mọi trạng thái</option>
        <option value="ACTIVE" ${statusFilter eq 'ACTIVE'  ? 'selected' : ''}>Đang hoạt động</option>
        <option value="BANNED" ${statusFilter eq 'BANNED'  ? 'selected' : ''}>Đã khoá</option>
    </select>

    <button type="submit" class="btn btn-primary btn-sm">Tìm</button>

    <%-- Nút xoá lọc chỉ hiện khi ĐANG lọc. Hiện thường trực thì nó là một
         nút không làm gì trong phần lớn thời gian. --%>
    <c:if test="${not empty q or not empty statusFilter}">
        <a class="btn btn-ghost btn-sm"
           href="${pageContext.request.contextPath}/admin/user">Xoá lọc</a>
    </c:if>
</form>

<p class="muted-note" style="margin:-6px 0 16px">
    <c:choose>
        <c:when test="${empty users}">
            Không có tài khoản nào khớp.
        </c:when>
        <c:when test="${not empty q or not empty statusFilter}">
            Tìm thấy <b>${users.size()}</b> tài khoản.
        </c:when>
        <c:otherwise>
            <b>${users.size()}</b> tài khoản, mới nhất trước (tối đa 200).
        </c:otherwise>
    </c:choose>
</p>

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

                <%-- Đổi vai trò. Nút chỉ hiện với tài khoản KHÁC mình —
                     admin tự hạ quyền mình là tự khoá cửa. --%>
                <c:if test="${currentUser.id ne u.id}">
                    <form action="${pageContext.request.contextPath}/admin/user"
                          method="post" style="display:inline">
                        <input type="hidden" name="action" value="role">
                        <input type="hidden" name="id" value="${u.id}">
                        <input type="hidden" name="q" value="<c:out value='${q}'/>">
                        <input type="hidden" name="status" value="<c:out value='${statusFilter}'/>">
                        <input type="hidden" name="role"
                               value="${u.admin ? 'USER' : 'ADMIN'}">
                        <button type="submit" class="btn btn-ghost btn-sm">
                            ${u.admin ? '↓ Hạ' : '↑ Nâng'}</button>
                    </form>
                </c:if>
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
            <td>${u.storyCount}</td>
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
                            <input type="hidden" name="q" value="<c:out value='${q}'/>">
                            <input type="hidden" name="status" value="<c:out value='${statusFilter}'/>">
                            <button type="submit" class="btn btn-ghost btn-sm">Bỏ khoá</button>
                        </form>
                    </c:when>
                    <c:otherwise>
                        <form action="${pageContext.request.contextPath}/admin/user"
                              method="post" style="display:inline">
                            <input type="hidden" name="action" value="ban">
                            <input type="hidden" name="id" value="${u.id}">
                            <input type="hidden" name="q" value="<c:out value='${q}'/>">
                            <input type="hidden" name="status" value="<c:out value='${statusFilter}'/>">
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
