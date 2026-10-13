<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%-- ================================================================================
     MẢNH: TỦ HUY HIỆU & THÀNH TÍCH ĐỘC GIẢ (ACHIEVEMENTS SHOWCASE)
     Nhận vào biến: `achievements` (List<Achievement>)
     ================================================================================ --%>
<c:if test="${not empty achievements}">
    <c:set var="unlockedCount" value="0" />
    <c:forEach var="a" items="${achievements}">
        <c:if test="${a.unlocked}">
            <c:set var="unlockedCount" value="${unlockedCount + 1}" />
        </c:if>
    </c:forEach>

    <div class="achievement-showcase-panel" id="achievements-showcase">
        <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:12px; margin-bottom:18px;">
            <div style="display:flex; align-items:center; gap:10px;">
                <span style="font-size:1.5rem; line-height:1;">🏆</span>
                <div>
                    <h3 style="margin:0; font-size:1.15rem; font-weight:800; color:var(--text);">Tủ Huy Hiệu &amp; Danh Hiệu</h3>
                    <p style="margin:2px 0 0 0; font-size:0.82rem; color:var(--text-dim);">Ghi nhận dấu ấn, hoạt động đọc truyện và đóng góp cộng đồng</p>
                </div>
            </div>
            <div style="display:flex; align-items:center; gap:8px;">
                <span class="badge ${unlockedCount gt 0 ? 'badge-amber' : 'badge-muted'}" style="padding:6px 14px; border-radius:99px; font-weight:700; font-size:0.85rem; display:inline-flex; align-items:center; gap:6px;">
                    ✨ Đã đạt: <b>${unlockedCount} / ${achievements.size()}</b> danh hiệu
                </span>
            </div>
        </div>

        <div class="achievements-grid">
            <c:forEach var="ach" items="${achievements}">
                <div class="achievement-card ${ach.unlocked ? 'is-unlocked' : 'is-locked'}" title="${ach.unlocked ? 'Đã đạt danh hiệu này' : 'Chưa mở khoá: '.concat(ach.progressText)}">
                    <div class="ach-icon-wrap">
                        <span class="ach-icon">${ach.icon}</span>
                        <c:if test="${ach.unlocked}">
                            <span class="ach-check-mark" title="Đã mở khoá">✓</span>
                        </c:if>
                    </div>
                    <div class="ach-info">
                        <div class="ach-title"><c:out value="${ach.title}"/></div>
                        <div class="ach-desc"><c:out value="${ach.description}"/></div>
                        <div class="ach-meta">
                            <span class="ach-progress-text">
                                <c:choose>
                                    <c:when test="${ach.unlocked}">
                                        <span style="color:#10b981; font-weight:700;">✓ Hoàn thành</span> (${ach.progressText})
                                    </c:when>
                                    <c:otherwise>
                                        <c:out value="${ach.progressText}"/>
                                    </c:otherwise>
                                </c:choose>
                            </span>
                            <div class="ach-progress-bar">
                                <div class="ach-progress-fill" style="width: ${ach.progressPercent}%; ${ach.unlocked ? 'background: #10b981;' : ''}"></div>
                            </div>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </div>
</c:if>
