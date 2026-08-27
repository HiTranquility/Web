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

<form action="${pageContext.request.contextPath}/story" method="post" class="wide-form">
    <input type="hidden" name="action"
           value="${empty story.id or story.id eq 0 ? 'create' : 'edit'}">
    <input type="hidden" name="id" value="${story.id}">

    <label for="title">Tiêu đề *</label>
    <input type="text" id="title" name="title" maxlength="200" required
           value="<c:out value='${story.title}'/>">
    <small>Đường dẫn thân thiện (slug) tự sinh từ tiêu đề, trùng thì tự thêm số.</small>

    <label for="description">Giới thiệu</label>
    <textarea id="description" name="description" rows="5"><c:out value="${story.description}"/></textarea>

    <label for="coverUrl">Link ảnh bìa</label>
    <input type="url" id="coverUrl" name="coverUrl"
           value="<c:out value='${story.coverUrl}'/>"
           placeholder="https://…  (để trống thì lấy chữ cái đầu làm bìa)">

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

    <div class="form-actions">
        <button type="submit" class="btn btn-primary">Lưu truyện</button>
        <a class="btn btn-ghost"
           href="${pageContext.request.contextPath}/story?action=mine">Huỷ</a>

        <c:if test="${not empty story.id and story.id ne 0}">
            <%-- Gỡ truyện là XOÁ MỀM nên khôi phục được — nói rõ trong lời hỏi
                 để người dùng không hoảng. --%>
            <a class="btn btn-danger" style="margin-left:auto"
               href="${pageContext.request.contextPath}/story?action=delete&amp;id=${story.id}"
               onclick="return confirm('Gỡ truyện này khỏi trang? Admin có thể khôi phục lại.')">
                Gỡ truyện</a>
        </c:if>
    </div>
</form>
