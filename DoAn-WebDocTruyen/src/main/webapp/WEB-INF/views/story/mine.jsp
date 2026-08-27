<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- story/mine.jsp — Truyện của tôi. Gồm cả bản nháp.   CASE 05 --%>
<div class="section-head">
    <h2>Truyện của tôi</h2>
    <a class="btn btn-primary btn-sm"
       href="${pageContext.request.contextPath}/story?action=create">+ Đăng truyện mới</a>
</div>

<c:choose>
    <c:when test="${not empty stories}">
        <div class="story-grid">
            <c:forEach var="story" items="${stories}">
                <%@ include file="/WEB-INF/views/story/_card.jsp" %>
            </c:forEach>
        </div>
    </c:when>
    <c:otherwise>
        <div class="empty">
            <div class="empty-icon">✍️</div>
            <h3>Bạn chưa đăng truyện nào</h3>
            <p>Bắt đầu viết câu chuyện đầu tiên của bạn. Có thể lưu nháp trước,
               khi nào ưng ý mới công khai.</p>
            <a class="btn btn-primary"
               href="${pageContext.request.contextPath}/story?action=create">Đăng truyện đầu tiên</a>
        </div>
    </c:otherwise>
</c:choose>
