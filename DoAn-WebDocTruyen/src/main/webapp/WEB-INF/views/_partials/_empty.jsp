<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
================================================================================
  _empty.jsp — MẢNH TÁI DÙNG: trạng thái rỗng
================================================================================
  TẦNG: views/

  DÙNG Ở: mọi trang có danh sách — kho truyện, truyện đã lưu, truyện của tôi,
          trang tác giả, bảng xếp hạng, các trang quản trị.

  CÁCH DÙNG:
      <c:set var="emIcon"  value="🔍"/>
      <c:set var="emTitle" value="Không tìm thấy truyện nào"/>
      <c:set var="emText"  value="Thử bỏ bớt bộ lọc hoặc tìm từ khoá khác."/>
      <c:set var="emUrl"   value="/story?action=list"/>
      <c:set var="emBtn"   value="Xem tất cả truyện"/>
      <%@ include file="/WEB-INF/views/_partials/_empty.jsp" %>

  emUrl và emBtn là TUỲ CHỌN — không đặt thì không hiện nút.

  VÌ SAO TRẠNG THÁI RỖNG PHẢI ĐƯỢC THIẾT KẾ:
    Danh sách trống mà để trang trắng trơn thì người dùng tưởng web hỏng.
    Một ô rỗng có biểu tượng, có lời giải thích và có lối đi tiếp thì trông
    như CHỦ Ý — và quan trọng hơn, nó nói cho người dùng biết phải làm gì.
================================================================================
--%>
<div class="empty">
    <div class="empty-icon">${empty emIcon ? '📭' : emIcon}</div>
    <h3><c:out value="${emTitle}"/></h3>
    <c:if test="${not empty emText}">
        <p><c:out value="${emText}"/></p>
    </c:if>
    <c:if test="${not empty emUrl and not empty emBtn}">
        <a class="btn btn-primary" href="${pageContext.request.contextPath}${emUrl}">
            <c:out value="${emBtn}"/></a>
    </c:if>
</div>
