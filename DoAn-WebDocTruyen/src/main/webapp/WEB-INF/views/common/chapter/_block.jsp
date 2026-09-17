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
================================================================================
--%>
<article class="chapter-block"
         data-chapter-id="${chapter.id}"
         data-chapter-no="${chapter.chapterNo}"
         data-story-id="${story.id}"
         data-next-id="${empty next ? '' : next.id}"
         data-title="Chương ${chapter.chapterNo} — <c:out value='${story.title}'/>"
         data-url="${pageContext.request.contextPath}/chapter?action=read&amp;id=${chapter.id}">

    <h1 class="chapter-h"><c:out value="${chapter.title}"/></h1>
    <p class="reader-sub">
        Chương ${chapter.chapterNo} &middot; <c:out value="${story.title}"/>
    </p>

    <div class="chapter-content">
        <%--
          Mỗi đoạn văn một thẻ <p>.

          chapter.paragraphs do model cắt sẵn — xem ghi chú đầy đủ ở
          model/Chapter.getParagraphs(). Tóm tắt: cắt trong JSP bằng
          fn:replace hay c:forTokens đều hỏng vì chuyện CRLF và vì JSP
          không giải mã entity trong thuộc tính.

          c:out escape từng đoạn — nội dung chương là chữ tác giả nhập,
          bắt buộc phải escape.
        --%>
        <c:forEach var="para" items="${chapter.paragraphs}">
            <p><c:out value="${para}"/></p>
        </c:forEach>
    </div>

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
