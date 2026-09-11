<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
================================================================================
  _cover.jsp — MẢNH TÁI DÙNG: ảnh bìa truyện (hoặc chữ cái thay thế)
================================================================================
  TẦNG: views/

  DÙNG Ở: 7 chỗ — story/_card · story/detail · story/home (dải Đọc tiếp)
          story/rank · user/bookmarks · user/history · _partials/_story-row

  CÁCH DÙNG:
      <c:set var="cvUrl"     value="${story.coverUrl}"/>
      <c:set var="cvAlt"     value="${story.title}"/>
      <c:set var="cvInitial" value="${story.initial}"/>
      <%@ include file="/WEB-INF/views/_partials/_cover.jsp" %>

  VÌ SAO GOM LẠI
    Bảy chỗ đang chép đi chép lại cùng một khối <c:choose> "có ảnh thì vẽ ảnh,
    không thì vẽ chữ cái đầu". Chép bảy lần nghĩa là sửa cũng phải nhớ đủ bảy —
    và thực tế đã lệch: 3 chỗ có loading="lazy", 4 chỗ quên.

  ---- LỖI THẬT MÀ MẢNH NÀY VÁ ----------------------------------------------
  UploadUtil lưu đường dẫn dạng "/uploads/abc.png" — KHÔNG kèm context path,
  và như vậy là đúng: context path phụ thuộc nơi triển khai, không được đóng
  cứng vào dữ liệu trong CSDL.

  Nhưng các trang lại vẽ thẳng ${story.coverUrl} ra thuộc tính src. Ở máy phát
  triển, context path rỗng nên chạy tốt. Bung file .war lên Tomcat thật (ứng
  dụng nằm ở /webdoctruyen) thì MỌI ảnh tải lên đều 404 — đúng lúc đem đi
  bảo vệ. Ảnh dán link ngoài (https://…) vẫn hiện, nên nhìn qua tưởng chỉ
  hỏng vài tấm.

  Cách vá: đường dẫn bắt đầu bằng "/" là đường dẫn NỘI BỘ -> chắp context path
  vào. Bắt đầu bằng "http" là của web khác -> để nguyên.
================================================================================
--%>
<c:choose>
    <c:when test="${not empty cvUrl}">
        <%-- fn:startsWith phân biệt ảnh của mình với ảnh dán từ ngoài --%>
        <c:set var="cvSrc" value="${fn:startsWith(cvUrl, '/')
                                    ? pageContext.request.contextPath.concat(cvUrl)
                                    : cvUrl}"/>
        <%--
          alt lấy từ tiêu đề: cần cho trình đọc màn hình, và là thứ hiện ra khi
          ảnh lỗi. Vẫn phải escape vì tiêu đề do người dùng nhập.

          loading="lazy": kho truyện tải 24 bìa một lúc, trình duyệt chỉ tải
          tấm nào sắp lọt vào màn hình. Trước đây chỉ 3/7 chỗ có thuộc tính
          này — đúng kiểu lệch mà việc chép code sinh ra.
        --%>
        <img src="<c:out value='${cvSrc}'/>"
             alt="<c:out value='${cvAlt}'/>" loading="lazy">
    </c:when>
    <c:otherwise>
        <%--
          Không có bìa thì lấy chữ cái đầu. Chữ này do model tính
          (getInitial()), không tính trong JSP.

          Dùng <span> chứ không <div>: mảnh này được nhét vào cả bên trong thẻ
          <a> (ở _story-row, _card), mà <div> trong <a> tuy hợp lệ HTML5 nhưng
          <span> thì chắc chắn đúng ở mọi chỗ. CSS .cover-fallback đã đặt
          display:grid nên nhìn không khác gì.
        --%>
        <span class="cover-fallback">${cvInitial}</span>
    </c:otherwise>
</c:choose>
