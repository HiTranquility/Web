<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
================================================================================
  admin/comments.jsp — MẢNH NỘI DUNG: Quản lý bình luận         TRANG 29
================================================================================
  TẦNG: views/ · layout: admin

  Nhận: comments (List<Comment>) · filter (String: null | "hidden")
================================================================================
--%>
<h1>Quản trị bình luận</h1>
<p><b>Ẩn chứ không xoá.</b> Bình luận bị ẩn thì độc giả không thấy nữa nhưng
   dữ liệu vẫn còn — bấm nhầm khôi phục được, và còn nội dung gốc để đối chiếu
   nếu người viết quay lại khiếu nại.</p>

<div class="tag-row" style="margin-bottom:20px">
    <a class="tag ${empty filter ? 'is-on' : ''}"
       href="${pageContext.request.contextPath}/admin/comment">Tất cả</a>
    <a class="tag ${filter eq 'hidden' ? 'is-on' : ''}"
       href="${pageContext.request.contextPath}/admin/comment?filter=hidden">Đã ẩn</a>
</div>

<c:choose>
    <c:when test="${not empty comments}">
        <div class="admin-table-wrap">
        <table class="admin-table">
            <tr>
                <th style="width:150px">Người viết</th>
                <th>Nội dung</th>
                <th style="width:170px">Truyện</th>
                <th style="width:110px">Trạng thái</th>
                <th class="col-actions">Thao tác</th>
            </tr>
            <c:forEach var="cm" items="${comments}">
                <tr>
                    <td>
                        <span class="user-avatar">${cm.initial}</span>
                        <c:out value="${cm.name}"/>
                    </td>

                    <%--
                      c:out BẮT BUỘC ở đây.
                      Đây là chữ do người dùng gõ. Viết thẳng ${cm.content} là
                      mở cửa cho XSS — và trang quản trị chính là nơi kẻ tấn
                      công muốn đoạn mã của mình chạy nhất, vì người mở nó có
                      toàn quyền.
                    --%>
                    <td class="cell-text"><c:out value="${cm.content}"/></td>

                    <td>
                        <a href="${pageContext.request.contextPath}/story?action=detail&amp;id=${cm.storyId}">
                            <c:out value="${cm.storyTitle}"/></a>
                    </td>

                    <td>
                        <span class="pill ${cm.status eq 'HIDDEN' ? 'pill-danger' : 'pill-ok'}">
                            ${cm.status eq 'HIDDEN' ? 'Đã ẩn' : 'Hiển thị'}
                        </span>
                    </td>

                    <td class="col-actions">
                        <form method="post" style="display:inline"
                              action="${pageContext.request.contextPath}/admin/comment">
                            <input type="hidden" name="_csrf" value="${csrfToken}">
                            <input type="hidden" name="action"
                                   value="${cm.status eq 'HIDDEN' ? 'unhide' : 'hide'}">
                            <input type="hidden" name="id" value="${cm.id}">
                            <input type="hidden" name="filter" value="${filter}">
                            <button type="submit"
                                    class="btn btn-sm ${cm.status eq 'HIDDEN' ? 'btn-ghost' : 'btn-danger'}">
                                ${cm.status eq 'HIDDEN' ? 'Bỏ ẩn' : 'Ẩn'}
                            </button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
        </table>
        </div>
    </c:when>

    <c:otherwise>
        <c:set var="emIcon"  value="💬"/>
        <c:set var="emTitle"
               value="${filter eq 'hidden' ? 'Chưa ẩn bình luận nào' : 'Chưa có bình luận nào'}"/>
        <%@ include file="/WEB-INF/views/_partials/_empty.jsp" %>
    </c:otherwise>
</c:choose>
