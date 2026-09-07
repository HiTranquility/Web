<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--
================================================================================
  story/rank.jsp — MẢNH NỘI DUNG: Bảng xếp hạng                TRANG 5
================================================================================
  TẦNG: views/

  Nhận: stories (List<Story>) · by (String: views | chapters | newest)
================================================================================
--%>
<div class="section-head">
    <h2>🏆 Bảng xếp hạng</h2>
    <span class="more">Top 20</span>
</div>

<%--
  Tab chọn kiểu xếp hạng — là thẻ <a> chứ không phải nút JavaScript.
  Nhờ vậy mỗi bảng có URL riêng: chia sẻ được, bookmark được, nút Back của
  trình duyệt hoạt động đúng. Đây là ưu điểm của server-rendered.
--%>
<div class="tag-row" style="margin-bottom:22px">
    <a class="tag ${by eq 'views' ? 'is-on' : ''}"
       href="${pageContext.request.contextPath}/rank?by=views">👁️ Xem nhiều nhất</a>
    <a class="tag ${by eq 'chapters' ? 'is-on' : ''}"
       href="${pageContext.request.contextPath}/rank?by=chapters">📄 Nhiều chương nhất</a>
    <a class="tag ${by eq 'newest' ? 'is-on' : ''}"
       href="${pageContext.request.contextPath}/rank?by=newest">✨ Mới đăng</a>
</div>

<c:choose>
    <c:when test="${not empty stories}">
        <ol class="rank-list">
            <c:forEach var="story" items="${stories}" varStatus="st">
                <li class="rank-item">
                    <%-- varStatus.index đếm từ 0, nên +1 để hiện thứ hạng.
                         Ba hạng đầu tô màu hổ phách cho nổi. --%>
                    <span class="rank-no ${st.index lt 3 ? 'rank-top' : ''}">
                        ${st.index + 1}
                    </span>

                    <div class="rank-cover">
                        <c:choose>
                            <c:when test="${not empty story.coverUrl}">
                                <img src="<c:out value='${story.coverUrl}'/>"
                                     alt="<c:out value='${story.title}'/>" loading="lazy">
                            </c:when>
                            <c:otherwise>
                                <div class="cover-fallback">${story.initial}</div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div class="rank-info">
                        <a class="rank-title"
                           href="${pageContext.request.contextPath}/story?action=detail&amp;id=${story.id}">
                            <c:out value="${story.title}"/></a>
                        <p class="rank-meta">
                            <a href="${pageContext.request.contextPath}/user?action=profile&amp;id=${story.authorId}">
                                <c:out value="${story.authorName}"/></a>
                            &middot; ${story.chapterCount} chương
                            &middot; <fmt:formatNumber pattern="#,##0" value="${story.viewCount}"/> lượt xem
                        </p>
                    </div>

                    <span class="pill ${story.completed ? 'pill-ok' : 'pill-muted'}">
                        ${story.completed ? 'Hoàn thành' : 'Đang ra'}
                    </span>
                </li>
            </c:forEach>
        </ol>

        <div class="note" style="margin-top:26px">
            <b>Về cách xếp hạng.</b> Hiện xếp theo tổng tích luỹ từ trước tới nay,
            chưa có "theo tuần / theo tháng". Muốn làm được cần một bảng ghi từng
            lượt xem kèm thời điểm — chi tiết ở
            <code>docs/ke-hoach-database.md</code>.
        </div>
    </c:when>

    <c:otherwise>
        <c:set var="emIcon"  value="🏆"/>
        <c:set var="emTitle" value="Chưa có truyện nào để xếp hạng"/>
        <c:set var="emText"  value="Kho truyện đang trống."/>
        <c:set var="emUrl"   value="/story?action=list"/>
        <c:set var="emBtn"   value="Xem kho truyện"/>
        <%@ include file="/WEB-INF/views/_partials/_empty.jsp" %>
    </c:otherwise>
</c:choose>
