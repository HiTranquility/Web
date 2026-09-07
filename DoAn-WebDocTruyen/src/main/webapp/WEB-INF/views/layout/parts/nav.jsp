<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  parts/nav.jsp — thanh menu trên cùng.

  DÙNG CHUNG cho layout main và layout admin — vì vậy CSS của nó nằm trong
  components.css chứ không phải layout-main.css. Đặt sai chỗ một lần rồi:
  khu quản trị mất sạch định dạng thanh menu.

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
            <a href="${pageContext.request.contextPath}/rank"
               class="${activeNav eq 'rank' ? 'is-active' : ''}">Xếp hạng</a>
            <a href="${pageContext.request.contextPath}/page?name=rules"
               class="${activeNav eq 'rules' ? 'is-active' : ''}">Nội quy</a>

            <c:if test="${not empty currentUser}">
                <a href="${pageContext.request.contextPath}/story?action=mine"
                   class="${activeNav eq 'mine' ? 'is-active' : ''}">Truyện của tôi</a>
                <a href="${pageContext.request.contextPath}/bookmark"
                   class="${activeNav eq 'bookmark' ? 'is-active' : ''}">Đã lưu</a>
                <a href="${pageContext.request.contextPath}/follow?action=list"
                   class="${activeNav eq 'follow' ? 'is-active' : ''}">Theo dõi</a>
            </c:if>
        </nav>

        <div class="header-actions">
            <c:choose>
                <c:when test="${not empty currentUser}">

                    <%--
                      Chuông thông báo.

                      GIỚI HẠN ĐÃ BIẾT: chấm đỏ chỉ hiện ở trang nào có đặt
                      unreadCount vào request. Muốn chấm đỏ xuất hiện trên MỌI
                      trang thì phải đếm ở một chỗ chạy trước mọi servlet —
                      đúng việc của một filter. Chưa làm; hiện chuông luôn dẫn
                      đúng tới /notification, chỉ là không báo số.
                    --%>
                    <a class="bell ${unreadCount gt 0 ? 'bell-on' : ''}"
                       href="${pageContext.request.contextPath}/notification"
                       title="Thông báo">
                        🔔
                        <c:if test="${unreadCount gt 0}">
                            <span class="bell-badge">${unreadCount gt 9 ? '9+' : unreadCount}</span>
                        </c:if>
                    </a>

                    <%-- Chỉ hiện với admin. isAdmin() trong model -> ${...admin} --%>
                    <c:if test="${currentUser.admin}">
                        <a class="btn btn-ghost btn-sm"
                           href="${pageContext.request.contextPath}/admin/dashboard">Quản trị</a>
                    </c:if>

                    <%-- Chip người dùng giờ là LINK vào hồ sơ của mình (trang 14) --%>
                    <a class="user-chip" title="<c:out value='${currentUser.username}'/>"
                       href="${pageContext.request.contextPath}/user?action=me">
                        <span class="user-avatar">${currentUser.initial}</span>
                        <c:out value="${currentUser.name}"/>
                    </a>

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
