<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  story/form.jsp — MẢNH nội dung. Form đăng / sửa truyện.          CASE 05
  Nhận: story (rỗng khi tạo mới) · allTags · selectedTags · message
--%>
<h1>${empty story.id or story.id eq 0 ? 'Đăng truyện mới' : 'Sửa truyện'}</h1>

<c:if test="${not empty message}">
    <p class="form-error"><c:out value="${message}"/></p>
</c:if>

<%--
  enctype="multipart/form-data" BẮT BUỘC khi form có <input type="file">.

  Thiếu nó thì trình duyệt chỉ gửi TÊN file, không gửi nội dung — server nhận
  được một chuỗi vô dụng, không có lỗi nào để lần ra. Đây là kiểu hỏng im
  lặng: form gửi đi bình thường, chỉ ảnh không bao giờ tới nơi.

  Đổi lại, mọi ô khác cũng chuyển sang dạng multipart, nên servlet BẮT BUỘC
  phải có @MultipartConfig — không thì getParameter() trả null cho tất cả và
  form trông như người dùng bỏ trống hết.
--%>
<form action="${pageContext.request.contextPath}/story?_csrf=${csrfToken}" method="post"
      class="wide-form" enctype="multipart/form-data">
    <input type="hidden" name="_csrf" value="${csrfToken}">
    <input type="hidden" name="action"
           value="${empty story.id or story.id eq 0 ? 'create' : 'edit'}">
    <input type="hidden" name="id" value="${story.id}">

    <%--
      BA NHÓM CÓ TIÊU ĐỀ, thay cho một cột ô nhập xếp thẳng.

      Form này có 7 ô thuộc ba loại việc khác hẳn nhau: viết nội dung, chọn
      ảnh, phân loại. Xếp thẳng một mạch thì người dùng phải đọc hết mới biết
      còn gì phía dưới, và lúc sửa lại một chi tiết nhỏ thì phải dò từ đầu.

      <fieldset> + <legend> là thẻ HTML CÓ SẴN cho đúng việc này — trình đọc
          màn hình đọc tên nhóm trước khi đọc từng ô, nên người khiếm thị cũng
          nhận được cùng thông tin mà mắt thường thấy qua đường kẻ.
        --%>
    <fieldset class="form-group">
        <legend>Nội dung</legend>

        <label for="title">Tiêu đề *</label>
        <input type="text" id="title" name="title" maxlength="200" required
           value="<c:out value='${story.title}'/>">
        <small>Đường dẫn thân thiện (slug) tự sinh từ tiêu đề, trùng thì tự thêm số.</small>

        <label for="description">Giới thiệu</label>
        <textarea id="description" name="description" rows="5"><c:out value="${story.description}"/></textarea>

    </fieldset>

    <fieldset class="form-group">
        <legend>Ảnh bìa</legend>

        <%--
          HAI CÁCH ĐẶT BÌA, giữ cả hai vì phục vụ hai tình huống khác nhau:
        tải file  — ảnh nằm trong máy
        dán link  — ảnh đã có sẵn trên mạng

          Chọn cả hai thì FILE THẮNG (xem StoryServlet): người dùng vừa chủ động
          chọn file, còn ô link thường chỉ là giá trị cũ còn sót lại.
        --%>
        <div class="cover-edit-box" style="display:flex; gap:20px; align-items:flex-start; flex-wrap:wrap; margin-top:10px; padding:16px; background:var(--surface-1, rgba(255,255,255,0.03)); border:1px solid var(--border, rgba(255,255,255,0.1)); border-radius:12px;">
            <div class="cover-preview-wrap" style="flex:none;">
                <div id="cover-preview-box" class="story-cover-preview" style="width:105px; height:145px; border-radius:8px; overflow:hidden; position:relative; background:var(--surface-2, #1e293b); border:2px dashed var(--border, #475569); display:grid; place-items:center; box-shadow:0 4px 14px rgba(0,0,0,0.2);">
                    <c:choose>
                        <c:when test="${not empty story.coverUrl}">
                            <c:set var="cvUrl"     value="${story.coverUrl}"/>
                            <c:set var="cvAlt"     value="${story.title}"/>
                            <c:set var="cvInitial" value="${story.initial}"/>
                            <%@ include file="/WEB-INF/views/_partials/_cover.jsp" %>
                        </c:when>
                        <c:otherwise>
                            <span id="cover-empty-placeholder" style="font-size:2.2rem; color:var(--text-mut, #94a3b8);">📖</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="cover-controls" style="flex:1; min-width:220px;">
                <div style="display:flex; align-items:center; gap:10px; flex-wrap:wrap; margin-bottom:8px;">
                    <label for="coverFile" class="btn btn-ghost btn-sm" style="cursor:pointer; display:inline-flex; align-items:center; gap:6px; border:1px solid var(--border, #334155); background:var(--surface-2, #1e293b); padding:7px 14px; border-radius:6px;">
                        <span>📁 Chọn ảnh bìa từ máy</span>
                    </label>
                    <input type="file" id="coverFile" name="coverFile"
                           accept="image/png,image/jpeg,image/gif,image/webp" style="display:none;">
                    <span id="coverFileName" style="font-size:.85rem; color:var(--text-mut, #94a3b8);">Chưa chọn tệp mới</span>
                </div>
                <p class="field-hint" style="margin:4px 0 10px 0; font-size:.85rem; color:var(--text-mut, #94a3b8);">
                    PNG, JPG, GIF hoặc WebP (tối đa 2 MB). Tỷ lệ chuẩn 3:4.
                </p>

                <label for="coverUrl" style="font-size:.85rem; display:block; margin-bottom:4px; color:var(--text-mut, #94a3b8);">…hoặc dán đường dẫn ảnh:</label>
                <input type="text" id="coverUrl" name="coverUrl"
                       value="<c:out value='${story.coverUrl}'/>"
                       placeholder="https://… hoặc /assets/images/covers/cover-1.svg">
            </div>
        </div>

    </fieldset>

    <fieldset class="form-group">
        <legend>Phân loại &amp; trạng thái</legend>

        <label>Thể loại</label>
        <div class="tag-picker">
        <c:forEach var="t" items="${allTags}">
            <%--
              Đánh dấu tag đã chọn: duyệt selectedTags tìm id trùng.
              Cách này là O(n×m), nhưng n và m đều ~10 nên không đáng lo.
              Nếu số tag lên hàng trăm thì nên cho servlet dựng sẵn một Set id.
            --%>
            <c:set var="checked" value="false"/>
            <c:forEach var="st" items="${selectedTags}">
                <c:if test="${st.id eq t.id}"><c:set var="checked" value="true"/></c:if>
            </c:forEach>

            <label class="tag-check">
                <input type="checkbox" name="tagIds" value="${t.id}"
                       ${checked ? 'checked' : ''}>
                <c:out value="${t.name}"/>
            </label>
        </c:forEach>
        </div>

        <div class="form-row">
        <div>
            <label for="status">Trạng thái</label>
            <select id="status" name="status">
                <option value="DRAFT" ${story.status eq 'DRAFT' ? 'selected' : ''}>
                    Bản nháp — chỉ mình tôi thấy</option>
                <option value="PUBLISHED" ${story.status eq 'PUBLISHED' ? 'selected' : ''}>
                    Công khai</option>
            </select>
        </div>
        <div>
            <label for="progress">Tiến độ</label>
            <select id="progress" name="progress">
                <option value="ONGOING" ${story.progress eq 'ONGOING' ? 'selected' : ''}>
                    Đang ra</option>
                <option value="COMPLETED" ${story.progress eq 'COMPLETED' ? 'selected' : ''}>
                    Hoàn thành</option>
            </select>
        </div>
        </div>
    </fieldset>

    <div class="form-actions">
        <button type="submit" class="btn btn-primary">Lưu truyện</button>
        <a class="btn btn-ghost"
           href="${pageContext.request.contextPath}/story?action=mine">Huỷ</a>

        <c:if test="${not empty story.id and story.id ne 0}">
            <%-- Gỡ truyện là XOÁ MỀM nên khôi phục được — nói rõ trong lời hỏi
                 để người dùng không hoảng. Dùng POST + token CSRF để bảo mật. --%>
            <button type="submit" form="deleteStoryForm" class="btn btn-danger" style="margin-left:auto"
                    onclick="return confirm('Gỡ truyện này khỏi trang? Admin có thể khôi phục lại.')">
                Gỡ truyện</button>
        </c:if>
    </div>
</form>

<c:if test="${not empty story.id and story.id ne 0}">
    <form id="deleteStoryForm" action="${pageContext.request.contextPath}/story" method="post" style="display:none">
        <input type="hidden" name="_csrf" value="${csrfToken}">
        <input type="hidden" name="action" value="delete">
        <input type="hidden" name="id" value="${story.id}">
    </form>
</c:if>

<script>
document.addEventListener('DOMContentLoaded', function () {
    var coverFileInput = document.getElementById('coverFile');
    var coverFileName = document.getElementById('coverFileName');
    var coverUrlInput = document.getElementById('coverUrl');
    var previewBox = document.getElementById('cover-preview-box');

    if (!coverFileInput || !previewBox) return;

    coverFileInput.addEventListener('change', function (e) {
        var file = e.target.files && e.target.files[0];
        if (file) {
            if (file.size > 2 * 1024 * 1024) {
                alert('Ảnh bìa vượt quá dung lượng tối đa 2 MB. Vui lòng chọn ảnh nhỏ hơn.');
                coverFileInput.value = '';
                if (coverFileName) coverFileName.textContent = 'Chưa chọn tệp mới';
                return;
            }
            if (coverFileName) coverFileName.textContent = file.name;
            var reader = new FileReader();
            reader.onload = function (evt) {
                previewBox.style.borderStyle = 'solid';
                previewBox.innerHTML = '<img src="' + evt.target.result + '" alt="Xem trước bìa" style="width:100%;height:100%;object-fit:cover;display:block;">';
            };
            reader.readAsDataURL(file);
        } else {
            if (coverFileName) coverFileName.textContent = 'Chưa chọn tệp mới';
        }
    });

    if (coverUrlInput) {
        coverUrlInput.addEventListener('input', function () {
            var val = coverUrlInput.value.trim();
            if (val && (!coverFileInput.files || coverFileInput.files.length === 0)) {
                var src = val.startsWith('/') ? ('${pageContext.request.contextPath}' + val) : val;
                previewBox.style.borderStyle = 'solid';
                previewBox.innerHTML = '<img src="' + src + '" alt="Xem trước bìa" onerror="this.onerror=null;this.parentElement.innerHTML=\'<span style=\\\'font-size:2rem;color:#ef4444;\\\'>⚠️</span>\';" style="width:100%;height:100%;object-fit:cover;display:block;">';
            }
        });
    }
});
</script>
