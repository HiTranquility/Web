<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--
================================================================================
  chapter/read.jsp — MẢNH nội dung, dùng khung layout/reader.jsp   TRANG 23
================================================================================
  TẦNG: views/
  Nhận: chapter · story · prev · next
================================================================================
--%>

<%-- Nơi JavaScript nối thêm chương. Chương đầu server dựng sẵn từ _block.jsp. --%>
<div id="chapters">
    <%@ include file="/WEB-INF/views/common/chapter/_block.jsp" %>
</div>

<%-- Khối Thả Cảm Xúc Nhanh Cuối Chương (Chapter Quick Reactions) --%>
<div class="chapter-reactions-card" id="chapter-reactions-box" data-chapter-id="${chapter.id}">
    <div class="reaction-title">
        <span>✨ Cảm nhận của bạn về chương này?</span>
    </div>
    <div class="reaction-buttons-grid">
        <button type="button" class="reaction-btn" data-reaction="love" title="Tuyệt vời! Yêu thích">
            <span class="rx-icon">❤️</span>
            <span class="rx-label">Yêu thích</span>
            <span class="rx-count">12</span>
        </button>
        <button type="button" class="reaction-btn" data-reaction="fire" title="Quá đỉnh cao! Hóng chương sau">
            <span class="rx-icon">🔥</span>
            <span class="rx-label">Đỉnh cao</span>
            <span class="rx-count">28</span>
        </button>
        <button type="button" class="reaction-btn" data-reaction="sad" title="Cảm động / Rơi nước mắt">
            <span class="rx-icon">😭</span>
            <span class="rx-label">Cảm động</span>
            <span class="rx-count">5</span>
        </button>
        <button type="button" class="reaction-btn" data-reaction="laugh" title="Hài hước / Cười vỡ bụng">
            <span class="rx-icon">😂</span>
            <span class="rx-label">Hài hước</span>
            <span class="rx-count">16</span>
        </button>
        <button type="button" class="reaction-btn" data-reaction="shock" title="Bất ngờ / Sốc toàn tập">
            <span class="rx-icon">🤯</span>
            <span class="rx-label">Bất ngờ</span>
            <span class="rx-count">9</span>
        </button>
    </div>
</div>

<script>
(function() {
    var rxBox = document.getElementById('chapter-reactions-box');
    if (!rxBox) return;
    var chapterId = rxBox.getAttribute('data-chapter-id');
    var storageKey = 'truyen_reaction_' + chapterId;
    var savedRx = localStorage.getItem(storageKey);

    var btns = rxBox.querySelectorAll('.reaction-btn');
    if (savedRx) {
        btns.forEach(function(b) {
            if (b.getAttribute('data-reaction') === savedRx) {
                b.classList.add('is-reacted');
            }
        });
    }

    btns.forEach(function(btn) {
        btn.addEventListener('click', function() {
            var rxType = this.getAttribute('data-reaction');
            var isCurrent = this.classList.contains('is-reacted');
            var countEl = this.querySelector('.rx-count');
            var count = parseInt(countEl ? countEl.textContent : '0', 10);

            if (isCurrent) {
                this.classList.remove('is-reacted');
                if (countEl && count > 0) countEl.textContent = (count - 1);
                localStorage.removeItem(storageKey);
            } else {
                btns.forEach(function(b) {
                    if (b.classList.contains('is-reacted')) {
                        b.classList.remove('is-reacted');
                        var c = b.querySelector('.rx-count');
                        var val = parseInt(c ? c.textContent : '0', 10);
                        if (c && val > 0) c.textContent = (val - 1);
                    }
                });
                this.classList.add('is-reacted');
                if (countEl) countEl.textContent = (count + 1);
                localStorage.setItem(storageKey, rxType);
            }
        });
    });
})();
</script>

<%-- Vòng quay báo đang tải chương sau. Ẩn cho tới khi JS bật lên. --%>
<div id="loading" class="chapter-loading" hidden style="display:none">
    <span class="spinner" aria-hidden="true"></span> Đang tải chương tiếp…
</div>

<%-- ================================================================================
     TRẠM KẾT THÚC CHƯƠNG THÔNG MINH (End-of-Chapter Smart Hub)
     ================================================================================ --%>
<div class="smart-chapter-hub" id="smart-chapter-hub">
    <div class="hub-progress-track">
        <c:set var="totChaps" value="${story.chapterCount gt 0 ? story.chapterCount : 1}"/>
        <c:set var="pctStory" value="${(chapter.chapterNo * 100.0) / totChaps}"/>
        <c:if test="${pctStory gt 100}"><c:set var="pctStory" value="100"/></c:if>
        <div class="hub-meta-info">
            <span class="hub-prog-text">
                📚 Tiến độ truyện: <strong>Chương ${chapter.chapterNo} / ${story.chapterCount}</strong>
                (<fmt:formatNumber value="${pctStory}" maxFractionDigits="0"/>%)
            </span>
            <span class="hub-key-hint">Phím tắt: &larr; / &rarr; để chuyển chương</span>
        </div>
        <div class="hub-prog-bar" role="progressbar" aria-valuenow="${pctStory}" aria-valuemin="0" aria-valuemax="100">
            <div class="hub-prog-fill" style="width: ${pctStory}%;"></div>
        </div>
    </div>

    <div class="reader-nav" id="reader-nav">
        <c:choose>
            <c:when test="${not empty prev}">
                <a class="btn btn-ghost hub-nav-btn" id="nav-prev"
                   href="${pageContext.request.contextPath}/chapter?action=read&amp;id=${prev.id}"
                   title="Chương trước (Phím tắt: Mũi tên trái &larr;)">
                    &larr; Chương ${prev.chapterNo}</a>
            </c:when>
            <c:otherwise><span class="spacer" id="nav-prev"></span></c:otherwise>
        </c:choose>

        <details class="toc-box" id="toc-box" data-story-id="${story.id}"
                 data-current-id="${chapter.id}">
            <summary class="btn btn-ghost hub-toc-summary">&#9776; Mục lục (Phím T)</summary>

            <div class="toc-panel" id="toc-panel">
                <div class="toc-head">
                    <b><c:out value="${story.title}"/></b>
                    <a href="${pageContext.request.contextPath}/story?action=detail&amp;id=${story.id}">
                        Trang truyện →</a>
                </div>

                <div class="toc-body" id="toc-body">
                    <p class="muted" style="padding:14px">
                        Đang tải mục lục…
                        <a href="${pageContext.request.contextPath}/story?action=detail&amp;id=${story.id}#muc-luc">
                            Xem mục lục đầy đủ →</a>
                    </p>
                </div>
            </div>
        </details>

        <c:choose>
            <c:when test="${not empty next}">
                <a class="btn btn-primary hub-nav-btn hub-btn-next" id="nav-next"
                   href="${pageContext.request.contextPath}/chapter?action=read&amp;id=${next.id}"
                   title="Chương sau (Phím tắt: Mũi tên phải &rarr;)">
                    Chương ${next.chapterNo} &rarr;</a>
            </c:when>
            <c:otherwise><span class="hub-done-badge" id="nav-next">🎉 Đã đến chương mới nhất</span></c:otherwise>
        </c:choose>
    </div>
</div>

<%-- ================================================================================
     BÌNH LUẬN THEO TỪNG CHƯƠNG (ISSUE-004)
     ================================================================================ --%>
<div class="chapter-comments-wrap" id="comments" style="margin-top: 48px; border-top: 1px solid rgba(128,128,128,0.2); padding-top: 24px;">
    <div class="section-head" style="margin-bottom: 20px;">
        <h3 style="font-size: 1.25rem; font-weight: 600;">
            💬 Thảo luận Chương ${chapter.chapterNo}
            <c:if test="${chapterCommentCount gt 0}">
                <span class="more" style="font-weight: normal; font-size: 0.9rem; opacity: 0.8;">(${chapterCommentCount} bình luận)</span>
            </c:if>
        </h3>
    </div>

    <c:choose>
        <c:when test="${not empty currentUser}">
            <form action="${pageContext.request.contextPath}/comment" method="post"
                  class="comment-form" style="margin-bottom: 28px;">
                <input type="hidden" name="_csrf" value="${csrfToken}">
                <input type="hidden" name="action" value="add">
                <input type="hidden" name="storyId" value="${story.id}">
                <input type="hidden" name="chapterId" value="${chapter.id}">
                <input type="hidden" name="g-recaptcha-token" class="recaptcha-token">
                <textarea name="content" rows="3" maxlength="1000" required
                          placeholder="Bạn nghĩ gì về diễn biến chương này? Chia sẻ cảm nhận nào…"></textarea>
                <button type="submit" class="btn btn-primary btn-sm" style="margin-top: 8px;">Gửi bình luận chương</button>
            </form>
        </c:when>
        <c:otherwise>
            <p class="muted-note" style="margin-bottom: 24px; padding: 12px 16px; background: rgba(128,128,128,0.08); border-radius: 6px;">
                <a href="${pageContext.request.contextPath}/auth?action=login">Đăng nhập</a> để tham gia thảo luận chương này cùng các độc giả khác.
            </p>
        </c:otherwise>
    </c:choose>

    <%-- Danh sách bình luận chương --%>
    <c:set var="cmStoryId" value="${story.id}" scope="request"/>
    <c:set var="cmChapterId" value="${chapter.id}" scope="request"/>
    <c:forEach var="cm" items="${chapterComments}">
        <%@ include file="/WEB-INF/views/_partials/_comment.jsp" %>
    </c:forEach>

    <c:if test="${empty chapterComments}">
        <p class="muted" style="text-align: center; padding: 24px 0; font-style: italic; opacity: 0.7;">
            Chưa có bình luận nào cho chương này. Hãy là người đầu tiên để lại cảm nghĩ!
        </p>
    </c:if>
</div>

<%-- Hiện khi đã nối tới chương cuối cùng --%>
<p class="chapter-done" id="chapter-done" hidden style="display:none">
    Hết truyện. Cảm ơn bạn đã đọc tới đây.
</p>

<%-- Khối gợi ý cuối truyện --%>
<div class="reader-recs" id="reader-recs" data-story-id="${story.id}"></div>
