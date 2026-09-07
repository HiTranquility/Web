<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--
================================================================================
  user/profile.jsp — MẢNH NỘI DUNG: Trang tác giả              TRANG 4
================================================================================
  TẦNG: views/ — chỉ hiển thị. Dữ liệu do UserServlet chuẩn bị sẵn.

  Nhận: author (User) · stories · totalStories · totalViews · page · totalPages

  Dùng khung layout/main.jsp — vẫn có nav và footer như trang thường.
  Đúng nhánh 5 của bảng quyết định chọn layout: không rơi vào khung đặc biệt
  nào thì dùng main, không tạo layout mới.
================================================================================
--%>
<div class="profile-head">
    <span class="profile-avatar">${author.initial}</span>

    <div class="profile-info">
        <h1><c:out value="${author.name}"/></h1>
        <p class="profile-username">@<c:out value="${author.username}"/></p>

        <c:if test="${not empty author.bio}">
            <p class="profile-bio"><c:out value="${author.bio}"/></p>
        </c:if>

        <div class="profile-stats">
            <span><b>${totalStories}</b> truyện</span>
            <span><b><fmt:formatNumber pattern="#,##0" value="${totalViews}"/></b> lượt xem</span>
            <c:if test="${author.admin}">
                <span class="pill pill-warn">Quản trị viên</span>
            </c:if>
        </div>
    </div>
</div>

<div class="section-head">
    <h2>Truyện đã đăng</h2>
    <span class="more">${totalStories} truyện</span>
</div>

<c:choose>
    <c:when test="${not empty stories}">
        <div class="story-grid">
            <c:forEach var="story" items="${stories}">
                <%@ include file="/WEB-INF/views/story/_card.jsp" %>
            </c:forEach>
        </div>

        <%-- MẢNH: phân trang. pgQuery giữ id tác giả khi chuyển trang. --%>
        <c:set var="pgBase"  value="user?action=profile" scope="request"/>
        <c:set var="pgQuery" value="&id=${author.id}" scope="request"/>
        <%@ include file="/WEB-INF/views/_partials/_pagination.jsp" %>
    </c:when>

    <c:otherwise>
        <%-- MẢNH: trạng thái rỗng --%>
        <c:set var="emIcon"  value="📖"/>
        <c:set var="emTitle" value="Tác giả chưa đăng truyện nào"/>
        <c:set var="emText"  value="Quay lại sau nhé, biết đâu sẽ có truyện mới."/>
        <c:set var="emUrl"   value="/story?action=list"/>
        <c:set var="emBtn"   value="Xem kho truyện"/>
        <%@ include file="/WEB-INF/views/_partials/_empty.jsp" %>
    </c:otherwise>
</c:choose>
