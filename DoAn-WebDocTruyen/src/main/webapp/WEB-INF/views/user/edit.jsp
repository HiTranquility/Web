<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
================================================================================
  user/edit.jsp — MẢNH NỘI DUNG: Sửa hồ sơ                     TRANG 15
================================================================================
  TẦNG: views/

  Nhận: me (User)

  HAI FORM RIÊNG BIỆT TRONG MỘT TRANG
    Form trên sửa thông tin, form dưới đổi mật khẩu. Gộp làm một thì mỗi lần
    đổi tên hiển thị lại phải gõ cả mật khẩu — vô lý. Tách ra thì mỗi form có
    một quy tắc kiểm tra riêng, và hỏng cái này không xoá mất cái kia.
================================================================================
--%>
<div class="section-head">
    <h2>Sửa hồ sơ</h2>
    <a class="more" href="${pageContext.request.contextPath}/user?action=me">← Quay lại</a>
</div>

<form class="form-card" method="post"
      action="${pageContext.request.contextPath}/user">
    <input type="hidden" name="action" value="save">

    <div class="field">
        <label for="username">Tên đăng nhập</label>
        <%--
          disabled chứ không phải readonly, và KHÔNG có name.
          Ô disabled không được gửi lên server, nên dù có sửa bằng công cụ
          nhà phát triển thì cũng không có gì tới được UserServlet. Đó là
          hàng rào thật, không phải hàng rào trang trí.
          Server còn một lớp nữa: updateProfile() không hề có cột username.
        --%>
        <input type="text" id="username" value="${me.username}" disabled>
        <p class="field-hint">
            Không đổi được. Tên đăng nhập là danh tính — đổi thì mọi bình luận
            cũ và mọi đường dẫn hồ sơ đã chia sẻ đều trỏ sai người.
        </p>
    </div>

    <div class="field">
        <label for="displayName">Tên hiển thị *</label>
        <input type="text" id="displayName" name="displayName" maxlength="100"
               required value="<c:out value='${me.displayName}'/>">
        <p class="field-hint">Đây mới là tên người khác nhìn thấy.</p>
    </div>

    <div class="field">
        <label for="email">Email *</label>
        <input type="email" id="email" name="email" maxlength="150"
               required value="<c:out value='${me.email}'/>">
    </div>

    <div class="field">
        <label for="avatarUrl">Đường dẫn ảnh đại diện</label>
        <input type="url" id="avatarUrl" name="avatarUrl" maxlength="255"
               placeholder="https://..." value="<c:out value='${me.avatarUrl}'/>">
        <p class="field-hint">
            Để trống thì dùng chữ cái đầu của tên. Dán đường dẫn ảnh có sẵn —
            trang này chưa nhận tải ảnh lên.
        </p>
    </div>

    <div class="field">
        <label for="bio">Giới thiệu bản thân</label>
        <textarea id="bio" name="bio" rows="4" maxlength="500"
                  placeholder="Vài dòng về bạn và những gì bạn viết…"><c:out value="${me.bio}"/></textarea>
        <p class="field-hint">Tối đa 500 ký tự.</p>
    </div>

    <div class="form-actions">
        <button type="submit" class="btn btn-primary">Lưu thay đổi</button>
        <a class="btn btn-ghost"
           href="${pageContext.request.contextPath}/user?action=me">Huỷ</a>
    </div>
</form>

<div class="section-head" style="margin-top:34px">
    <h2>Đổi mật khẩu</h2>
</div>

<form class="form-card" method="post"
      action="${pageContext.request.contextPath}/user">
    <input type="hidden" name="action" value="password">

    <div class="field">
        <label for="oldPassword">Mật khẩu hiện tại *</label>
        <input type="password" id="oldPassword" name="oldPassword" required>
        <p class="field-hint">
            Phải nhập dù bạn đang đăng nhập — để người mượn máy lúc bạn quên
            đăng xuất không chiếm được tài khoản.
        </p>
    </div>

    <div class="field">
        <label for="newPassword">Mật khẩu mới *</label>
        <input type="password" id="newPassword" name="newPassword"
               minlength="6" required>
        <p class="field-hint">Từ 6 ký tự trở lên.</p>
    </div>

    <div class="field">
        <label for="confirmPassword">Nhập lại mật khẩu mới *</label>
        <input type="password" id="confirmPassword" name="confirmPassword"
               minlength="6" required>
    </div>

    <div class="form-actions">
        <button type="submit" class="btn btn-primary">Đổi mật khẩu</button>
    </div>
</form>
