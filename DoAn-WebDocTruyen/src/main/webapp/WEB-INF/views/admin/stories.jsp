<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- admin/stories.jsp — MẢNH. Quản trị truyện.   CASE 10
     AdminFilter đã chặn ở /admin/* nên tới đây chắc chắn là admin. --%>
<h1>Quản trị truyện</h1>
<p>Gỡ truyện vi phạm nội quy. Đây là <b>xoá mềm</b> — khôi phục lại được.</p>

<c:choose>
    <c:when test="${not empty stories}">
<div class="admin-table-wrap">
<table class="admin-table">
    <tr>
        <th>Tiêu đề</th><th>Tác giả</th><th>Trạng thái</th>
        <th>Chương</th><th>Lượt xem</th><th class="col-actions">Thao tác</th>
    </tr>
    <c:forEach var="s" items="${stories}">
        <tr>
            <td>
                <a href="${pageContext.request.contextPath}/story?action=detail&amp;id=${s.id}">
                    <c:out value="${s.title}"/></a>
            </td>
            <td><c:out value="${s.authorName}"/></td>
            <td>
                <c:choose>
                    <c:when test="${s.status eq 'PUBLISHED'}">
                        <span class="pill pill-ok">Công khai</span></c:when>
                    <c:when test="${s.status eq 'DRAFT'}">
                        <span class="pill pill-muted">Nháp</span></c:when>
                    <c:otherwise>
                        <span class="pill pill-danger">Đã gỡ</span></c:otherwise>
                </c:choose>
            </td>
            <td>${s.chapterCount}</td>
            <td><fmt:formatNumber pattern="#,##0" value="${s.viewCount}"/></td>
            <td class="col-actions">
                <c:choose>
                    <c:when test="${s.status eq 'DELETED'}">
                        <form action="${pageContext.request.contextPath}/admin/story"
                              method="post" style="display:inline">
                            <input type="hidden" name="_csrf" value="${csrfToken}">
                            <input type="hidden" name="action" value="restore">
                            <input type="hidden" name="id" value="${s.id}">
                            <button type="submit" class="btn btn-ghost btn-sm">Khôi phục</button>
                        </form>
                    </c:when>
                    <c:otherwise>
                        <form action="${pageContext.request.contextPath}/admin/story"
                              method="post" style="display:inline">
                            <input type="hidden" name="_csrf" value="${csrfToken}">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="id" value="${s.id}">
                            <button type="submit" class="btn btn-danger btn-sm">Gỡ</button>
                        </form>
                    </c:otherwise>
                </c:choose>
            </td>
        </tr>
    </c:forEach>
</table>
</div>
    </c:when>

    <c:otherwise>
        <%-- MẢNH: trạng thái rỗng, dùng chung với mọi trang danh sách.
             Bảng rỗng chỉ còn hàng tiêu đề cột trông như trang hỏng;
             một ô rỗng có thiết kế thì trông như chủ ý. --%>
        <c:set var="emIcon"  value="📚"/>
        <c:set var="emTitle" value="Không có truyện nào"/>
        <c:set var="emText"  value="Chưa ai đăng truyện, hoặc bộ lọc hiện tại không khớp truyện nào."/>
        <%@ include file="/WEB-INF/views/_partials/_empty.jsp" %>
    </c:otherwise>
</c:choose>

<c:if test="${empty stories}">
    <div class="empty" style="padding:40px"><p>Chưa có truyện nào.</p></div>
</c:if>
