<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
================================================================================
  user/notifications.jsp — MẢNH NỘI DUNG: Trung tâm Thông báo     TRANG 18
================================================================================
--%>
<div class="section-head">
    <div>
        <h2>🔔 Trung tâm Thông báo</h2>
        <span class="more">Cập nhật chương mới từ tác giả bạn theo dõi và các thông báo hệ thống</span>
    </div>
    <span class="more"><b>${empty notifications ? 0 : notifications.size()}</b> thông báo</span>
</div>

<c:choose>
    <c:when test="${not empty notifications}">
        <%-- Thanh công cụ lọc loại thông báo --%>
        <div class="bookshelf-toolbar" style="margin-bottom: 16px;">
            <div class="bookshelf-filters" id="notif-filters">
                <button type="button" class="bs-filter-chip is-active" data-filter="all">
                    Tất cả <span class="bs-chip-count">${fn:length(notifications)}</span>
                </button>
                <button type="button" class="bs-filter-chip" data-filter="chapter">
                    📖 Chương mới
                </button>
                <button type="button" class="bs-filter-chip" data-filter="comment">
                    💬 Bình luận &amp; Thảo luận
                </button>
                <button type="button" class="bs-filter-chip" data-filter="system">
                    🛡️ Hệ thống
                </button>
            </div>

            <div class="bookshelf-tools-right">
                <button type="button" class="btn btn-ghost btn-sm" id="btn-mark-all-read" title="Đánh dấu tất cả là đã đọc">
                    ✓ Đã đọc hết
                </button>
            </div>
        </div>

        <div class="notif-list" id="notif-list-container">
            <c:forEach var="n" items="${notifications}">
                <c:set var="notifType" value="system"/>
                <c:if test="${not empty n.chapterId}"><c:set var="notifType" value="chapter"/></c:if>
                <c:if test="${empty n.chapterId and not empty n.storyId}"><c:set var="notifType" value="comment"/></c:if>

                <c:choose>
                    <c:when test="${not empty n.chapterId}">
                        <a class="notif notif-item ${n.read ? '' : 'notif-new'}"
                           data-type="${notifType}"
                           href="${pageContext.request.contextPath}/chapter?action=read&amp;id=${n.chapterId}">
                            <span class="notif-icon">${not empty n.icon ? n.icon : '📖'}</span>
                            <span class="notif-body">
                                <span class="notif-text"><c:out value="${n.message}"/></span>
                                <span class="notif-time">
                                    <c:if test="${not empty n.createdAt}">
                                        ${n.createdAt.dayOfMonth}/${n.createdAt.monthValue}
                                        lúc ${n.createdAt.hour}:${n.createdAt.minute lt 10 ? '0' : ''}${n.createdAt.minute}
                                    </c:if>
                                </span>
                            </span>
                            <c:if test="${not n.read}"><span class="notif-dot"></span></c:if>
                        </a>
                    </c:when>
                    <c:when test="${not empty n.storyId}">
                        <a class="notif notif-item ${n.read ? '' : 'notif-new'}"
                           data-type="${notifType}"
                           href="${pageContext.request.contextPath}/story?action=detail&amp;id=${n.storyId}#comments">
                            <span class="notif-icon">${not empty n.icon ? n.icon : '💬'}</span>
                            <span class="notif-body">
                                <span class="notif-text"><c:out value="${n.message}"/></span>
                                <span class="notif-time">
                                    <c:if test="${not empty n.createdAt}">
                                        ${n.createdAt.dayOfMonth}/${n.createdAt.monthValue}
                                        lúc ${n.createdAt.hour}:${n.createdAt.minute lt 10 ? '0' : ''}${n.createdAt.minute}
                                    </c:if>
                                </span>
                            </span>
                            <c:if test="${not n.read}"><span class="notif-dot"></span></c:if>
                        </a>
                    </c:when>
                    <c:otherwise>
                        <div class="notif notif-item ${n.read ? '' : 'notif-new'}"
                             data-type="${notifType}">
                            <span class="notif-icon">${not empty n.icon ? n.icon : '🔔'}</span>
                            <span class="notif-body">
                                <span class="notif-text"><c:out value="${n.message}"/></span>
                                <span class="notif-time">
                                    <c:if test="${not empty n.createdAt}">
                                        ${n.createdAt.dayOfMonth}/${n.createdAt.monthValue}
                                    </c:if>
                                </span>
                            </span>
                        </div>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
        </div>

        <div id="notif-empty-filter" style="display:none; padding:32px 10px; text-align:center; color:var(--text-mut);">
            Không có thông báo nào thuộc danh mục này.
        </div>

        <script>
        (function() {
            var filter = 'all';
            var items = document.querySelectorAll('#notif-list-container .notif-item');
            var emptyNotice = document.getElementById('notif-empty-filter');

            function applyNotifFilter() {
                var visibleCount = 0;
                items.forEach(function(el) {
                    var t = el.getAttribute('data-type') || 'system';
                    if (filter === 'all' || t === filter) {
                        el.style.display = 'flex';
                        visibleCount++;
                    } else {
                        el.style.display = 'none';
                    }
                });
                if (emptyNotice) {
                    emptyNotice.style.display = (visibleCount === 0 && items.length > 0) ? 'block' : 'none';
                }
            }

            document.querySelectorAll('#notif-filters .bs-filter-chip').forEach(function(btn) {
                btn.addEventListener('click', function() {
                    document.querySelectorAll('#notif-filters .bs-filter-chip').forEach(function(b) {
                        b.classList.remove('is-active');
                    });
                    this.classList.add('is-active');
                    filter = this.getAttribute('data-filter');
                    applyNotifFilter();
                });
            });

            var markAllBtn = document.getElementById('btn-mark-all-read');
            if (markAllBtn) {
                markAllBtn.addEventListener('click', function() {
                    document.querySelectorAll('.notif-dot').forEach(function(dot) { dot.remove(); });
                    document.querySelectorAll('.notif-new').forEach(function(el) { el.classList.remove('notif-new'); });
                    this.textContent = '✓ Tất cả đã đọc';
                    this.disabled = true;
                });
            }
        })();
        </script>
    </c:when>

    <c:otherwise>
        <c:set var="emIcon"  value="🔔"/>
        <c:set var="emTitle" value="Chưa có thông báo nào"/>
        <c:set var="emText"  value="Theo dõi một tác giả để được báo khi họ đăng chương mới."/>
        <c:set var="emUrl"   value="/follow?action=list"/>
        <c:set var="emBtn"   value="Xem đang theo dõi"/>
        <%@ include file="/WEB-INF/views/_partials/_empty.jsp" %>
    </c:otherwise>
</c:choose>
