<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
================================================================================
  chapter/read.jsp — MẢNH nội dung, dùng khung layout/reader.jsp     CASE 06
================================================================================
  Nhận: chapter · story · prev · next

  Đây là trang duy nhất dùng layout `reader` — bỏ hết nav và footer, chữ to,
  cột hẹp, font serif. Mọi thứ để đọc lâu không mỏi mắt.
================================================================================
--%>
<h1><c:out value="${chapter.title}"/></h1>
<p class="reader-sub">
    Chương ${chapter.chapterNo} &middot; <c:out value="${story.title}"/>
</p>

<div class="chapter-content">
    <%--
      GIỮ XUỐNG DÒNG CỦA TÁC GIẢ — và vẫn chống được XSS.

      Vấn đề: trong database nội dung là chữ thuần, có ký tự xuống dòng. Nhưng
      HTML gộp mọi khoảng trắng liên tiếp thành MỘT dấu cách — in thẳng ra thì
      cả chương thành một khối chữ dính liền, không có đoạn nào.

      Cách xử lý — THỨ TỰ HAI BƯỚC NÀY BẮT BUỘC:
        1. fn:escapeXml  đổi < > & thành &lt; &gt; &amp;   -> vô hiệu hoá thẻ
                         độc mà tác giả có thể chèn vào
        2. fn:replace    đổi ký tự xuống dòng thành thẻ <br>

      Làm ngược lại (replace trước, escape sau) thì chính thẻ <br> vừa thêm
      cũng bị escape thành chữ "&lt;br&gt;" hiện ra màn hình — và tệ hơn, thẻ
      độc của người dùng lại lọt qua.

      Chuỗi tìm kiếm ở fn:replace là một ký tự XUỐNG DÒNG THẬT nằm giữa hai
      dấu nháy, không phải chuỗi "\n" — EL không hiểu escape kiểu Java.
    --%>
    ${fn:replace(fn:escapeXml(chapter.content), '
', '<br>')}
</div>

<div class="reader-nav">
    <%-- prev/next là null ở chương đầu và chương cuối. Dùng span trống giữ chỗ
         để nút "Mục lục" luôn nằm chính giữa, không bị lệch. --%>
    <c:choose>
        <c:when test="${not empty prev}">
            <a class="btn btn-ghost"
               href="${pageContext.request.contextPath}/chapter?action=read&amp;id=${prev.id}">
                &larr; Chương ${prev.chapterNo}</a>
        </c:when>
        <c:otherwise><span class="spacer"></span></c:otherwise>
    </c:choose>

    <a class="btn btn-ghost"
       href="${pageContext.request.contextPath}/story?action=detail&amp;id=${story.id}">
        &#9776; Mục lục</a>

    <c:choose>
        <c:when test="${not empty next}">
            <a class="btn btn-primary"
               href="${pageContext.request.contextPath}/chapter?action=read&amp;id=${next.id}">
                Chương ${next.chapterNo} &rarr;</a>
        </c:when>
        <c:otherwise><span class="spacer"></span></c:otherwise>
    </c:choose>
</div>
