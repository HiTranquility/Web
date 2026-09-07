<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
================================================================================
  story/detail.jsp — MẢNH NỘI DUNG: Chi tiết truyện               TRANG 3
================================================================================
  TẦNG: views/

  Nhận: story · tags · chapters · comments · canEdit
        bookmarked · myRating · following

  Trang đông đặc nhất dự án: mô tả, mục lục, chấm sao, theo dõi tác giả, lưu
  truyện, tải .txt, chia sẻ, báo cáo, bình luận. Vì vậy nó dùng lại nhiều mảnh
  nhất — _rating-stars, _chapter-list, _comment, _empty.
================================================================================
--%>
<div class="story-detail">
    <div class="detail-cover">
        <c:choose>
            <c:when test="${not empty story.coverUrl}">
                <img src="<c:out value='${story.coverUrl}'/>"
                     alt="<c:out value='${story.title}'/>">
            </c:when>
            <c:otherwise>
                <div class="cover-fallback">${story.initial}</div>
            </c:otherwise>
        </c:choose>
    </div>

    <div class="detail-info">
        <h1><c:out value="${story.title}"/></h1>

        <p class="detail-author">
            ✍️
            <%-- Tên tác giả là LINK sang hồ sơ (trang 4). Trước đây chỉ là
                 chữ — đọc xong một truyện hay mà không có đường tìm truyện
                 khác của cùng người viết là bỏ phí. --%>
            <a href="${pageContext.request.contextPath}/user?action=profile&amp;id=${story.authorId}">
                <c:out value="${story.authorName}"/></a>

            <c:if test="${story.status eq 'DRAFT'}">
                <span class="pill pill-warn" style="margin-left:8px">Bản nháp</span>
            </c:if>

            <%-- Nút theo dõi tác giả. Không hiện trên hồ sơ của chính mình. --%>
            <c:if test="${not empty currentUser and currentUser.id ne story.authorId}">
                <form method="post" style="display:inline;margin-left:10px"
                      action="${pageContext.request.contextPath}/follow">
                    <input type="hidden" name="do" value="${following ? 'unfollow' : 'follow'}">
                    <input type="hidden" name="authorId" value="${story.authorId}">
                    <button type="submit" class="btn btn-ghost btn-sm">
                        ${following ? '✓ Đang theo dõi' : '+ Theo dõi'}
                    </button>
                </form>
            </c:if>
        </p>

        <div class="tag-row" style="margin:14px 0">
            <c:forEach var="t" items="${tags}">
                <a class="tag"
                   href="${pageContext.request.contextPath}/story?action=list&amp;tag=${t.slug}">
                    <c:out value="${t.name}"/></a>
            </c:forEach>
        </div>

        <div class="detail-stats">
            <span>📄 ${story.chapterCount} chương</span>
            <span>👁️ <fmt:formatNumber pattern="#,##0" value="${story.viewCount}"/> lượt xem</span>
            <span class="${story.completed ? 'yes' : ''}">
                ${story.completed ? '✓ Hoàn thành' : '⏳ Đang ra'}</span>
        </div>

        <%-- MẢNH: sao đánh giá, bản CÓ chấm được (rsForm) --%>
        <c:set var="rsStory" value="${story}"/>
        <c:set var="rsForm"  value="${true}"/>
        <c:set var="rsMine"  value="${myRating}"/>
        <%@ include file="/WEB-INF/views/_partials/_rating-stars.jsp" %>

        <p class="detail-desc"><c:out value="${story.description}"/></p>

        <div class="detail-actions">
            <c:if test="${not empty chapters}">
                <a class="btn btn-primary"
                   href="${pageContext.request.contextPath}/chapter?action=read&amp;id=${chapters[0].id}">
                    Đọc từ đầu</a>
            </c:if>

            <%-- Nút lưu/bỏ lưu. Chỉ hiện khi đã đăng nhập.
                 Dùng <form method="post"> vì đây là hành động GHI. --%>
            <c:if test="${not empty currentUser}">
                <form action="${pageContext.request.contextPath}/bookmark" method="post"
                      style="display:inline">
                    <input type="hidden" name="action" value="${bookmarked ? 'remove' : 'add'}">
                    <input type="hidden" name="storyId" value="${story.id}">
                    <button type="submit" class="btn btn-ghost">
                        ${bookmarked ? '★ Đã lưu' : '☆ Lưu truyện'}
                    </button>
                </form>
            </c:if>

            <a class="btn btn-ghost"
               href="${pageContext.request.contextPath}/download?storyId=${story.id}">
                ⬇ Tải .txt</a>

            <%-- canEdit do servlet tính: chủ truyện HOẶC admin --%>
            <c:if test="${canEdit}">
                <a class="btn btn-ghost"
                   href="${pageContext.request.contextPath}/story?action=edit&amp;id=${story.id}">Sửa</a>
                <a class="btn btn-ghost"
                   href="${pageContext.request.contextPath}/chapter?action=create&amp;storyId=${story.id}">+ Thêm chương</a>
            </c:if>
        </div>

        <%--
          CHIA SẺ — ba mạng phổ biến, dựng bằng link thuần.

          Không nhúng nút chính chủ của Facebook/X. Nút của họ kéo theo mã
          JavaScript của bên thứ ba và theo dõi mọi người mở trang, kể cả
          người không bấm chia sẻ. Một thẻ <a> tới URL chia sẻ của họ làm đúng
          việc cần làm mà không gửi gì đi trước khi người dùng chủ động bấm.

          rel="noopener" bắt buộc khi có target="_blank": thiếu nó, trang mới
          mở ra có thể điều khiển ngược trang này qua window.opener.
        --%>
        <div class="share-row">
            <span class="share-label">Chia sẻ:</span>
            <c:set var="shareUrl"
                   value="${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}${pageContext.request.contextPath}/story?action=detail&id=${story.id}"/>
            <a class="share-btn" target="_blank" rel="noopener"
               href="https://www.facebook.com/sharer/sharer.php?u=${fn:escapeXml(shareUrl)}">Facebook</a>
            <a class="share-btn" target="_blank" rel="noopener"
               href="https://twitter.com/intent/tweet?url=${fn:escapeXml(shareUrl)}&amp;text=${fn:escapeXml(story.title)}">X</a>
            <a class="share-btn" target="_blank" rel="noopener"
               href="https://t.me/share/url?url=${fn:escapeXml(shareUrl)}">Telegram</a>

            <%-- Báo cáo truyện vi phạm. Form gọn nằm ngay đây, không bắt
                 chuyển trang — bắt đổi trang để tố cáo là cách chắc chắn
                 khiến không ai buồn báo cáo. --%>
            <c:if test="${not empty currentUser}">
                <details class="report-box">
                    <summary class="share-btn">⚠ Báo cáo</summary>
                    <form method="post" action="${pageContext.request.contextPath}/report">
                        <input type="hidden" name="targetType" value="STORY">
                        <input type="hidden" name="targetId" value="${story.id}">
                        <input type="hidden" name="storyId" value="${story.id}">
                        <input type="text" name="reason" maxlength="500"
                               class="inline-input"
                               placeholder="Lý do: spam, nội dung cấm, đạo văn…">
                        <button type="submit" class="btn btn-danger btn-sm">Gửi</button>
                    </form>
                </details>
            </c:if>
        </div>
    </div>
</div>

<%-- ---- Mục lục chương — MẢNH _chapter-list ---- --%>
<div class="section-head">
    <h2>Danh sách chương</h2>
    <span class="more">${story.chapterCount} chương</span>
</div>

<c:set var="clChapters" value="${chapters}"/>
<c:set var="clStoryId"  value="${story.id}"/>
<c:set var="clCanEdit"  value="${canEdit}"/>
<%@ include file="/WEB-INF/views/_partials/_chapter-list.jsp" %>

<%-- ---- Bình luận ---- --%>
<div class="section-head" id="comments">
    <h2>Bình luận (${fn:length(comments)})</h2>
</div>

<c:choose>
    <c:when test="${not empty currentUser}">
        <form action="${pageContext.request.contextPath}/comment" method="post"
              class="comment-form">
            <input type="hidden" name="action" value="add">
            <input type="hidden" name="storyId" value="${story.id}">
            <textarea name="content" rows="3" maxlength="1000" required
                      placeholder="Viết bình luận… Nhớ giữ lời lẽ văn minh."></textarea>
            <button type="submit" class="btn btn-primary btn-sm">Gửi bình luận</button>
        </form>
    </c:when>
    <c:otherwise>
        <p class="muted-note">
            <a href="${pageContext.request.contextPath}/auth?action=login">Đăng nhập</a>
            để bình luận.
        </p>
    </c:otherwise>
</c:choose>

<%-- MẢNH: mỗi bình luận. cmStoryId để nút gỡ biết quay về truyện nào. --%>
<c:set var="cmStoryId" value="${story.id}" scope="request"/>
<c:forEach var="cm" items="${comments}">
    <%@ include file="/WEB-INF/views/_partials/_comment.jsp" %>
</c:forEach>

<c:if test="${empty comments}">
    <c:set var="emIcon"  value="💬"/>
    <c:set var="emTitle" value="Chưa có bình luận nào"/>
    <c:set var="emText"  value="Hãy là người đầu tiên chia sẻ cảm nhận về truyện này."/>
    <%@ include file="/WEB-INF/views/_partials/_empty.jsp" %>
</c:if>

<%--
  ---- Gợi ý truyện tương tự ----

  Đặt SAU phần bình luận, không đặt trên.

  Người đã cuộn tới đây là người đã đọc xong mô tả và xem hết mục lục — họ
  đang ở đúng thời điểm cần câu hỏi "đọc gì tiếp". Đặt khối gợi ý ngay dưới
  mô tả thì nó tranh chỗ với chính truyện mà người ta vừa bấm vào.

  Cách đo "tương tự" là đếm số thể loại trùng nhau — xem ghi chú trong
  StoryDAO.findSimilar(). Không có truyện nào trùng thì khối này biến mất
  hẳn, không hiện ô rỗng.
--%>
<c:if test="${not empty similar}">
    <div class="section-head" style="margin-top:44px">
        <h2>Có thể bạn cũng thích</h2>
        <span class="more">cùng thể loại</span>
    </div>

    <div class="story-grid">
        <c:forEach var="story" items="${similar}">
            <%@ include file="/WEB-INF/views/story/_card.jsp" %>
        </c:forEach>
    </div>
</c:if>

<%--
  ĐÁNH DẤU CHƯƠNG ĐÃ ĐỌC.

  Danh sách chương đã đọc do trang đọc ghi vào localStorage (xem
  layout/reader.jsp). Ở đây chỉ đọc lại và làm mờ những hàng tương ứng.

  Chạy được cho cả khách chưa đăng nhập — đó là ưu điểm so với lưu vào CSDL,
  và cũng là hạn chế: đổi máy là mất. Vị trí đọc "chính thức" vẫn nằm ở bảng
  bookmarks cho người đã đăng nhập.
--%>
<script>
(function () {
    try {
        var done = JSON.parse(localStorage.getItem('read.${story.id}') || '[]');
        if (!done.length) return;
        document.querySelectorAll('.chapter-item').forEach(function (row) {
            var a = row.querySelector('.chapter-link');
            if (!a) return;
            var m = a.getAttribute('href').match(/id=(\d+)/);
            if (m && done.indexOf(m[1]) !== -1) {
                row.classList.add('chapter-read');
            }
        });
    } catch (e) { /* trình duyệt chặn lưu trữ — mục lục vẫn dùng bình thường */ }
})();
</script>
