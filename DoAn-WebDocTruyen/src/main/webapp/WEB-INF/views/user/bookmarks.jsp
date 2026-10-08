<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
  user/bookmarks.jsp — MẢNH nội dung. Tủ sách.                     CASE 08
  Nhận: bookmarks (List<Bookmark>) · marks (List<ChapterBookmark>)
        tab = "stories" | "marks"

  HAI THẺ LÀ HAI LINK (?tab=marks), KHÔNG PHẢI JavaScript.
    Mỗi thẻ có địa chỉ riêng nên gửi link / bấm Back / F5 đều đứng đúng thẻ,
    và trang đọc có thể dẫn thẳng về "Đoạn đã đánh dấu".
--%>
<div class="section-head">
    <h2>Tủ sách</h2>
</div>

<div class="tab-bar bookshelf-tabs">
    <a class="tab-btn ${tab ne 'marks' ? 'is-on' : ''}"
       href="${pageContext.request.contextPath}/bookmark?action=list">
        📚 Truyện đã lưu <span class="tab-count">${fn:length(bookmarks)}</span></a>
    <a class="tab-btn ${tab eq 'marks' ? 'is-on' : ''}"
       href="${pageContext.request.contextPath}/bookmark?action=list&amp;tab=marks">
        🔖 Đoạn đã đánh dấu <span class="tab-count">${fn:length(marks)}</span></a>
</div>

<c:choose>
<%-- =========================================================== ĐOẠN ĐÃ ĐÁNH DẤU --%>
<c:when test="${tab eq 'marks'}">
    <c:choose>
        <c:when test="${not empty marks}">
            <div class="bookmark-list">
                <c:forEach var="m" items="${marks}">
                    <div class="bookmark-item mark-item">
                        <div class="bm-cover">
                            <c:set var="cvUrl"     value="${m.coverUrl}"/>
                            <c:set var="cvAlt"     value="${m.storyTitle}"/>
                            <c:set var="cvInitial" value="${m.initial}"/>
                            <%@ include file="/WEB-INF/views/_partials/_cover.jsp" %>
                        </div>

                        <div class="bm-info">
                            <a class="bm-title"
                               href="${pageContext.request.contextPath}/story?action=detail&amp;id=${m.storyId}">
                                <c:out value="${m.storyTitle}"/></a>
                            <p class="bm-progress">
                                Chương ${m.chapterNo} · <c:out value="${m.chapterTitle}"/>
                                <c:if test="${m.paraNo gt 0}"> · ${m.unitLabel} ${m.paraNo}</c:if>
                            </p>
                            <%-- Truyện tranh: ảnh thu nhỏ của đúng trang đánh dấu (ảnh đi qua
                                 UploadedFileServlet nên vẫn kiểm quyền đọc chương). --%>
                            <c:if test="${m.comic and not empty m.pageUrl}">
                                <img class="mark-thumb" src="${pageContext.request.contextPath}${m.pageUrl}"
                                     alt="Trang ${m.paraNo}" loading="lazy">
                            </c:if>
                            <c:if test="${not empty m.snippet}">
                                <blockquote class="mark-snippet"><c:out value="${m.snippet}"/></blockquote>
                            </c:if>
                        </div>

                        <div class="bm-actions">
                            <%-- #doan-N: trang đọc cuộn thẳng tới đúng đoạn (xem layout/reader.jsp) --%>
                            <a class="btn btn-primary btn-sm"
                               href="${pageContext.request.contextPath}/chapter?action=read&amp;id=${m.chapterId}${m.paraNo gt 0 ? '#doan-' : ''}${m.paraNo gt 0 ? m.paraNo : ''}">
                                Đọc ${m.unitLabel} này</a>
                            <form action="${pageContext.request.contextPath}/bookmark" method="post"
                                  style="display:inline">
                                <input type="hidden" name="_csrf" value="${csrfToken}">
                                <input type="hidden" name="action" value="unmark">
                                <input type="hidden" name="id" value="${m.id}">
                                <button type="submit" class="btn btn-ghost btn-sm">Bỏ đánh dấu</button>
                            </form>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:when>
        <c:otherwise>
            <c:set var="emIcon"  value="🔖"/>
            <c:set var="emTitle" value="Chưa đánh dấu đoạn nào"/>
            <c:set var="emText"
                   value="Đang đọc gặp đoạn (hoặc trang truyện tranh) hay? Bấm 🔖 trên thanh công cụ (hoặc phím B) để đánh dấu chỗ đang ở đầu màn hình, rồi quay lại đây đọc lại bất cứ lúc nào."/>
            <c:set var="emUrl"   value="/history"/>
            <c:set var="emBtn"   value="Mở lịch sử đọc"/>
            <%@ include file="/WEB-INF/views/_partials/_empty.jsp" %>
        </c:otherwise>
    </c:choose>
</c:when>

<%-- =========================================================== TRUYỆN ĐÃ LƯU --%>
<c:otherwise>
    <c:choose>
        <c:when test="${not empty bookmarks}">
            <div class="bookmark-list">
                <c:forEach var="b" items="${bookmarks}">
                    <div class="bookmark-item">
                        <div class="bm-cover">
                            <c:set var="cvUrl"     value="${b.coverUrl}"/>
                            <c:set var="cvAlt"     value="${b.storyTitle}"/>
                            <c:set var="cvInitial" value="${b.initial}"/>
                            <%@ include file="/WEB-INF/views/_partials/_cover.jsp" %>
                        </div>

                        <div class="bm-info">
                            <a class="bm-title"
                               href="${pageContext.request.contextPath}/story?action=detail&amp;id=${b.storyId}">
                                <c:out value="${b.storyTitle}"/></a>

                            <p class="bm-progress">
                                <%-- isStarted() trong model: lastChapterId > 0 (lấy từ reading_progress) --%>
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
                                    <%-- Trang đọc tự cuộn tới đúng đoạn đang dở --%>
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
                                <input type="hidden" name="_csrf" value="${csrfToken}">
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
            <c:set var="emIcon"  value="📚"/>
            <c:set var="emTitle" value="Chưa lưu truyện nào"/>
            <c:set var="emText"
                   value="Bấm ☆ Lưu truyện ở trang truyện (hoặc 📚 khi đang đọc) để cất vào tủ sách. Vị trí đọc thì hệ thống tự nhớ, không cần lưu."/>
            <c:set var="emUrl"   value="/story?action=list"/>
            <c:set var="emBtn"   value="Tìm truyện để đọc"/>
            <%@ include file="/WEB-INF/views/_partials/_empty.jsp" %>
        </c:otherwise>
    </c:choose>
</c:otherwise>
</c:choose>
