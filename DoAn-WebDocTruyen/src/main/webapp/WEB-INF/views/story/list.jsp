<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  story/list.jsp — MẢNH nội dung. Kho truyện + lọc tag + phân trang.   CASE 02+03
  Nhận: stories · tags · currentTag · keyword · sort · page · totalPages · totalStories
--%>
<div class="section-head">
    <h2>
        <c:choose>
            <c:when test="${not empty keyword}">Tìm: "<c:out value='${keyword}'/>"</c:when>
            <c:when test="${not empty currentTag}">Thể loại đã lọc</c:when>
            <c:otherwise>Kho truyện</c:otherwise>
        </c:choose>
    </h2>
    <span class="more">${totalStories} truyện</span>
</div>

<%-- Ô tìm kiếm. method="get" vì tìm kiếm chỉ ĐỌC — và nhờ vậy URL kết quả
     chia sẻ và bookmark được. --%>
<form action="${pageContext.request.contextPath}/story" method="get" class="search-bar">
    <input type="hidden" name="action" value="list">
    <c:if test="${not empty currentTag}">
        <input type="hidden" name="tag" value="<c:out value='${currentTag}'/>">
    </c:if>
    <input type="text" name="q" placeholder="Tìm theo tên truyện…"
           value="<c:out value='${keyword}'/>">
    <button type="submit" class="btn btn-primary btn-sm">Tìm</button>
</form>

<%-- ---- Bộ lọc thể loại ---- --%>
<div class="tag-row" style="margin-bottom:22px">
    <a class="tag ${empty currentTag ? 'is-on' : ''}"
       href="${pageContext.request.contextPath}/story?action=list">Tất cả</a>
    <c:forEach var="t" items="${tags}">
        <a class="tag ${currentTag eq t.slug ? 'is-on' : ''}"
           href="${pageContext.request.contextPath}/story?action=list&amp;tag=${t.slug}">
            <c:out value="${t.name}"/> (${t.storyCount})
        </a>
    </c:forEach>
</div>

<c:choose>
    <c:when test="${not empty stories}">
        <div class="story-grid">
            <c:forEach var="story" items="${stories}">
                <%@ include file="/WEB-INF/views/story/_card.jsp" %>
            </c:forEach>
        </div>

        <%-- ---- Phân trang ----
             Giữ nguyên tag/keyword/sort khi chuyển trang, không thì bấm sang
             trang 2 là mất bộ lọc — lỗi rất hay gặp. --%>
        <c:if test="${totalPages > 1}">
            <c:set var="qs" value="action=list"/>
            <c:if test="${not empty currentTag}"><c:set var="qs" value="${qs}&tag=${currentTag}"/></c:if>
            <c:if test="${not empty keyword}"><c:set var="qs" value="${qs}&q=${keyword}"/></c:if>
            <c:if test="${not empty sort}"><c:set var="qs" value="${qs}&sort=${sort}"/></c:if>

            <div class="pager">
                <c:if test="${page > 1}">
                    <a class="btn btn-ghost btn-sm"
                       href="${pageContext.request.contextPath}/story?${qs}&amp;page=${page-1}">← Trước</a>
                </c:if>
                <span class="pager-info">Trang ${page} / ${totalPages}</span>
                <c:if test="${page < totalPages}">
                    <a class="btn btn-ghost btn-sm"
                       href="${pageContext.request.contextPath}/story?${qs}&amp;page=${page+1}">Sau →</a>
                </c:if>
            </div>
        </c:if>
    </c:when>
    <c:otherwise>
        <div class="empty">
            <div class="empty-icon">🔍</div>
            <h3>Không tìm thấy truyện nào</h3>
            <p>Thử bỏ bớt bộ lọc, hoặc tìm bằng từ khoá khác.</p>
            <a class="btn btn-primary"
               href="${pageContext.request.contextPath}/story?action=list">Xem tất cả truyện</a>
        </div>
    </c:otherwise>
</c:choose>
