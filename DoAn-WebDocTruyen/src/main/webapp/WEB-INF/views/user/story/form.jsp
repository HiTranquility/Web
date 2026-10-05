<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  story/form.jsp — MẢNH nội dung. Form đăng / sửa truyện.          CASE 05
  Nhận: story (rỗng khi tạo mới) · allTags · selectedTags · message
--%>
<div class="form-studio-wrapper">
    <div class="form-header-banner">
        <h1>${empty story.id or story.id eq 0 ? '✨ Đăng truyện mới' : '✍️ Chỉnh sửa truyện'}</h1>
        <p class="form-subtitle">Thiết lập nội dung, ảnh bìa và thể loại để phát hành tác phẩm trên hệ thống</p>
    </div>

    <c:if test="${not empty message}">
        <p class="form-error"><c:out value="${message}"/></p>
    </c:if>

    <form id="storyForm" action="${pageContext.request.contextPath}/story?_csrf=${csrfToken}" method="post"
          class="wide-form" enctype="multipart/form-data">
        <input type="hidden" name="_csrf" value="${csrfToken}">
        <input type="hidden" name="action"
               value="${empty story.id or story.id eq 0 ? 'create' : 'edit'}">
        <input type="hidden" name="id" id="storyId" value="${story.id}">

        <fieldset class="form-group">
            <legend>📝 Thông tin truyện</legend>

            <div class="field-with-counter">
                <label for="title">Tiêu đề *</label>
                <input type="text" id="title" name="title" maxlength="200" required
                       placeholder="Nhập tên truyện..."
                       value="<c:out value='${story.title}'/>">
                <div style="display:flex; justify-content:space-between; align-items:center; margin-top:6px; flex-wrap:wrap; gap:4px;">
                    <small id="slugPreviewRow" style="color:var(--text-mut);">
                        Đường dẫn dự kiến: <code id="slugPreview" style="color:var(--ember); background:var(--ink-800); padding:3px 8px; border-radius:6px; font-size:.82rem;">/truyen/<c:out value='${story.slug}'/></code>
                    </small>
                    <span id="storyTitleCounter" class="char-counter">0 / 200 ký tự</span>
                </div>
            </div>

            <div class="field-with-counter" style="margin-top:16px;">
                <label for="description">Giới thiệu tóm tắt</label>
                <textarea id="description" name="description" rows="5" maxlength="5000"
                          placeholder="Mô tả bối cảnh, giới thiệu nhân vật và cốt truyện lôi cuốn..."><c:out value="${story.description}"/></textarea>
                <span id="storyDescCounter" class="char-counter">0 / 5.000 ký tự</span>
            </div>

        </fieldset>

        <fieldset class="form-group">
            <legend>🖼️ Thiết kế ảnh bìa</legend>

            <div class="cover-edit-box">
                <div class="cover-preview-wrap">
                    <div id="cover-preview-box" class="story-cover-preview">
                        <c:choose>
                            <c:when test="${not empty story.coverUrl}">
                                <c:set var="cvUrl"     value="${story.coverUrl}"/>
                                <c:set var="cvAlt"     value="${story.title}"/>
                                <c:set var="cvInitial" value="${story.initial}"/>
                                <%@ include file="/WEB-INF/views/_partials/_cover.jsp" %>
                            </c:when>
                            <c:otherwise>
                                <span id="cover-empty-placeholder" style="font-size:2.4rem; color:var(--text-mut);">📖</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div class="cover-controls">
                    <div style="display:flex; align-items:center; gap:12px; flex-wrap:wrap; margin-bottom:8px;">
                        <label for="coverFile" class="file-upload-btn">
                            <span>📁 Chọn ảnh bìa từ máy</span>
                        </label>
                        <input type="file" id="coverFile" name="coverFile"
                               accept="image/png,image/jpeg,image/gif,image/webp" style="display:none;">
                        <span id="coverFileName" class="cover-file-status">Chưa chọn tệp mới</span>
                    </div>
                    <p class="field-hint" style="margin:6px 0 12px 0;">
                        PNG, JPG, GIF hoặc WebP (tối đa 2 MB). Tỷ lệ chuẩn 3:4.
                    </p>

                    <label for="coverUrl" class="field-label-sub">…hoặc dán đường dẫn ảnh:</label>
                    <input type="text" id="coverUrl" name="coverUrl"
                           value="<c:out value='${story.coverUrl}'/>"
                           placeholder="https://… hoặc /assets/images/covers/cover-1.svg">
                </div>
            </div>

        </fieldset>

        <fieldset class="form-group">
            <legend>🏷️ Phân loại &amp; Phát hành</legend>

        <div style="display:flex; align-items:baseline; gap:8px;">
            <label style="margin-bottom:0">Thể loại</label>
            <span id="selectedTagCount" style="font-size:.82rem; color:var(--ember); font-weight:600;"></span>
        </div>
        <div class="tag-picker" style="margin-top:6px;">
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
                       class="tag-checkbox"
                       ${checked ? 'checked' : ''}>
                <c:out value="${t.name}"/>
            </label>
        </c:forEach>
        </div>

        <div class="form-row" style="margin-top:14px;">
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
</div>

<script>
document.addEventListener('DOMContentLoaded', function () {
    var form = document.getElementById('storyForm');
    var titleInput = document.getElementById('title');
    var titleCounter = document.getElementById('storyTitleCounter');
    var descInput = document.getElementById('description');
    var descCounter = document.getElementById('storyDescCounter');
    var slugPreview = document.getElementById('slugPreview');
    var tagCheckboxes = document.querySelectorAll('.tag-checkbox');
    var selectedTagCount = document.getElementById('selectedTagCount');
    var coverFileInput = document.getElementById('coverFile');
    var coverFileName = document.getElementById('coverFileName');
    var coverUrlInput = document.getElementById('coverUrl');
    var previewBox = document.getElementById('cover-preview-box');

    // 1. TẠO SLUG TỰ ĐỘNG TỪ TIẾNG VIỆT CÓ DẤU
    function toSlug(str) {
        if (!str) return '...';
        str = str.toLowerCase();
        str = str.replace(/à|á|ạ|ả|ã|â|ầ|ấ|ậ|ẩ|ẫ|ă|ằ|ắ|ặ|ẳ|ẵ/g, 'a');
        str = str.replace(/è|é|ẹ|ẻ|ẽ|ê|ề|ế|ệ|ể|ễ/g, 'e');
        str = str.replace(/ì|í|ị|ỉ|ĩ/g, 'i');
        str = str.replace(/ò|ó|ọ|ỏ|õ|ô|ồ|ố|ộ|ổ|ỗ|ơ|ờ|ớ|ợ|ở|ỡ/g, 'o');
        str = str.replace(/ù|ú|ụ|ủ|ũ|ư|ừ|ứ|ự|ử|ữ/g, 'u');
        str = str.replace(/ỳ|ý|ỵ|ỷ|ỹ/g, 'y');
        str = str.replace(/đ/g, 'd');
        str = str.replace(/[^a-z0-9\s-]/g, '');
        str = str.trim().replace(/\s+/g, '-').replace(/-+/g, '-');
        return str || '...';
    }

    // 2. BỘ ĐẾM KÝ TỰ TIÊU ĐỀ & LIVE SLUG
    function updateTitleCounter() {
        if (!titleInput) return;
        var len = titleInput.value.length;
        if (titleCounter) {
            titleCounter.textContent = len + ' / 200 ký tự';
            if (len >= 200) {
                titleCounter.className = 'char-counter limit-full';
            } else if (len >= 180) {
                titleCounter.className = 'char-counter limit-near';
            } else {
                titleCounter.className = 'char-counter';
            }
        }
        if (slugPreview) {
            slugPreview.textContent = '/truyen/' + toSlug(titleInput.value);
        }
    }
    if (titleInput) {
        titleInput.addEventListener('input', updateTitleCounter);
        updateTitleCounter();
    }

    // 3. BỘ ĐẾM KÝ TỰ MÔ TẢ
    function updateDescCounter() {
        if (!descInput || !descCounter) return;
        var len = descInput.value.length;
        descCounter.textContent = len.toLocaleString('vi-VN') + ' / 5.000 ký tự';
        if (len >= 5000) {
            descCounter.className = 'char-counter limit-full';
        } else if (len >= 4500) {
            descCounter.className = 'char-counter limit-near';
        } else {
            descCounter.className = 'char-counter';
        }
    }
    if (descInput) {
        descInput.addEventListener('input', updateDescCounter);
        updateDescCounter();
    }

    // 4. ĐẾM SỐ THỂ LOẠI ĐÃ CHỌN
    function updateTagCount() {
        if (!selectedTagCount || !tagCheckboxes) return;
        var count = 0;
        tagCheckboxes.forEach(function (cb) {
            if (cb.checked) count++;
        });
        if (count > 0) {
            selectedTagCount.textContent = '· Đã chọn ' + count + ' thể loại';
        } else {
            selectedTagCount.textContent = '';
        }
    }
    if (tagCheckboxes.length > 0) {
        tagCheckboxes.forEach(function (cb) {
            cb.addEventListener('change', updateTagCount);
        });
        updateTagCount();
    }

    // 5. XỬ LÝ CHỌN VÀ XEM TRƯỚC ẢNH BÌA
    if (coverFileInput && previewBox) {
        coverFileInput.addEventListener('change', function (e) {
            var file = e.target.files && e.target.files[0];
            if (file) {
                if (file.size > 2 * 1024 * 1024) {
                    alert('⚠️ Ảnh bìa vượt quá dung lượng tối đa 2 MB. Vui lòng chọn ảnh nhỏ hơn.');
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
    }

    if (coverUrlInput && previewBox) {
        coverUrlInput.addEventListener('input', function () {
            var val = coverUrlInput.value.trim();
            if (val && (!coverFileInput.files || coverFileInput.files.length === 0)) {
                var src = val.startsWith('/') ? ('${pageContext.request.contextPath}' + val) : val;
                previewBox.style.borderStyle = 'solid';
                previewBox.innerHTML = '<img src="' + src + '" alt="Xem trước bìa" onerror="this.onerror=null;this.parentElement.innerHTML=\'<span style=\\\'font-size:2rem;color:#ef4444;\\\'>⚠️</span>\';" style="width:100%;height:100%;object-fit:cover;display:block;">';
            }
        });
    }

    // 6. CLIENT-SIDE VALIDATION TRƯỚC KHI SUBMIT
    if (form) {
        form.addEventListener('submit', function (e) {
            var title = titleInput ? titleInput.value.trim() : '';
            var desc = descInput ? descInput.value.trim() : '';

            if (title.length === 0) {
                e.preventDefault();
                alert('⚠️ Vui lòng nhập tiêu đề truyện (không được để trống hoặc chỉ chứa khoảng trắng).');
                if (titleInput) titleInput.focus();
                return;
            }

            if (title.length > 200) {
                e.preventDefault();
                alert('⚠️ Tiêu đề truyện không được vượt quá 200 ký tự.');
                if (titleInput) titleInput.focus();
                return;
            }

            if (desc.length > 5000) {
                e.preventDefault();
                alert('⚠️ Giới thiệu truyện không được vượt quá 5.000 ký tự.');
                if (descInput) descInput.focus();
                return;
            }
        });
    }
});
</script>
