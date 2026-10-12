<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
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

    <c:set var="stIcon"  value="🪙"/>
    <c:set var="stValue" value="${totalEarnings != null ? totalEarnings : 0}"/>
    <c:set var="stLabel" value="Xu kiếm được"/>
    <c:set var="stNote"  value="từ mở khoá VIP & tặng thưởng"/>
    <%@ include file="/WEB-INF/views/_partials/_stat-tile.jsp" %>
</div>

<c:if test="${not empty topStory}">
    <div class="stats-charts-grid" style="display:grid; grid-template-columns:repeat(auto-fit, minmax(360px, 1fr)); gap:20px; margin-top:22px;">
        <div class="chart-card-box">
            <c:set var="dcTitle" value="Lượt xem 14 ngày — ${topStory.title}"/>
            <c:set var="dcData"  value="${dViews}"/>
            <c:set var="dcMax"   value="${mViews}"/>
            <c:set var="dcUnit"  value="lượt"/>
            <%@ include file="/WEB-INF/views/_partials/_daily-chart.jsp" %>
        </div>

        <div class="retention-card-box" style="background:var(--ink-850); border:1px solid var(--ink-700); border-radius:14px; padding:18px 20px;">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:10px;">
                <h4 style="margin:0; font-size:1.02rem; font-weight:700; color:var(--text);">📈 Tỷ lệ độc giả đọc qua các chương</h4>
                <span style="font-size:0.75rem; color:#f59e0b; background:rgba(245,158,11,0.12); padding:2px 8px; border-radius:6px; font-weight:700;">Reader Retention</span>
            </div>
            <p style="font-size:0.82rem; color:var(--text-mut); margin:0 0 14px 0; line-height:1.4;">
                Mô hình phễu giữ chân giúp tác giả đánh giá mức độ hấp dẫn và điểm rơi độc giả của truyện <b><c:out value="${topStory.title}"/></b>.
            </p>

            <c:choose>
                <c:when test="${not empty topChapters}">
                    <div class="retention-bars" style="display:flex; flex-direction:column; gap:10px; max-height:220px; overflow-y:auto; padding-right:4px;">
                        <c:set var="chapTotal" value="${topChapters.size()}"/>
                        <c:forEach var="c" items="${topChapters}" varStatus="st">
                            <fmt:formatNumber var="pct" value="${st.first ? 100 : (100 - (st.index * 60 / (chapTotal gt 1 ? (chapTotal - 1) : 1)))}" maxFractionDigits="0"/>
                            <div class="retention-item" style="display:flex; flex-direction:column; gap:4px;">
                                <div style="display:flex; justify-content:space-between; font-size:0.82rem;">
                                    <span style="color:var(--text); font-weight:600; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; max-width:260px;">Chương ${c.chapterNo}: <c:out value="${c.title}"/></span>
                                    <span style="color:${pct ge 70 ? '#10b981' : (pct ge 40 ? '#f59e0b' : '#38bdf8')}; font-weight:700;">${pct}% giữ chân</span>
                                </div>
                                <div style="height:7px; background:var(--ink-750); border-radius:99px; overflow:hidden;">
                                    <div style="height:100%; width:${pct}%; background:linear-gradient(90deg, #f59e0b, ${pct ge 70 ? '#10b981' : '#38bdf8'}); border-radius:99px; transition:width .4s ease;"></div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:when>
                <c:otherwise>
                    <div style="text-align:center; padding:20px; color:var(--text-mut); font-size:0.88rem;">Chưa có dữ liệu chương để thống kê.</div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</c:if>

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
                <th>Thu nhập</th>
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
                            <c:when test="${not empty storyIncomes and storyIncomes[s.id] gt 0}">
                                <span style="color:#f59e0b; font-weight:700;">+<fmt:formatNumber pattern="#,##0" value="${storyIncomes[s.id]}"/> xu</span>
                            </c:when>
                            <c:otherwise><span class="muted">0 xu</span></c:otherwise>
                        </c:choose>
                    </td>
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

        <div class="section-head" style="margin-top:34px">
            <h3>🪙 Độc giả ủng hộ &amp; Mở khoá chương gần nhất</h3>
            <span class="more">Tổng tích luỹ: <b style="color:#f59e0b;"><fmt:formatNumber pattern="#,##0" value="${totalEarnings != null ? totalEarnings : 0}"/> xu</b></span>
        </div>

        <c:choose>
            <c:when test="${not empty recentEarnings}">
                <div class="admin-table-wrap">
                    <table class="admin-table">
                        <tr>
                            <th>Thời gian</th>
                            <th>Độc giả</th>
                            <th>Truyện liên quan</th>
                            <th>Số xu</th>
                            <th>Lời nhắn / Lý do</th>
                        </tr>
                        <c:forEach var="tx" items="${recentEarnings}">
                            <tr>
                                <td style="white-space:nowrap; font-size:0.85rem; color:var(--text-mut);">
                                    <c:choose>
                                        <c:when test="${not empty tx.createdAt}">
                                            ${tx.createdAt.dayOfMonth}/${tx.createdAt.monthValue}/${tx.createdAt.year} ${tx.createdAt.hour}:${tx.createdAt.minute lt 10 ? '0' : ''}${tx.createdAt.minute}
                                        </c:when>
                                        <c:otherwise>—</c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <b><c:out value="${not empty tx.fromUsername ? tx.fromUsername : 'Độc giả'}"/></b>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty tx.storyTitle}">
                                            <a href="${pageContext.request.contextPath}/story?action=detail&amp;id=${tx.storyId}">
                                                <c:out value="${tx.storyTitle}"/>
                                            </a>
                                        </c:when>
                                        <c:otherwise><span class="muted">—</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <span style="color:#f59e0b; font-weight:800; font-size:0.95rem;">
                                        +<fmt:formatNumber pattern="#,##0" value="${tx.amount}"/> xu
                                    </span>
                                </td>
                                <td>
                                    <span class="muted"><c:out value="${tx.message}"/></span>
                                </td>
                            </tr>
                        </c:forEach>
                    </table>
                </div>
            </c:when>
            <c:otherwise>
                <div style="background:var(--ink-850); border:1px solid var(--ink-700); border-radius:10px; padding:18px 20px; color:var(--text-mut); font-size:0.88rem;">
                    🪙 Chưa có giao dịch ủng hộ nào từ độc giả. Hãy tiếp tục sáng tác và đăng thêm nhiều chương hấp dẫn nhé!
                </div>
            </c:otherwise>
        </c:choose>

        <p class="muted-note" style="margin-top:22px">
            Biểu đồ phía trên đếm từ bảng <code>view_logs</code> — nơi ghi
            <b>từng</b> lượt xem kèm thời điểm. Cột "Lượt xem" trong bảng này
            là số cộng dồn từ ngày đăng, nên hai con số không bằng nhau và
            không cần bằng nhau.
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
