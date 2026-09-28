<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
================================================================================
  chapter/read.jsp — MẢNH nội dung, dùng khung layout/reader.jsp   TRANG 23
================================================================================
  TẦNG: views/

  Nhận: chapter · story · prev · next

  Trang duy nhất dùng layout `reader` — bỏ hết nav và footer, chữ to, cột hẹp,
  font serif. Mọi thứ để đọc lâu không mỏi mắt.

  CHƯƠNG ĐẦU DÙNG CHUNG MẢNH _block.jsp VỚI CÁC CHƯƠNG NỐI THÊM.
    Nhờ vậy JavaScript đối xử với mọi chương như nhau — cùng một lớp CSS,
    cùng bộ data-*. Nếu chương đầu vẽ một kiểu còn chương nối vẽ kiểu khác
    thì mọi thứ động vào cả hai đều phải viết hai lần.
================================================================================
--%>

<%-- Nơi JavaScript nối thêm chương. Chương đầu server dựng sẵn từ _block.jsp. --%>
<div id="chapters">
    <%@ include file="/WEB-INF/views/common/chapter/_block.jsp" %>
</div>

<%-- Vòng quay báo đang tải chương sau. Ẩn cho tới khi JS bật lên. --%>
<div id="loading" class="chapter-loading" hidden style="display:none">
    <span class="spinner" aria-hidden="true"></span> Đang tải chương tiếp…
</div>

<%--
  ---- Điều hướng chương ----

  GIỮ NGUYÊN dù đã có đọc liên tục.

  Đọc liên tục bắt buộc phải có JavaScript — không thể vừa nối nội dung mới
  vừa giữ nguyên chỗ mắt đang đọc bằng HTML thuần. Bỏ ba nút này đi thì người
  tắt JavaScript đọc được đúng một chương rồi hết đường.

  Giữ cả hai thì tắt JS là rơi về đúng cách cũ, không mất gì.
  JavaScript sẽ tự cập nhật href của hai nút mỗi khi nối thêm chương.
--%>
<div class="reader-nav" id="reader-nav">
    <c:choose>
        <c:when test="${not empty prev}">
            <a class="btn btn-ghost" id="nav-prev"
               href="${pageContext.request.contextPath}/chapter?action=read&amp;id=${prev.id}">
                &larr; Chương ${prev.chapterNo}</a>
        </c:when>
        <c:otherwise><span class="spacer" id="nav-prev"></span></c:otherwise>
    </c:choose>

    <%--
      MỤC LỤC — bảng thả xuống NGAY TẠI CHỖ, không rời trang.

      Trước đây đây là một link sang trang chi tiết truyện. Đang đọc chương 40
      mà muốn xem chương 12 thì bị văng khỏi trang đọc: mất vị trí cuộn, mất
      luôn mạch đọc liên tục, và phải bấm quay lại nếu đổi ý.

      <details> làm phần đóng/mở mà không cần JavaScript. JavaScript chỉ lo
      MỘT việc: nạp danh sách chương lần đầu mở (xem reader.jsp). Tách vai như
      vậy nên khi tắt JavaScript, thẻ vẫn mở ra được và bên trong là link dự
      phòng sang trang chi tiết — không bấm vào khoảng trống.
    --%>
    <details class="toc-box" id="toc-box" data-story-id="${story.id}"
             data-current-id="${chapter.id}">
        <summary class="btn btn-ghost">&#9776; Mục lục</summary>

        <div class="toc-panel" id="toc-panel">
            <div class="toc-head">
                <b><c:out value="${story.title}"/></b>
                <a href="${pageContext.request.contextPath}/story?action=detail&amp;id=${story.id}">
                    Trang truyện →</a>
            </div>

            <%-- Chỗ này bị JavaScript thay bằng danh sách chương. Không có
                 JavaScript thì nó ở lại, và vẫn là một đường đi được. --%>
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
            <a class="btn btn-primary" id="nav-next"
               href="${pageContext.request.contextPath}/chapter?action=read&amp;id=${next.id}">
                Chương ${next.chapterNo} &rarr;</a>
        </c:when>
        <c:otherwise><span class="spacer" id="nav-next"></span></c:otherwise>
    </c:choose>
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

<%--
  Ghi chú về "tự động ghi nhớ vị trí đọc".

  Không có nút "Lưu vị trí" — cố ý. ChapterServlet ghi ngay khi trang được mở,
  và action=raw cũng ghi mỗi lần nối thêm một chương. Người đọc không phải
  bấm gì cả.
--%>
