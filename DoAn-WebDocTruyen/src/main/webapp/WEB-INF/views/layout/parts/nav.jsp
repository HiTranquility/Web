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

        <%--
          Ô TICK ẨN làm công tắc cho menu điện thoại.

          VÌ SAO KHÔNG DÙNG <details> NHƯ CÁC CHỖ KHÁC
            Đã thử và HỎNG: bảng menu phải position:absolute để nổi lên trên
            nội dung (đẩy nội dung xuống thì cả trang nhảy mỗi lần mở menu),
            nhưng phần tử absolute lại THOÁT khỏi cơ chế ẩn của <details> —
            đóng thẻ lại mà menu vẫn hiện nguyên.
            Chỗ xoá lịch sử và mục lục lúc đọc không gặp lỗi này vì chúng
            không cần nổi lên trên.

          Cách này CSS điều khiển hoàn toàn: :checked ~ .nav là hiện, không
          thì ẩn. Trên màn hình rộng thì luật đó bị tắt và .nav luôn hiện, nên
          desktop không phụ thuộc vào trạng thái ô tick.

          Ô tick KHÔNG display:none mà chỉ đẩy ra ngoài tầm nhìn — display:none
          thì bàn phím không Tab tới được, người không dùng chuột mất menu.
        --%>
        <input type="checkbox" id="nav-open" class="nav-check" aria-label="Mở menu">
        <label for="nav-open" class="nav-toggle" aria-hidden="true">☰</label>

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
                <%-- Đặt NGAY SAU "Đã lưu": hai mục cùng trả lời "truyện nào
                     liên quan tới tôi", chỉ khác một cái do tôi tự chọn, một
                     cái hệ thống tự ghi. Để cạnh nhau thì so sánh ra ngay. --%>
                <a href="${pageContext.request.contextPath}/history"
                   class="${activeNav eq 'history' ? 'is-active' : ''}">Lịch sử</a>
                <a href="${pageContext.request.contextPath}/follow?action=list"
                   class="${activeNav eq 'follow' ? 'is-active' : ''}">Theo dõi</a>
            </c:if>
        </nav>

        <div class="header-actions">
            <c:choose>
                <c:when test="${not empty currentUser}">

                    <%--
                      Chuông thông báo, có chấm đỏ đếm số chưa đọc.

                      ${unreadCount} do NotificationFilter đặt vào request —
                      filter chạy trước MỌI servlet nên chấm đỏ hiện ở mọi
                      trang mà không servlet nào phải nhớ đếm hộ.

                      Trên 9 thì hiện "9+": con số chính xác không giúp gì thêm,
                      mà ba chữ số làm vỡ hình tròn.
                    --%>
                    <a class="bell" title="Thông báo"
                       href="${pageContext.request.contextPath}/notification">
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
