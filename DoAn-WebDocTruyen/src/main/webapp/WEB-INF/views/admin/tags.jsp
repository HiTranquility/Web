<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
================================================================================
  admin/tags.jsp — MẢNH NỘI DUNG: Quản lý thể loại              TRANG 28
================================================================================
  TẦNG: views/ · layout: admin

  Nhận: tags (List<Tag> có storyCount)
================================================================================
--%>
<h1>Quản trị thể loại</h1>
<p>Thêm, đổi tên, xoá thể loại. <b>Slug sinh tự động</b> từ tên và không đổi
   khi đổi tên — mọi đường dẫn đã chia sẻ vẫn sống.</p>

<div class="panel">
    <h3 class="panel-title">➕ Thêm thể loại</h3>
    <form method="post" class="inline-form"
          action="${pageContext.request.contextPath}/admin/tag">
        <input type="hidden" name="_csrf" value="${csrfToken}">
        <input type="hidden" name="action" value="create">
        <input type="text" name="name" maxlength="50" required
               placeholder="Tên thể loại, ví dụ: Huyền huyễn" class="inline-input">
        <button type="submit" class="btn btn-primary btn-sm">Thêm</button>
    </form>
</div>

<div class="admin-table-wrap">
<table class="admin-table">
    <tr>
        <th style="width:70px">ID</th>
        <th>Tên</th>
        <th>Slug (trong URL)</th>
        <th style="width:100px">Truyện</th>
        <th class="col-actions">Thao tác</th>
    </tr>
    <c:forEach var="t" items="${tags}">
        <tr>
            <td class="muted">${t.id}</td>

            <td>
                <%-- Mỗi hàng là một form đổi tên tại chỗ. Không cần dựng hẳn
                     một trang sửa riêng cho đúng một trường. --%>
                <form method="post" class="inline-form"
                      action="${pageContext.request.contextPath}/admin/tag">
                    <input type="hidden" name="_csrf" value="${csrfToken}">
                    <input type="hidden" name="action" value="update">
                    <input type="hidden" name="id" value="${t.id}">
                    <input type="text" name="name" maxlength="50" required
                           class="inline-input"
                           value="<c:out value='${t.name}'/>">
                    <button type="submit" class="btn btn-ghost btn-sm">Lưu</button>
                </form>
            </td>

            <td><code><c:out value="${t.slug}"/></code></td>

            <td>
                <a href="${pageContext.request.contextPath}/story?action=list&amp;tag=${t.slug}">
                    ${t.storyCount} truyện</a>
            </td>

            <td class="col-actions">
                <c:choose>
                    <%--
                      Còn truyện thì KHÔNG cho xoá.
                      Khoá ngoại của story_tags khai ON DELETE CASCADE — xoá
                      thể loại là xoá im lặng mọi liên kết truyện–thể loại,
                      không có đường hoàn tác. Chặn ở đây để cái CASCADE đó
                      không bao giờ có cơ hội chạy.
                    --%>
                    <c:when test="${t.storyCount gt 0}">
                        <span class="muted-note"
                              title="Gỡ thể loại khỏi các truyện đó trước">
                            đang được dùng
                        </span>
                    </c:when>
                    <c:otherwise>
                        <form method="post" style="display:inline"
                              action="${pageContext.request.contextPath}/admin/tag"
                              onsubmit="return confirm('Xoá thể loại này?')">
                            <input type="hidden" name="_csrf" value="${csrfToken}">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="id" value="${t.id}">
                            <button type="submit" class="btn btn-danger btn-sm">Xoá</button>
                        </form>
                    </c:otherwise>
                </c:choose>
            </td>
        </tr>
    </c:forEach>
</table>
</div>
