<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--
================================================================================
  _story-row.jsp — MẢNH TÁI DÙNG: truyện dạng HÀNG NGANG
================================================================================
  TẦNG: views/

  DÙNG Ở: trang 11 (đã lưu), 16 (thống kê), 17 (theo dõi), 26 (quản trị truyện)

  KHÁC GÌ _card.jsp
    _card là ô đứng trong lưới — dùng khi muốn KHOE bìa truyện, người dùng
    đang lướt tìm cái gì đó bắt mắt.
    _story-row là hàng ngang — dùng khi người dùng đã BIẾT mình tìm gì và cần
    so sánh nhanh theo cột (tiến độ đọc, lượt xem, trạng thái).

    Cùng dữ liệu, hai cách đọc khác nhau. Đó là lý do có hai mảnh chứ không
    phải một mảnh với cờ bật/tắt.

  CÁCH DÙNG:
      <c:set var="srStory" value="${story}"/>
      <c:set var="srMeta"  value="Đang đọc chương 12 / 31"/>   (tuỳ chọn)
      <c:set var="srSlot"  value="..."/>                        (tuỳ chọn, HTML thô)
      <%@ include file="/WEB-INF/views/_partials/_story-row.jsp" %>
================================================================================
--%>
<div class="story-row">

    <a class="story-row-cover"
       href="${pageContext.request.contextPath}/story?action=detail&amp;id=${srStory.id}">
        <c:choose>
            <c:when test="${not empty srStory.coverUrl}">
                <img src="<c:out value='${srStory.coverUrl}'/>"
                     alt="<c:out value='${srStory.title}'/>" loading="lazy">
            </c:when>
            <c:otherwise>
                <span class="cover-fallback">${srStory.initial}</span>
            </c:otherwise>
        </c:choose>
    </a>

    <div class="story-row-body">
        <a class="story-row-title"
           href="${pageContext.request.contextPath}/story?action=detail&amp;id=${srStory.id}">
            <c:out value="${srStory.title}"/></a>

        <p class="story-row-meta">
            <a href="${pageContext.request.contextPath}/user?action=profile&amp;id=${srStory.authorId}">
                <c:out value="${srStory.authorName}"/></a>
            &middot; ${srStory.chapterCount} chương
            &middot; <fmt:formatNumber pattern="#,##0" value="${srStory.viewCount}"/> lượt xem
        </p>

        <%-- Dòng phụ do trang gọi quyết định: tiến độ đọc, ngày cập nhật... --%>
        <c:if test="${not empty srMeta}">
            <p class="story-row-sub"><c:out value="${srMeta}"/></p>
        </c:if>
    </div>

    <div class="story-row-side">
        <span class="pill ${srStory.completed ? 'pill-ok' : 'pill-muted'}">
            ${srStory.completed ? 'Hoàn thành' : 'Đang ra'}
        </span>
    </div>
</div>
