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

<c:if test="${not empty message}">
    <div class="panel panel-err" style="margin-bottom:16px;"><c:out value="${message}"/></div>
</c:if>
<c:if test="${not empty sessionScope.flashError}">
    <div class="panel panel-err" style="margin-bottom:16px;"><c:out value="${sessionScope.flashError}"/></div>
    <c:remove var="flashError" scope="session"/>
</c:if>

<form class="form-card" method="post"
      action="${pageContext.request.contextPath}/user"
      enctype="multipart/form-data">
    <input type="hidden" name="_csrf" value="${csrfToken}">
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

    <div class="field avatar-upload-field">
        <label>Ảnh đại diện</label>
        <div class="avatar-edit-box">
            <div class="avatar-preview-wrap">
                <c:set var="avUrl" value="${me.avatarUrl}"/>
                <c:set var="avAlt" value="${me.name}"/>
                <c:set var="avInitial" value="${me.initial}"/>
                <c:set var="avClass" value="profile-avatar avatar-preview-img"/>
                <%@ include file="/WEB-INF/views/_partials/_avatar.jsp" %>
            </div>
            <div class="avatar-edit-controls">
                <div class="file-upload-btn-wrap">
                    <label for="avatarFile" class="btn btn-ghost btn-sm" style="display:inline-flex; align-items:center; gap:6px; cursor:pointer; border:1px solid var(--border, #334155); background:var(--surface-2, #1e293b); padding:7px 14px; border-radius:6px;">
                        <span>📁 Chọn ảnh từ máy</span>
                    </label>
                    <input type="file" id="avatarFile" name="avatarFile"
                           accept="image/png,image/jpeg,image/gif,image/webp" style="display:none;">
                    <span id="avatarFileName" class="file-chosen-name muted" style="margin-left:10px; font-size:.85rem;">Chưa chọn tệp mới</span>
                </div>
                <p class="field-hint" style="margin:6px 0 10px 0;">
                    PNG, JPG, GIF hoặc WebP (tối đa 2 MB).
                </p>

                <div class="avatar-url-input-wrap">
                    <label for="avatarUrl" class="field-hint" style="font-size:.85rem; display:block; margin-bottom:4px;">…hoặc dán đường dẫn ảnh:</label>
                    <input type="text" id="avatarUrl" name="avatarUrl" maxlength="1000"
                           placeholder="https://... hoặc /uploads/..." value="<c:out value='${me.avatarUrl}'/>">
                </div>

                <c:if test="${not empty me.avatarUrl}">
                    <div class="avatar-remove-wrap" style="margin-top:10px;">
                        <label style="color:var(--text-mut, #94a3b8); font-size:.86rem; cursor:pointer; display:inline-flex; align-items:center; gap:6px;">
                            <input type="checkbox" name="removeAvatar" value="1" id="removeAvatar">
                            <span>Xóa ảnh đại diện (quay về chữ cái đầu)</span>
                        </label>
                    </div>
                </c:if>
            </div>
        </div>
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

<div class="section-head" id="link-google" style="margin-top:34px">
    <h2>Tài khoản liên kết</h2>
</div>

<div class="form-card">
    <div class="field">
        <label>Google</label>
        <c:choose>
            <c:when test="${not empty googleIdentity}">
                <div style="display:flex; align-items:center; justify-content:space-between; flex-wrap:wrap; gap:12px; padding:12px 16px; background:var(--ink-850, #f8fafc); border:1px solid var(--ink-700, #e2e8f0); border-radius:8px;">
                    <div style="display:flex; align-items:center; gap:10px;">
                        <span class="pill pill-ok">Đã liên kết</span>
                        <b><c:out value="${googleIdentity.email}"/></b>
                    </div>
                    <form method="post" action="${pageContext.request.contextPath}/user" style="margin:0;"
                          onsubmit="return confirm('Bạn có chắc chắn muốn hủy liên kết tài khoản Google?');">
                        <input type="hidden" name="_csrf" value="${csrfToken}">
                        <input type="hidden" name="action" value="unlink-google">
                        <button type="submit" class="btn btn-ghost" style="color:#dc2626; border-color:#fca5a5;"
                                <c:if test="${!hasPassword}">disabled title="Cần tạo mật khẩu trước khi hủy liên kết"</c:if>>
                            Hủy liên kết
                        </button>
                    </form>
                </div>
                <c:if test="${!hasPassword}">
                    <p class="field-hint" style="color:#b45309; margin-top:8px;">
                        ⚠️ <strong>Lưu ý:</strong> Bạn chưa tạo mật khẩu riêng. Vui lòng tạo mật khẩu ở mục bên dưới trước khi hủy liên kết Google để không bị mất tài khoản.
                    </p>
                </c:if>
            </c:when>
            <c:otherwise>
                <div style="padding:12px 16px; background:var(--ink-850, #f8fafc); border:1px solid var(--ink-700, #e2e8f0); border-radius:8px;">
                    <p class="muted" style="margin:0 0 10px 0;">Chưa liên kết với tài khoản Google nào.</p>
                    <c:choose>
                        <c:when test="${googleEnabled}">
                            <button type="button" id="btn-link-google" class="btn btn-google" style="display:inline-flex;align-items:center;gap:8px;padding:8px 16px;border:1px solid var(--ink-700, #d1d5db);border-radius:6px;background:var(--ink-900, #ffffff);color:var(--text);cursor:pointer;font-weight:500;">
                                <svg width="18" height="18" viewBox="0 0 18 18"><path d="M17.64 9.2c0-.637-.057-1.251-.164-1.84H9v3.481h4.844c-.209 1.125-.843 2.078-1.796 2.717v2.258h2.908c1.702-1.567 2.684-3.874 2.684-6.616z" fill="#4285F4"/><path d="M9 18c2.43 0 4.467-.806 5.956-2.184l-2.908-2.258c-.806.54-1.837.86-3.048.86-2.344 0-4.328-1.584-5.036-3.711H.96v2.332C2.44 16.583 5.48 18 9 18z" fill="#34A853"/><path d="M3.964 10.707c-.18-.54-.282-1.117-.282-1.707 0-.59.102-1.167.282-1.707V4.961H.96C.348 6.173 0 7.55 0 9s.348 2.827.96 4.039l3.004-2.332z" fill="#FBBC05"/><path d="M9 3.58c1.321 0 2.508.454 3.44 1.345l2.582-2.58C13.463.891 11.426 0 9 0 5.482 0 2.438 2.017.957 4.961L3.964 7.293C4.672 5.166 6.656 3.58 9 3.58z" fill="#EA4335"/></svg>
                                <span>Liên kết với tài khoản Google</span>
                            </button>
                            <div id="google-link-feedback" style="margin-top:8px;"></div>
                        </c:when>
                        <c:otherwise>
                            <span class="muted-note">Chức năng Google Auth chưa được bật trên hệ thống.</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<c:choose>
    <c:when test="${hasPassword}">
        <div class="section-head" style="margin-top:34px">
            <h2>Đổi mật khẩu</h2>
        </div>

        <form class="form-card" method="post"
              action="${pageContext.request.contextPath}/user">
            <input type="hidden" name="_csrf" value="${csrfToken}">
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
    </c:when>
    <c:otherwise>
        <div class="section-head" style="margin-top:34px">
            <h2>Tạo mật khẩu lần đầu</h2>
        </div>

        <form class="form-card" method="post"
              action="${pageContext.request.contextPath}/user">
            <input type="hidden" name="_csrf" value="${csrfToken}">
            <input type="hidden" name="action" value="set-password">

            <p class="muted-note" style="margin-bottom:16px;">
                Tài khoản của bạn hiện đang đăng nhập qua Google và chưa có mật khẩu riêng.
                Tạo mật khẩu giúp bạn có thể đăng nhập bằng tên đăng nhập <strong><c:out value="${me.username}"/></strong> hoặc email bất kỳ lúc nào.
            </p>

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
                <button type="submit" class="btn btn-primary">Tạo mật khẩu</button>
            </div>
        </form>
    </c:otherwise>
</c:choose>

<script>
document.addEventListener('DOMContentLoaded', function() {
    const fileInput = document.getElementById('avatarFile');
    const fileNameSpan = document.getElementById('avatarFileName');
    const urlInput = document.getElementById('avatarUrl');
    const previewContainer = document.querySelector('.avatar-preview-wrap .profile-avatar');
    const removeCheck = document.getElementById('removeAvatar');
    const initialLetter = "<c:out value='${me.initial}'/>";

    if (!fileInput || !previewContainer) return;

    // Khi chọn file từ máy
    fileInput.addEventListener('change', function(e) {
        const file = e.target.files && e.target.files[0];
        if (file) {
            if (file.size > 2 * 1024 * 1024) {
                alert('Ảnh vượt quá dung lượng tối đa 2 MB. Vui lòng chọn ảnh nhỏ hơn.');
                fileInput.value = '';
                if (fileNameSpan) fileNameSpan.textContent = 'Chưa chọn tệp mới';
                return;
            }
            if (fileNameSpan) fileNameSpan.textContent = file.name;

            const reader = new FileReader();
            reader.onload = function(evt) {
                previewContainer.innerHTML = '<img src="' + evt.target.result + '" alt="Avatar" style="width:100%;height:100%;object-fit:cover;border-radius:inherit;display:block;">';
                if (removeCheck) removeCheck.checked = false;
            };
            reader.readAsDataURL(file);
        } else {
            if (fileNameSpan) fileNameSpan.textContent = 'Chưa chọn tệp mới';
        }
    });

    // Khi dán URL ảnh
    if (urlInput) {
        urlInput.addEventListener('input', function() {
            const val = urlInput.value.trim();
            if (val && (!fileInput.files || fileInput.files.length === 0)) {
                let src = val.startsWith('/') ? ('${pageContext.request.contextPath}' + val) : val;
                previewContainer.innerHTML = '<img src="' + src + '" alt="Avatar" onerror="this.onerror=null;this.parentElement.textContent=\'' + initialLetter + '\';" style="width:100%;height:100%;object-fit:cover;border-radius:inherit;display:block;">';
                if (removeCheck) removeCheck.checked = false;
            }
        });
    }

    // Khi tick vào ô xóa ảnh
    if (removeCheck) {
        removeCheck.addEventListener('change', function() {
            if (this.checked) {
                previewContainer.innerHTML = initialLetter;
                fileInput.value = '';
                if (fileNameSpan) fileNameSpan.textContent = 'Chưa chọn tệp mới';
            }
        });
    }
});
</script>

<c:if test="${googleEnabled}">
    <script>
        window.APP_CONTEXT = '${pageContext.request.contextPath}';
        window.FIREBASE_CONFIG = {
            apiKey: '<c:out value="${googleApiKey}"/>',
            authDomain: '<c:out value="${googleAuthDomain}"/>',
            projectId: '<c:out value="${googleProjectId}"/>',
            appId: '<c:out value="${googleAppId}"/>'
        };
    </script>
    <script src="https://www.gstatic.com/firebasejs/10.8.0/firebase-app-compat.js"></script>
    <script src="https://www.gstatic.com/firebasejs/10.8.0/firebase-auth-compat.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/firebase-auth.js"></script>
</c:if>
