<%@ page pageEncoding="UTF-8" %>
<%--
  parts/nav.jsp — thanh menu trên cùng.

  Tách khỏi main.jsp vì layout admin sau này cũng dùng lại thanh này, chỉ khác
  phần bên phải. Tách sẵn thì lúc đó khỏi phải chép.

  CASE 01 sẽ thay khối .header-actions bằng kiểm tra session:
      đã đăng nhập -> avatar + menu thả xuống
      chưa         -> hai nút Đăng nhập / Đăng ký
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
        </nav>

        <div class="header-actions">
            <a class="btn btn-ghost btn-sm"
               href="${pageContext.request.contextPath}/auth?action=login">Đăng nhập</a>
            <a class="btn btn-primary btn-sm"
               href="${pageContext.request.contextPath}/auth?action=register">Đăng ký</a>
        </div>
    </div>
</header>
