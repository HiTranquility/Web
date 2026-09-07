<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--
================================================================================
  admin/dashboard.jsp — MẢNH NỘI DUNG: Bảng điều khiển          TRANG 25
================================================================================
  TẦNG: views/ · layout: admin

  Nhận: cStories cChapters cUsers cViews cComments cDeleted
        pending · topStories · tags
================================================================================
--%>
<h1>Bảng điều khiển</h1>
<p>Toàn cảnh hệ thống trong một màn hình.</p>

<c:if test="${pending gt 0}">
    <%-- Việc cần làm luôn nằm trên cùng. Số liệu đẹp mà báo cáo tồn đọng
         nằm dưới cuối trang thì không ai xử lý. --%>
    <div class="panel panel-warn">
        <b>${pending} báo cáo</b> đang chờ xử lý.
        <a href="${pageContext.request.contextPath}/admin/report?status=PENDING">Xem ngay →</a>
    </div>
</c:if>

<div class="stat-grid">
    <c:set var="stIcon" value="📚"/><c:set var="stValue" value="${cStories}"/>
    <c:set var="stLabel" value="Truyện công khai"/><c:set var="stNote" value=""/>
    <%@ include file="/WEB-INF/views/_partials/_stat-tile.jsp" %>

    <c:set var="stIcon" value="📄"/><c:set var="stValue" value="${cChapters}"/>
    <c:set var="stLabel" value="Chương"/>
    <%@ include file="/WEB-INF/views/_partials/_stat-tile.jsp" %>

    <c:set var="stIcon" value="👤"/><c:set var="stValue" value="${cUsers}"/>
    <c:set var="stLabel" value="Tài khoản"/>
    <%@ include file="/WEB-INF/views/_partials/_stat-tile.jsp" %>

    <c:set var="stIcon" value="👁️"/><c:set var="stValue" value="${cViews}"/>
    <c:set var="stLabel" value="Tổng lượt xem"/>
    <%@ include file="/WEB-INF/views/_partials/_stat-tile.jsp" %>

    <c:set var="stIcon" value="💬"/><c:set var="stValue" value="${cComments}"/>
    <c:set var="stLabel" value="Bình luận"/>
    <%@ include file="/WEB-INF/views/_partials/_stat-tile.jsp" %>

    <c:set var="stIcon" value="🗑️"/><c:set var="stValue" value="${cDeleted}"/>
    <c:set var="stLabel" value="Truyện đã gỡ"/>
    <c:set var="stNote" value="xoá mềm, khôi phục được"/>
    <%@ include file="/WEB-INF/views/_partials/_stat-tile.jsp" %>
</div>

<div class="dash-cols">
    <section>
        <div class="section-head">
            <h3>Truyện đọc nhiều nhất</h3>
            <a class="more" href="${pageContext.request.contextPath}/rank">Bảng xếp hạng →</a>
        </div>

        <c:choose>
            <c:when test="${not empty topStories}">
                <ol class="rank-list">
                    <c:forEach var="story" items="${topStories}" varStatus="st">
                        <li class="rank-item">
                            <span class="rank-no ${st.index lt 3 ? 'rank-top' : ''}">
                                ${st.index + 1}</span>
                            <div class="rank-info">
                                <a class="rank-title"
                                   href="${pageContext.request.contextPath}/story?action=detail&amp;id=${story.id}">
                                    <c:out value="${story.title}"/></a>
                                <p class="rank-meta">
                                    <c:out value="${story.authorName}"/>
                                    &middot; <fmt:formatNumber pattern="#,##0" value="${story.viewCount}"/> lượt xem
                                </p>
                            </div>
                        </li>
                    </c:forEach>
                </ol>
            </c:when>
            <c:otherwise><p class="muted">Chưa có truyện nào.</p></c:otherwise>
        </c:choose>
    </section>

    <section>
        <div class="section-head">
            <h3>Thể loại được dùng nhiều</h3>
            <a class="more" href="${pageContext.request.contextPath}/admin/tag">Quản lý →</a>
        </div>

        <%--
          Thanh ngang thay cho biểu đồ.
          Vẽ biểu đồ thật cần thư viện JavaScript ngoài; ở đây độ dài thanh
          tính bằng CSS từ tỉ lệ phần trăm, không thêm phụ thuộc nào và vẫn
          so sánh được bằng mắt — đúng nhu cầu của trang này.
        --%>
        <div class="bar-list">
            <c:forEach var="t" items="${tags}">
                <c:if test="${t.storyCount gt 0}">
                    <div class="bar-row">
                        <span class="bar-label">
                            <a href="${pageContext.request.contextPath}/story?action=list&amp;tag=${t.slug}">
                                <c:out value="${t.name}"/></a>
                        </span>
                        <span class="bar-track">
                            <span class="bar-fill"
                                  style="width:${cStories gt 0 ? (t.storyCount * 100 / cStories) : 0}%"></span>
                        </span>
                        <span class="bar-value">${t.storyCount}</span>
                    </div>
                </c:if>
            </c:forEach>
        </div>
    </section>
</div>

<p class="muted-note" style="margin-top:26px">
    <b>Vì sao chưa có "tài khoản mới theo ngày".</b> Bảng <code>users</code> có
    <code>created_at</code> nên đếm được, nhưng vẽ đường theo ngày cần một
    biểu đồ thật. Sáu con số trên đủ trả lời câu hỏi thường gặp nhất — hệ
    thống đang lớn hay đang đứng yên.
</p>
