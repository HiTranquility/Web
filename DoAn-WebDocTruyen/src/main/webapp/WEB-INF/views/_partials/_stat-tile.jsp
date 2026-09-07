<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--
================================================================================
  _stat-tile.jsp — MẢNH TÁI DÙNG: ô thống kê
================================================================================
  TẦNG: views/

  DÙNG Ở: trang 16 (thống kê truyện của tôi), 25 (bảng điều khiển quản trị)

  CÁCH DÙNG:
      <c:set var="stIcon"  value="👁️"/>
      <c:set var="stValue" value="${cViews}"/>
      <c:set var="stLabel" value="Lượt xem"/>
      <c:set var="stNote"  value="tính từ ngày đăng"/>   (tuỳ chọn)
      <%@ include file="/WEB-INF/views/_partials/_stat-tile.jsp" %>

  SỐ TO, NHÃN NHỎ — có lý do
    Người mở bảng thống kê đang QUÉT chứ không ĐỌC. Cho số nổi bật hẳn thì
    mắt bắt được ngay cả khi lướt nhanh; nhãn chỉ cần đủ rõ khi đã dừng lại.
================================================================================
--%>
<div class="stat-tile">
    <div class="stat-icon">${empty stIcon ? '📊' : stIcon}</div>
    <div class="stat-value"><fmt:formatNumber pattern="#,##0" value="${stValue}"/></div>
    <div class="stat-label"><c:out value="${stLabel}"/></div>
    <c:if test="${not empty stNote}">
        <div class="stat-note"><c:out value="${stNote}"/></div>
    </c:if>
</div>
