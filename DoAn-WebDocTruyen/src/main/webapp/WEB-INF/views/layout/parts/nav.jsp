<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  parts/nav.jsp — thanh menu trên cùng.

  Tách khỏi main.jsp vì layout admin sau này cũng dùng lại thanh này, chỉ khác
  phần bên phải. Tách sẵn thì lúc đó khỏi phải chép.

  CASE 01 đã xong: khối .header-actions kiểm tra ${currentUser} trong SESSION.
      currentUser != null -> avatar + tên + nút Đăng xuất
      currentUser == null -> hai nút Đăng nhập / Đăng ký

  currentUser là attribute DUY NHẤT của dự án nằm ở session scope
  (xem standards/02-VIEW_CONVENTIONS.md §3). EL tự tìm qua 4 scope nên viết
  ${currentUser} là đủ, không cần ${sessionScope.currentUser}.
--%>
<header class="site-header">
    <div class="shell">
        <a href="${pageContext.request.contextPath}/" class="brand">
            <span class="brand-mark">📖</span>
            <span>Đọc<em>Truyện</em></span>
        </a>

        <nav class="nav">
            <a href="${pageContext.request.contextPath}/"
               class="${activeNav eq 'home' ? 'is-active' : ''}">Trang chủ</a>
            <a href="${pageContext.request.contextPath}/story?action=list"
               class="${activeNav eq 'browse' ? 'is-active' : ''}">Kho truyện</a>
            <a href="${pageContext.request.contextPath}/page?name=rules"
               class="${activeNav eq 'rules' ? 'is-active' : ''}">Nội quy</a>
            <c:if test="${not empty currentUser}">
                <a href="${pageContext.request.contextPath}/story?action=mine">Truyện của tôi</a>
                <a href="${pageContext.request.contextPath}/bookmark?action=list">Đã lưu</a>
            </c:if>
        </nav>

        <div class="header-actions">
            <c:choose>
                <c:when test="${not empty currentUser}">
                    <%-- Chỉ hiện với admin. isAdmin() trong model -> ${...admin} --%>
                    <c:if test="${currentUser.admin}">
                        <a class="btn btn-ghost btn-sm"
                           href="${pageContext.request.contextPath}/admin/story">Quản trị</a>
                    </c:if>

                    <span class="user-chip" title="<c:out value='${currentUser.username}'/>">
                        <span class="user-avatar">${currentUser.initial}</span>
                        <c:out value="${currentUser.name}"/>
                    </span>

                    <%-- Đăng xuất là hành động GHI (huỷ phiên) nhưng để form
                         POST thì rườm rà cho một nút nhỏ. Ở quy mô đồ án,
                         link GET chấp nhận được. --%>
                    <a class="btn btn-ghost btn-sm"
                       href="${pageContext.request.contextPath}/auth?action=logout">Đăng xuất</a>
                </c:when>
                <c:otherwise>
                    <a class="btn btn-ghost btn-sm"
                       href="${pageContext.request.contextPath}/auth?action=login">Đăng nhập</a>
                    <a class="btn btn-primary btn-sm"
                       href="${pageContext.request.contextPath}/auth?action=register">Đăng ký</a>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</header>
