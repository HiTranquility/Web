<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
  user/bookmarks.jsp — MẢNH nội dung. Truyện đã lưu.               CASE 08
  Nhận: bookmarks (List<Bookmark>)
--%>
<div class="section-head">
    <h2>Truyện đã lưu</h2>
    <span class="more">${fn:length(bookmarks)} truyện</span>
</div>

<c:choose>
    <c:when test="${not empty bookmarks}">
        <div class="bookmark-list">
            <c:forEach var="b" items="${bookmarks}">
                <div class="bookmark-item">
                    <div class="bm-cover">
                        <c:choose>
                            <c:when test="${not empty b.coverUrl}">
                                <img src="<c:out value='${b.coverUrl}'/>"
                                     alt="<c:out value='${b.storyTitle}'/>">
                            </c:when>
                            <c:otherwise>
                                <div class="cover-fallback">${b.initial}</div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div class="bm-info">
                        <a class="bm-title"
                           href="${pageContext.request.contextPath}/story?action=detail&amp;id=${b.storyId}">
                            <c:out value="${b.storyTitle}"/></a>

                        <p class="bm-progress">
                            <%-- isStarted() trong model: lastChapterId > 0 --%>
                            <c:choose>
                                <c:when test="${b.started}">
                                    Đang đọc chương ${b.lastChapterNo} / ${b.totalChapters}
                                </c:when>
                                <c:otherwise>
                                    Chưa đọc &middot; ${b.totalChapters} chương
                                </c:otherwise>
                            </c:choose>
                        </p>
                    </div>

                    <div class="bm-actions">
                        <c:choose>
                            <c:when test="${b.started}">
                                <a class="btn btn-primary btn-sm"
                                   href="${pageContext.request.contextPath}/chapter?action=read&amp;id=${b.lastChapterId}">
                                    Đọc tiếp</a>
                            </c:when>
                            <c:otherwise>
                                <a class="btn btn-primary btn-sm"
                                   href="${pageContext.request.contextPath}/story?action=detail&amp;id=${b.storyId}">
                                    Bắt đầu đọc</a>
                            </c:otherwise>
                        </c:choose>

                        <%-- from=list để servlet biết quay về đây, không quay về
                             trang truyện — chi tiết nhỏ nhưng đỡ khó chịu. --%>
                        <form action="${pageContext.request.contextPath}/bookmark" method="post"
                              style="display:inline">
                            <input type="hidden" name="action" value="remove">
                            <input type="hidden" name="storyId" value="${b.storyId}">
                            <input type="hidden" name="from" value="list">
                            <button type="submit" class="btn btn-ghost btn-sm">Bỏ lưu</button>
                        </form>
                    </div>
                </div>
            </c:forEach>
        </div>
    </c:when>
    <c:otherwise>
        <%-- MẢNH: trạng thái rỗng. Trước đây khối này viết thẳng tại chỗ —
             đã thay bằng mảnh chung để sáu trang danh sách trông giống nhau. --%>
        <c:set var="emIcon"  value="🔖"/>
        <c:set var="emTitle" value="Chưa lưu truyện nào"/>
        <c:set var="emText"
               value="Bấm ☆ Lưu truyện ở trang truyện để đọc sau. Hệ thống tự nhớ bạn đang đọc tới chương mấy."/>
        <c:set var="emUrl"   value="/story?action=list"/>
        <c:set var="emBtn"   value="Tìm truyện để đọc"/>
        <%@ include file="/WEB-INF/views/_partials/_empty.jsp" %>
    </c:otherwise>
</c:choose>
