<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--
================================================================================
  story/list.jsp — MẢNH NỘI DUNG: Kho truyện          CASE 02 + 03
================================================================================
  TẦNG: views/  — chỉ hiển thị. Dữ liệu do StoryServlet chuẩn bị sẵn.

  Nhận từ servlet:
      stories · tags · currentTag · keyword · sort · page · totalPages · totalStories

  Trang này gánh CẢ duyệt, lọc thể loại VÀ tìm kiếm — không tách trang riêng
  cho kết quả tìm kiếm. Cùng giao diện, chỉ khác tham số URL.
================================================================================
--%>
<div class="section-head">
    <h2>
        <c:choose>
            <c:when test="${not empty keyword}">Tìm: "<c:out value='${keyword}'/>"</c:when>
            <c:when test="${not empty currentTag}">Lọc theo thể loại</c:when>
            <c:otherwise>Kho truyện</c:otherwise>
        </c:choose>
    </h2>
    <span class="more">${totalStories} truyện</span>
</div>

<%-- Ô tìm kiếm. method="get" vì tìm kiếm chỉ ĐỌC — nhờ vậy URL kết quả
     chia sẻ và bookmark được. --%>
<form action="${pageContext.request.contextPath}/story" method="get" class="search-bar">
    <input type="hidden" name="action" value="list">
    <c:if test="${not empty currentTag}">
        <input type="hidden" name="tag" value="<c:out value='${currentTag}'/>">
    </c:if>
    <input type="text" name="q" placeholder="Tìm theo tên truyện…"
           value="<c:out value='${keyword}'/>">
    <button type="submit" class="btn btn-primary btn-sm">Tìm</button>
</form>

<%-- MẢNH: bộ lọc thể loại --%>
<c:set var="tfBase" value="story?action=list" scope="request"/>
<%@ include file="/WEB-INF/views/_partials/_tag-filter.jsp" %>

<c:choose>
    <c:when test="${not empty stories}">
        <div class="story-grid">
            <c:forEach var="story" items="${stories}">
                <%@ include file="/WEB-INF/views/story/_card.jsp" %>
            </c:forEach>
        </div>

        <%--
          MẢNH: phân trang.
          pgQuery giữ lại tag / từ khoá / sắp xếp khi chuyển trang — không thì
          bấm sang trang 2 là mất sạch bộ lọc.
        --%>
        <c:set var="pgBase" value="story?action=list" scope="request"/>
        <c:set var="pgQuery" value="" scope="request"/>
        <c:if test="${not empty currentTag}">
            <c:set var="pgQuery" value="${pgQuery}&tag=${currentTag}" scope="request"/>
        </c:if>
        <c:if test="${not empty keyword}">
            <c:set var="pgQuery" value="${pgQuery}&q=${keyword}" scope="request"/>
        </c:if>
        <c:if test="${not empty sort}">
            <c:set var="pgQuery" value="${pgQuery}&sort=${sort}" scope="request"/>
        </c:if>
        <%@ include file="/WEB-INF/views/_partials/_pagination.jsp" %>
    </c:when>

    <c:otherwise>
        <%-- MẢNH: trạng thái rỗng --%>
        <c:set var="emIcon"  value="🔍"/>
        <c:set var="emTitle" value="Không tìm thấy truyện nào"/>
        <c:set var="emText"  value="Thử bỏ bớt bộ lọc, hoặc tìm bằng từ khoá khác."/>
        <c:set var="emUrl"   value="/story?action=list"/>
        <c:set var="emBtn"   value="Xem tất cả truyện"/>
        <%@ include file="/WEB-INF/views/_partials/_empty.jsp" %>
    </c:otherwise>
</c:choose>
