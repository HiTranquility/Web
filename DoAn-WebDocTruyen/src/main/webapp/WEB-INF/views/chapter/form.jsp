<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  chapter/form.jsp — MẢNH nội dung. Thêm / sửa chương.             CASE 06
  Nhận: chapter · story · message
--%>
<h1>${empty chapter.id or chapter.id eq 0 ? 'Thêm chương' : 'Sửa chương'}</h1>
<p class="muted-note">Truyện: <c:out value="${story.title}"/></p>

<c:if test="${not empty message}">
    <p class="form-error"><c:out value="${message}"/></p>
</c:if>

<form action="${pageContext.request.contextPath}/chapter" method="post" class="wide-form">
    <input type="hidden" name="action"
           value="${empty chapter.id or chapter.id eq 0 ? 'create' : 'edit'}">
    <input type="hidden" name="id" value="${chapter.id}">
    <input type="hidden" name="storyId" value="${story.id}">

    <div class="form-row">
        <div style="max-width:9em">
            <label for="chapterNo">Số chương *</label>
            <%-- Servlet đã điền sẵn số kế tiếp (MAX + 1) khi thêm mới --%>
            <input type="number" id="chapterNo" name="chapterNo" min="1" required
                   value="${chapter.chapterNo}">
        </div>
        <div style="flex:1">
            <label for="title">Tiêu đề chương *</label>
            <input type="text" id="title" name="title" maxlength="200" required
                   value="<c:out value='${chapter.title}'/>">
        </div>
    </div>

    <label for="content">Nội dung *</label>
    <%-- <c:out> bên trong textarea cũng cần escape: nội dung cũ có thể chứa
         </textarea> và làm vỡ cả form. --%>
    <textarea id="content" name="content" rows="22" required
              class="content-editor"><c:out value="${chapter.content}"/></textarea>
    <small>Xuống dòng để tách đoạn — hệ thống giữ nguyên khi hiển thị.</small>

    <div class="form-actions">
        <button type="submit" class="btn btn-primary">Lưu chương</button>
        <a class="btn btn-ghost"
           href="${pageContext.request.contextPath}/story?action=detail&amp;id=${story.id}">Huỷ</a>

        <c:if test="${not empty chapter.id and chapter.id ne 0}">
            <%-- Chương xoá THẬT (khác truyện) nên phải cảnh báo rõ. --%>
            <a class="btn btn-danger" style="margin-left:auto"
               href="${pageContext.request.contextPath}/chapter?action=delete&amp;id=${chapter.id}"
               onclick="return confirm('Xoá hẳn chương này? Không khôi phục được.')">
                Xoá chương</a>
        </c:if>
    </div>
</form>
