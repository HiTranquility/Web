<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
        <div class="empty">
            <div class="empty-icon">🔖</div>
            <h3>Chưa lưu truyện nào</h3>
            <p>Bấm <b>☆ Lưu truyện</b> ở trang truyện để lưu lại đọc sau.
               Hệ thống cũng tự nhớ bạn đang đọc tới chương mấy.</p>
            <a class="btn btn-primary"
               href="${pageContext.request.contextPath}/story?action=list">Tìm truyện để đọc</a>
        </div>
    </c:otherwise>
</c:choose>
