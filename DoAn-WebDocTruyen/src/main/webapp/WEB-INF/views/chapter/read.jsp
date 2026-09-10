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

<%-- Nơi JavaScript nối thêm chương. Chương đầu server dựng sẵn. --%>
<div id="chapters">
    <%@ include file="/WEB-INF/views/chapter/_block.jsp" %>
</div>

<%-- Vòng quay báo đang tải chương sau. Ẩn cho tới khi JS bật lên. --%>
<div id="loading" class="chapter-loading" hidden>
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

<%-- Hiện khi đã nối tới chương cuối cùng --%>
<p class="chapter-done" id="chapter-done" hidden>
    Hết truyện. Cảm ơn bạn đã đọc tới đây.
</p>

<%--
  Ghi chú về "tự động ghi nhớ vị trí đọc".

  Không có nút "Lưu vị trí" — cố ý. ChapterServlet ghi ngay khi trang được mở,
  và action=raw cũng ghi mỗi lần nối thêm một chương. Người đọc không phải
  bấm gì cả.
--%>
