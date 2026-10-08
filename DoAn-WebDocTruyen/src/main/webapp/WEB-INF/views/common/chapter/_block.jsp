<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
================================================================================
  chapter/_block.jsp — MẢNH TÁI DÙNG: một chương truyện
================================================================================
  TẦNG: views/

  DÙNG Ở: chapter/read.jsp (chương đầu tiên, có khung)
          ChapterServlet action=raw (các chương nối thêm, KHÔNG khung)

  ĐÂY LÀ MẢNH DUY NHẤT ĐƯỢC TRẢ VỀ TRẦN, không qua layout nào.
    Vì vậy nó phải tự đủ: không được dựa vào biến nào mà chỉ layout mới đặt.

  data-* trên thẻ <article> là CẦU NỐI DUY NHẤT giữa JSP và JavaScript.
    JS đọc data-next-id để biết phải nạp chương nào tiếp, đọc data-url để đổi
    thanh địa chỉ khi cuộn tới. Nhét số vào giữa mã JS thì trộn hai tầng —
    JSP lo dữ liệu, JS lo hành vi, ranh giới nằm đúng ở đây.

  Nhận: chapter · story · next (có thể null ở chương cuối)
        markedParas (tuỳ chọn) — "3,17": các đoạn người đọc đã đánh dấu
================================================================================
--%>
<article class="chapter-block"
         data-chapter-id="${chapter.id}"
         data-chapter-no="${chapter.chapterNo}"
         data-story-id="${story.id}"
         data-story-title="<c:out value='${story.title}'/>"
         data-chapter-title="<c:out value='${chapter.title}'/>"
         data-next-id="${empty next ? '' : next.id}"
         data-marked="${markedParas}"
         data-title="Chương ${chapter.chapterNo} — <c:out value='${story.title}'/>"
         data-url="${pageContext.request.contextPath}/chapter?action=read&amp;id=${chapter.id}">

    <h1 class="chapter-h"><c:out value="${chapter.title}"/></h1>
    <p class="reader-sub">
        Chương ${chapter.chapterNo} &middot; <c:out value="${story.title}"/>
    </p>

    <c:choose>
        <c:when test="${isLocked}">
            <%@ include file="/WEB-INF/views/common/chapter/_locked.jsp" %>
        </c:when>
        <c:when test="${story.comic}">
            <%--
              TRUYỆN TRANH — mỗi trang một <figure>, cuộn dọc liên tục.

              data-para dùng LẠI đúng như đoạn văn: đánh dấu, "đọc tiếp đúng
              chỗ" và thanh tiến độ ở layout/reader.jsp chỉ tìm [data-para]
              trong .chapter-content, không cần biết đó là chữ hay ảnh.

              width/height in sẵn (khi biết) để trình duyệt GIỮ CHỖ đúng tỉ lệ
              trước khi ảnh tải xong — không thì trang nhảy liên tục và cuộn
              tới "trang 20" rơi sai chỗ. Ba trang đầu tải ngay (eager), còn
              lại để trình duyệt tải dần khi gần tới (lazy).

              Ảnh đi qua /uploads/pages/… — UploadedFileServlet kiểm quyền đọc
              chương trước khi phát, nên link ảnh chương VIP không chia sẻ được.
            --%>
            <div class="chapter-content comic-pages">
                <c:forEach var="pg" items="${chapter.pages}" varStatus="ps">
                    <figure class="comic-page" data-para="${ps.count}">
                        <img src="${pageContext.request.contextPath}${pg.url}"
                             alt="Trang ${ps.count}"
                             <c:if test="${pg.sized}">width="${pg.width}" height="${pg.height}"</c:if>
                             loading="${ps.count le 3 ? 'eager' : 'lazy'}" decoding="async">
                    </figure>
                </c:forEach>
                <c:if test="${empty chapter.pages}">
                    <p class="muted comic-empty">Chương này chưa có trang ảnh nào.</p>
                </c:if>
            </div>
        </c:when>
        <c:otherwise>
            <div class="chapter-content">
                <%--
                  Mỗi đoạn văn một thẻ <p>.
                  chapter.paragraphs do model cắt (xem ChapterParagraphTest).

                  data-para = số thứ tự đoạn, đếm từ 1. Đánh dấu đoạn và "đọc
                  tiếp đúng đoạn" đều dựa vào số này — server dùng CÙNG hàm
                  getParagraphs() để cắt đoạn trích, nên hai bên luôn khớp.
                --%>
                <c:forEach var="para" items="${chapter.paragraphs}" varStatus="ps">
                    <p data-para="${ps.count}"><c:out value="${para}"/></p>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>

    <%--
      Mốc để JavaScript biết "sắp đọc hết chương này".

      Đặt Ở CUỐI mỗi chương chứ không phải cuối trang: có thế mới nạp được
      chương kế tiếp ngay khi người đọc gần tới, thay vì đợi họ chạm đáy rồi
      mới bắt đầu tải và phải ngồi chờ.

      KHÔNG ĐƯỢC DÙNG hidden.
        Thẻ có hidden bị display:none, mà phần tử display:none có kích thước
        bằng 0 — IntersectionObserver KHÔNG BAO GIỜ báo nó lọt vào màn hình.
        Đọc hết chương mà chẳng có gì xảy ra, và không có lỗi nào trong console.

        Nên nó vẫn được vẽ, chỉ cao 1px và trong suốt (xem layout-reader.css).
        aria-hidden để trình đọc màn hình bỏ qua — nó không có nội dung gì.
    --%>
    <span class="chapter-end" aria-hidden="true"></span>
</article>
