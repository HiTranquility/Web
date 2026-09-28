<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- admin/stories.jsp — MẢNH. Quản trị & Duyệt truyện.   CASE 10
     AdminFilter đã chặn ở /admin/* nên tới đây chắc chắn là admin. --%>
<div class="section-head" style="margin-bottom:8px">
    <h1>Quản trị &amp; Duyệt truyện</h1>
</div>
<p style="color:var(--text-dim); margin-bottom:20px;">
    Kiểm duyệt nội dung, duyệt đăng truyện mới hoặc gỡ truyện vi phạm nội quy. Thao tác gỡ là <b>xoá mềm</b> — có thể khôi phục bất cứ lúc nào.
</p>

<%-- BỘ LỌC THEO TRẠNG THÁI --%>
<div class="tag-row" style="margin-bottom:16px;">
    <a class="tag ${empty status ? 'is-on' : ''}"
       href="${pageContext.request.contextPath}/admin/story">
        Tất cả (${totalCount})
    </a>
    <a class="tag ${status eq 'PUBLISHED' ? 'is-on' : ''}"
       href="${pageContext.request.contextPath}/admin/story?status=PUBLISHED">
        Công khai (${publishedCount})
    </a>
    <a class="tag ${status eq 'DRAFT' ? 'is-on' : ''}"
       style="${draftCount gt 0 ? 'border-color:var(--ember);' : ''}"
       href="${pageContext.request.contextPath}/admin/story?status=DRAFT">
        Bản nháp / Chờ duyệt <c:if test="${draftCount gt 0}"><b style="color:var(--ember);">(${draftCount})</b></c:if>
    </a>
    <a class="tag ${status eq 'DELETED' ? 'is-on' : ''}"
       href="${pageContext.request.contextPath}/admin/story?status=DELETED">
        Đã gỡ (${deletedCount})
    </a>
</div>

<%-- Ô TÌM KIẾM NHANH --%>
<div style="margin-bottom:18px; display:flex; align-items:center; gap:12px; flex-wrap:wrap;">
    <input type="text" id="storyQuickFilter"
           placeholder="🔍 Tìm nhanh theo tên truyện hoặc tác giả..."
           style="max-width:380px; width:100%; padding:8px 14px; border-radius:var(--r-sm); border:1px solid var(--ink-700); background:var(--ink-900); color:var(--text); font-size:.9rem;">
    <span id="storyFilterCount" style="font-size:.84rem; color:var(--text-mut);"></span>
</div>

<c:choose>
    <c:when test="${not empty stories}">
<div class="admin-table-wrap">
<table class="admin-table" id="adminStoryTable">
    <thead>
        <tr>
            <th>Tiêu đề</th><th>Tác giả</th><th>Trạng thái</th>
            <th>Chương</th><th>Lượt xem</th><th class="col-actions">Thao tác</th>
        </tr>
    </thead>
    <tbody>
    <c:forEach var="s" items="${stories}">
        <tr class="story-data-row" data-title="<c:out value='${s.title}'/>" data-author="<c:out value='${s.authorName}'/>">
            <td>
                <a href="${pageContext.request.contextPath}/story?action=detail&amp;id=${s.id}" style="font-weight:600; color:var(--text);">
                    <c:out value="${s.title}"/></a>
                <c:if test="${s.status eq 'DRAFT'}">
                    <span class="badge badge-warning" style="font-size:.72rem; margin-left:6px; background:rgba(240,134,58,0.15); color:var(--ember); padding:2px 6px; border-radius:4px;">Chờ duyệt</span>
                </c:if>
            </td>
            <td><c:out value="${s.authorName}"/></td>
            <td>
                <c:choose>
                    <c:when test="${s.status eq 'PUBLISHED'}">
                        <span class="pill pill-ok">Công khai</span></c:when>
                    <c:when test="${s.status eq 'DRAFT'}">
                        <span class="pill pill-muted" style="background:rgba(240,134,58,0.15); color:var(--ember); border:1px solid rgba(240,134,58,0.3);">Nháp</span></c:when>
                    <c:otherwise>
                        <span class="pill pill-danger">Đã gỡ</span></c:otherwise>
                </c:choose>
            </td>
            <td><strong>${s.chapterCount}</strong></td>
            <td><fmt:formatNumber pattern="#,##0" value="${s.viewCount}"/></td>
            <td class="col-actions">
                <c:choose>
                    <c:when test="${s.status eq 'DELETED'}">
                        <form action="${pageContext.request.contextPath}/admin/story"
                              method="post" style="display:inline">
                            <input type="hidden" name="_csrf" value="${csrfToken}">
                            <input type="hidden" name="action" value="restore">
                            <input type="hidden" name="id" value="${s.id}">
                            <input type="hidden" name="status" value="${status}">
                            <button type="submit" class="btn btn-ghost btn-sm"
                                    onclick="return confirm('Khôi phục truyện này lại trạng thái Công khai?')">Khôi phục</button>
                        </form>
                    </c:when>
                    <c:when test="${s.status eq 'DRAFT'}">
                        <form action="${pageContext.request.contextPath}/admin/story"
                              method="post" style="display:inline">
                            <input type="hidden" name="_csrf" value="${csrfToken}">
                            <input type="hidden" name="action" value="publish">
                            <input type="hidden" name="id" value="${s.id}">
                            <input type="hidden" name="status" value="${status}">
                            <button type="submit" class="btn btn-primary btn-sm"
                                    title="Duyệt đăng truyện này lên kho truyện công khai">
                                ✓ Duyệt đăng</button>
                        </form>
                        <form action="${pageContext.request.contextPath}/admin/story"
                              method="post" style="display:inline">
                            <input type="hidden" name="_csrf" value="${csrfToken}">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="id" value="${s.id}">
                            <input type="hidden" name="status" value="${status}">
                            <button type="submit" class="btn btn-danger btn-sm"
                                    onclick="return confirm('Gỡ bản nháp này?')">Gỡ</button>
                        </form>
                    </c:when>
                    <c:otherwise>
                        <form action="${pageContext.request.contextPath}/admin/story"
                              method="post" style="display:inline">
                            <input type="hidden" name="_csrf" value="${csrfToken}">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="id" value="${s.id}">
                            <input type="hidden" name="status" value="${status}">
                            <button type="submit" class="btn btn-danger btn-sm"
                                    onclick="return confirm('Gỡ truyện này khỏi kho truyện?')">Gỡ</button>
                        </form>
                    </c:otherwise>
                </c:choose>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>
</div>

<script>
document.addEventListener('DOMContentLoaded', function () {
    var searchInput = document.getElementById('storyQuickFilter');
    var countNotice = document.getElementById('storyFilterCount');
    var rows = document.querySelectorAll('.story-data-row');

    if (!searchInput || !rows) return;

    searchInput.addEventListener('input', function () {
        var query = searchInput.value.trim().toLowerCase();
        var visible = 0;

        rows.forEach(function (row) {
            var title = (row.getAttribute('data-title') || '').toLowerCase();
            var author = (row.getAttribute('data-author') || '').toLowerCase();

            if (!query || title.includes(query) || author.includes(query)) {
                row.style.display = '';
                visible++;
            } else {
                row.style.display = 'none';
            }
        });

        if (query) {
            countNotice.textContent = 'Tìm thấy ' + visible + ' / ' + rows.length + ' truyện';
        } else {
            countNotice.textContent = '';
        }
    });
});
</script>
    </c:when>

    <c:otherwise>
        <%-- MẢNH: trạng thái rỗng, dùng chung với mọi trang danh sách. --%>
        <c:set var="emIcon"  value="📚"/>
        <c:set var="emTitle" value="Không có truyện nào"/>
        <c:set var="emText"  value="Chưa có truyện nào trong mục này hoặc bộ lọc hiện tại không khớp truyện nào."/>
        <%@ include file="/WEB-INF/views/_partials/_empty.jsp" %>
    </c:otherwise>
</c:choose>

