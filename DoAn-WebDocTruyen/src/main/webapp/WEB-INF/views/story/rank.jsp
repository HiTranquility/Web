<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--
================================================================================
  story/rank.jsp — MẢNH NỘI DUNG: Bảng xếp hạng                TRANG 5
================================================================================
  TẦNG: views/

  Nhận: stories (List<Story>) · by (views | week | month | rating | chapters | newest)
================================================================================
--%>
<div class="section-head">
    <h2>🏆 Bảng xếp hạng</h2>
    <span class="more">Top 20</span>
</div>

<%--
  Sáu tab, mỗi tab là một thẻ <a> chứ không phải nút JavaScript.
  Nhờ vậy mỗi bảng có URL riêng: chia sẻ được, bookmark được, nút Back của
  trình duyệt hoạt động đúng. Đây là ưu điểm của trang dựng ở máy chủ.

  Hai tab đầu (tuần / tháng) đọc từ bảng view_logs — nguồn dữ liệu KHÁC hẳn
  bốn tab còn lại, vì chỉ nhật ký mới biết lượt xem xảy ra khi nào.
--%>
<div class="tag-row" style="margin-bottom:10px">
    <a class="tag ${by eq 'week' ? 'is-on' : ''}"
       href="${pageContext.request.contextPath}/rank?by=week">📅 Tuần này</a>
    <a class="tag ${by eq 'month' ? 'is-on' : ''}"
       href="${pageContext.request.contextPath}/rank?by=month">🗓️ Tháng này</a>
    <a class="tag ${empty by or by eq 'views' ? 'is-on' : ''}"
       href="${pageContext.request.contextPath}/rank?by=views">👁️ Xem nhiều nhất</a>
    <a class="tag ${by eq 'rating' ? 'is-on' : ''}"
       href="${pageContext.request.contextPath}/rank?by=rating">⭐ Điểm cao</a>
    <a class="tag ${by eq 'chapters' ? 'is-on' : ''}"
       href="${pageContext.request.contextPath}/rank?by=chapters">📄 Nhiều chương</a>
    <a class="tag ${by eq 'newest' ? 'is-on' : ''}"
       href="${pageContext.request.contextPath}/rank?by=newest">✨ Mới đăng</a>
</div>

<%-- Giải thích cách tính cho tab đang xem. Bảng xếp hạng mà không nói rõ
     xếp theo cái gì thì người đọc chỉ đoán, và thường đoán sai. --%>
<p class="muted-note" style="margin-bottom:22px">
    <c:choose>
        <c:when test="${by eq 'week'}">
            Đếm lượt xem thật trong <b>7 ngày</b> gần nhất, từ nhật ký
            <code>view_logs</code> — không phải tổng tích luỹ.
        </c:when>
        <c:when test="${by eq 'month'}">
            Đếm lượt xem thật trong <b>30 ngày</b> gần nhất.
        </c:when>
        <c:when test="${by eq 'rating'}">
            Điểm trung bình. Truyện phải có <b>ít nhất 3 lượt chấm</b> mới vào
            nhóm trên — một người chấm 5 sao không nói lên điều gì.
        </c:when>
        <c:otherwise>
            Tổng tích luỹ từ ngày đăng. Muốn xem giai đoạn gần đây thì chọn
            <b>Tuần này</b> hoặc <b>Tháng này</b>.
        </c:otherwise>
    </c:choose>
</p>

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
                            <c:if test="${story.rated}">
                                &middot; ★ ${story.ratingAvg}
                            </c:if>
                        </p>
                    </div>

                    <span class="pill ${story.completed ? 'pill-ok' : 'pill-muted'}">
                        ${story.completed ? 'Hoàn thành' : 'Đang ra'}
                    </span>
                </li>
            </c:forEach>
        </ol>
    </c:when>

    <c:otherwise>
        <%-- Bảng tuần/tháng rỗng có nghĩa KHÁC hẳn kho truyện rỗng: kho vẫn
             đầy truyện, chỉ là chưa ai đọc trong giai đoạn đó. Nói rõ ra,
             không để người dùng tưởng trang hỏng. --%>
        <c:choose>
            <c:when test="${by eq 'week' or by eq 'month'}">
                <c:set var="emIcon"  value="📅"/>
                <c:set var="emTitle" value="Chưa có lượt xem nào trong giai đoạn này"/>
                <c:set var="emText"  value="Kho truyện vẫn còn nguyên — chỉ là chưa ai mở truyện nào gần đây. Thử xem bảng tổng tích luỹ."/>
                <c:set var="emUrl"   value="/rank?by=views"/>
                <c:set var="emBtn"   value="Xem nhiều nhất mọi thời"/>
            </c:when>
            <c:otherwise>
                <c:set var="emIcon"  value="🏆"/>
                <c:set var="emTitle" value="Chưa có truyện nào để xếp hạng"/>
                <c:set var="emText"  value="Kho truyện đang trống."/>
                <c:set var="emUrl"   value="/story?action=list"/>
                <c:set var="emBtn"   value="Xem kho truyện"/>
            </c:otherwise>
        </c:choose>
        <%@ include file="/WEB-INF/views/_partials/_empty.jsp" %>
    </c:otherwise>
</c:choose>
