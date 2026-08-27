<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
================================================================================
  layout/admin.jsp — KHUNG KHU QUẢN TRỊ                    (CASE 10)
================================================================================
  Khác main.jsp: có menu bên trái (sidebar).

  Vẫn dùng LẠI parts/nav.jsp của layout main — không chép lại. Nhờ vậy sửa
  logo hay menu chính một lần là cả hai khung đổi theo. Đó chính là lý do
  parts/ được tách ra từ đầu.

  AdminFilter đã chặn ở /admin/* nên tới được đây chắc chắn là admin.
================================================================================
--%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <c:set var="layoutCss" value="layout-admin" scope="request"/>
    <%@ include file="parts/head.jsp" %>
</head>
<body>

<%-- Dùng chung với layout main — không chép lại --%>
<%@ include file="parts/nav.jsp" %>

<div class="admin-shell">
    <aside class="admin-side">
        <h5>Quản trị</h5>
        <nav>
            <a href="${pageContext.request.contextPath}/admin/story"
               class="${adminSection eq 'story' ? 'is-active' : ''}">📚 Truyện</a>
            <a href="${pageContext.request.contextPath}/admin/user"
               class="${adminSection eq 'user' ? 'is-active' : ''}">👤 Tài khoản</a>
        </nav>
        <p class="admin-hint">
            Gỡ truyện và khoá tài khoản đều là <b>xoá mềm</b> — bấm nhầm vẫn
            khôi phục được.
        </p>
    </aside>

    <main class="admin-main">
        <c:if test="${not empty message}">
            <div class="panel panel-warn"><c:out value="${message}"/></div>
        </c:if>
        <jsp:include page="${contentPage}" />
    </main>
</div>

<%@ include file="parts/footer.jsp" %>

</body>
</html>
