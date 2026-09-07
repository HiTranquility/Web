<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--
================================================================================
  story/stats.jsp — MẢNH NỘI DUNG: Thống kê truyện của tôi     TRANG 16
================================================================================
  TẦNG: views/

  Nhận: stories (List<Story>) · stats (Map<Integer,int[]>)
        totalViews · totalSaves · totalComments

  stats là Map từ id truyện sang mảng [lượt xem, lượt lưu, bình luận].
  Trong EL, lấy phần tử mảng bằng chỉ số: ${stats[s.id][0]}.
================================================================================
--%>
<div class="section-head">
    <h2>📊 Thống kê truyện của tôi</h2>
    <a class="more" href="${pageContext.request.contextPath}/story?action=mine">
        Quản lý truyện →</a>
</div>

<%-- Ba ô tổng, dùng lại mảnh _stat-tile --%>
<div class="stat-grid">
    <c:set var="stIcon"  value="👁️"/>
    <c:set var="stValue" value="${totalViews}"/>
    <c:set var="stLabel" value="Lượt xem"/>
    <c:set var="stNote"  value="cộng dồn từ ngày đăng"/>
    <%@ include file="/WEB-INF/views/_partials/_stat-tile.jsp" %>

    <c:set var="stIcon"  value="🔖"/>
    <c:set var="stValue" value="${totalSaves}"/>
    <c:set var="stLabel" value="Lượt lưu"/>
    <c:set var="stNote"  value="người đánh dấu để đọc sau"/>
    <%@ include file="/WEB-INF/views/_partials/_stat-tile.jsp" %>

    <c:set var="stIcon"  value="💬"/>
    <c:set var="stValue" value="${totalComments}"/>
    <c:set var="stLabel" value="Bình luận"/>
    <c:set var="stNote"  value="chưa tính bình luận đã ẩn"/>
    <%@ include file="/WEB-INF/views/_partials/_stat-tile.jsp" %>
</div>

<c:choose>
    <c:when test="${not empty stories}">
        <div class="section-head" style="margin-top:34px">
            <h3>Theo từng truyện</h3>
        </div>

        <div class="admin-table-wrap">
        <table class="admin-table">
            <tr>
                <th>Truyện</th>
                <th>Trạng thái</th>
                <th>Chương</th>
                <th>Lượt xem</th>
                <th>Lượt lưu</th>
                <th>Bình luận</th>
                <th>Đánh giá</th>
            </tr>
            <c:forEach var="s" items="${stories}">
                <tr>
                    <td>
                        <a href="${pageContext.request.contextPath}/story?action=detail&amp;id=${s.id}">
                            <c:out value="${s.title}"/></a>
                    </td>
                    <td>
                        <span class="pill ${s.status eq 'PUBLISHED' ? 'pill-ok' : 'pill-muted'}">
                            ${s.status eq 'PUBLISHED' ? 'Công khai'
                              : (s.status eq 'DRAFT' ? 'Nháp' : 'Đã gỡ')}
                        </span>
                    </td>
                    <td>${s.chapterCount}</td>
                    <td><fmt:formatNumber pattern="#,##0" value="${stats[s.id][0]}"/></td>
                    <td>${stats[s.id][1]}</td>
                    <td>${stats[s.id][2]}</td>
                    <td>
                        <c:choose>
                            <c:when test="${s.rated}">
                                ★ ${s.ratingAvg}
                                <span class="muted">(${s.ratingCount})</span>
                            </c:when>
                            <c:otherwise><span class="muted">—</span></c:otherwise>
                        </c:choose>
                    </td>
                </tr>
            </c:forEach>
        </table>
        </div>

        <p class="muted-note" style="margin-top:18px">
            <b>Vì sao chưa có biểu đồ theo ngày.</b> Muốn vẽ đường lượt xem
            theo thời gian thì phải đếm từ bảng <code>view_logs</code> — bảng
            đó đã có trong lược đồ và đang ghi, nhưng trang này mới chỉ hiện
            số cộng dồn. Đó là việc của giai đoạn sau.
        </p>
    </c:when>

    <c:otherwise>
        <c:set var="emIcon"  value="📊"/>
        <c:set var="emTitle" value="Chưa có số liệu"/>
        <c:set var="emText"  value="Đăng truyện đầu tiên rồi quay lại đây xem có bao nhiêu người đọc."/>
        <c:set var="emUrl"   value="/story?action=create"/>
        <c:set var="emBtn"   value="Đăng truyện mới"/>
        <%@ include file="/WEB-INF/views/_partials/_empty.jsp" %>
    </c:otherwise>
</c:choose>
