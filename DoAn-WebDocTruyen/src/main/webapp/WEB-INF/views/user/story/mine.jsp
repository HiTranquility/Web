<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
================================================================================
  story/mine.jsp — MẢNH NỘI DUNG: Không gian Tác giả & Quản lý truyện   TRANG 12
================================================================================
--%>
<div class="section-head">
    <div>
        <h2>Khu vực Sáng tác &amp; Quản lý truyện</h2>
        <span class="more">Quản lý tác phẩm, theo dõi lượt đọc và xuất bản chương mới</span>
    </div>
    <div style="display:flex;gap:8px;flex-wrap:wrap;">
        <a class="btn btn-ghost btn-sm"
           href="${pageContext.request.contextPath}/story?action=stats">📊 Thống kê chung</a>
        <a class="btn btn-primary btn-sm"
           href="${pageContext.request.contextPath}/story?action=create">✍️ Đăng truyện mới</a>
    </div>
</div>

<c:choose>
    <c:when test="${not empty stories}">
        <c:set var="totalStoriesCount" value="${stories.size()}"/>
        <c:set var="totalChaptersCount" value="0"/>
        <c:set var="totalViewsCount" value="0"/>
        <c:set var="nDraft" value="0"/>
        <c:set var="nPublished" value="0"/>
        <c:set var="nDeleted" value="0"/>
        <c:forEach var="s" items="${stories}">
            <c:set var="totalChaptersCount" value="${totalChaptersCount + s.chapterCount}"/>
            <c:set var="totalViewsCount" value="${totalViewsCount + s.viewCount}"/>
            <c:choose>
                <c:when test="${s.status eq 'DRAFT'}"><c:set var="nDraft" value="${nDraft + 1}"/></c:when>
                <c:when test="${s.status eq 'DELETED'}"><c:set var="nDeleted" value="${nDeleted + 1}"/></c:when>
                <c:otherwise><c:set var="nPublished" value="${nPublished + 1}"/></c:otherwise>
            </c:choose>
        </c:forEach>

        <%-- Khối thống kê Studio của Tác giả --%>
        <div class="bookshelf-hero-stats" style="margin-bottom: 20px;">
            <div class="bs-stat-card">
                <div class="bs-stat-icon">📚</div>
                <div>
                    <div class="bs-stat-val">${totalStoriesCount}</div>
                    <div class="bs-stat-lbl">Tổng tác phẩm</div>
                </div>
            </div>
            <div class="bs-stat-card">
                <div class="bs-stat-icon">📄</div>
                <div>
                    <div class="bs-stat-val">${totalChaptersCount}</div>
                    <div class="bs-stat-lbl">Chương đã xuất bản</div>
                </div>
            </div>
            <div class="bs-stat-card">
                <div class="bs-stat-icon">👁️</div>
                <div>
                    <div class="bs-stat-val"><fmt:formatNumber pattern="#,##0" value="${totalViewsCount}"/></div>
                    <div class="bs-stat-lbl">Lượt đọc tích luỹ</div>
                </div>
            </div>
            <div class="bs-stat-card">
                <div class="bs-stat-icon">📝</div>
                <div>
                    <div class="bs-stat-val">${nDraft}</div>
                    <div class="bs-stat-lbl">Bản nháp đang viết</div>
                </div>
            </div>
        </div>

        <%-- Thanh công cụ: Lọc trạng thái & Tìm kiếm nhanh --%>
        <div class="bookshelf-toolbar" style="margin-bottom: 16px;">
            <div class="bookshelf-filters" id="mine-status-filters">
                <button type="button" class="bs-filter-chip is-active" data-status="all">
                    Tất cả <span class="bs-chip-count">${totalStoriesCount}</span>
                </button>
                <button type="button" class="bs-filter-chip" data-status="PUBLISHED">
                    🌟 Công khai <span class="bs-chip-count">${nPublished}</span>
                </button>
                <button type="button" class="bs-filter-chip" data-status="DRAFT">
                    📝 Bản nháp <span class="bs-chip-count">${nDraft}</span>
                </button>
                <c:if test="${nDeleted gt 0}">
                    <button type="button" class="bs-filter-chip" data-status="DELETED">
                        🗑️ Đã gỡ <span class="bs-chip-count">${nDeleted}</span>
                    </button>
                </c:if>
            </div>

            <div class="bookshelf-tools-right">
                <div class="bs-search-box">
                    <span class="bs-search-icon">🔍</span>
                    <input type="text" id="mine-search-input" placeholder="Lọc theo tên truyện…">
                    <button type="button" class="bs-search-clear" id="mine-search-clear">&times;</button>
                </div>
            </div>
        </div>

        <div class="story-rows" id="mine-story-list">
            <c:forEach var="story" items="${stories}">
                <c:set var="srStory" value="${story}"/>
                <c:set var="srMeta">
                    <c:choose>
                        <c:when test="${story.status eq 'DRAFT'}">📝 Bản nháp — chỉ mình bạn thấy</c:when>
                        <c:when test="${story.status eq 'DELETED'}">🗑️ Đã gỡ khỏi kho truyện</c:when>
                        <c:otherwise>🌟 Đang công khai</c:otherwise>
                    </c:choose>
                </c:set>

                <div class="mine-row"
                     data-status="${story.status}"
                     data-title="${fn:toLowerCase(story.title)}">

                    <%@ include file="/WEB-INF/views/_partials/_story-row.jsp" %>

                    <div class="mine-actions">
                        <c:if test="${googleDriveEnabled}">
                            <button type="button" class="btn btn-ghost btn-sm btn-drive-backup"
                                    data-story-id="${story.id}"
                                    data-story-title="<c:out value='${story.title}'/>"
                                    title="Sao lưu toàn bộ chương lên Google Drive cá nhân">
                                ☁️ Sao lưu Drive</button>
                        </c:if>
                        <a class="btn btn-ghost btn-sm"
                           href="${pageContext.request.contextPath}/story?action=stats&amp;id=${story.id}"
                           title="Xem biểu đồ lượt đọc và phễu giữ chân độc giả">
                            📊 Phễu</a>
                        <a class="btn btn-primary btn-sm"
                           href="${pageContext.request.contextPath}/chapter?action=create&amp;storyId=${story.id}">
                            + Chương</a>
                        <a class="btn btn-ghost btn-sm"
                           href="${pageContext.request.contextPath}/story?action=edit&amp;id=${story.id}">
                            Sửa</a>

                        <form method="post" style="display:inline"
                              action="${pageContext.request.contextPath}/story"
                              data-confirm="Gỡ truyện này khỏi kho truyện?"
                              data-confirm-title="Xác nhận gỡ truyện"
                              data-confirm-ok="Đồng ý gỡ">
                            <input type="hidden" name="_csrf" value="${csrfToken}">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="id" value="${story.id}">
                            <button type="submit" class="btn btn-danger btn-sm">Gỡ</button>
                        </form>
                    </div>
                </div>
            </c:forEach>
        </div>

        <div id="mine-empty-notice" style="display:none; padding:32px 10px; text-align:center; color:var(--text-mut);">
            Không tìm thấy truyện nào khớp với bộ lọc.
        </div>

        <script>
        (function() {
            var filterStatus = 'all';
            var searchKw = '';
            var items = document.querySelectorAll('#mine-story-list .mine-row');
            var emptyNotice = document.getElementById('mine-empty-notice');
            var searchInput = document.getElementById('mine-search-input');
            var searchClear = document.getElementById('mine-search-clear');

            function applyMineFilter() {
                var visibleCount = 0;
                items.forEach(function(el) {
                    var st = el.getAttribute('data-status') || '';
                    var title = el.getAttribute('data-title') || '';
                    var matchStatus = (filterStatus === 'all') || (st === filterStatus);
                    var matchSearch = (!searchKw) || (title.indexOf(searchKw) !== -1);
                    if (matchStatus && matchSearch) {
                        el.style.display = '';
                        visibleCount++;
                    } else {
                        el.style.display = 'none';
                    }
                });
                if (emptyNotice) {
                    emptyNotice.style.display = (visibleCount === 0 && items.length > 0) ? 'block' : 'none';
                }
            }

            document.querySelectorAll('#mine-status-filters .bs-filter-chip').forEach(function(btn) {
                btn.addEventListener('click', function() {
                    document.querySelectorAll('#mine-status-filters .bs-filter-chip').forEach(function(b) {
                        b.classList.remove('is-active');
                    });
                    this.classList.add('is-active');
                    filterStatus = this.getAttribute('data-status');
                    applyMineFilter();
                });
            });

            if (searchInput) {
                searchInput.addEventListener('input', function() {
                    searchKw = (this.value || '').trim().toLowerCase();
                    if (searchClear) searchClear.style.display = searchKw ? 'block' : 'none';
                    applyMineFilter();
                });
            }

            if (searchClear && searchInput) {
                searchClear.addEventListener('click', function() {
                    searchInput.value = '';
                    searchKw = '';
                    searchClear.style.display = 'none';
                    applyMineFilter();
                });
            }
        })();
        </script>
    </c:when>

    <c:otherwise>
        <c:set var="emIcon"  value="✍️"/>
        <c:set var="emTitle" value="Bạn chưa đăng truyện nào"/>
        <c:set var="emText"  value="Bắt đầu viết câu chuyện đầu tiên. Có thể lưu nháp trước, khi nào ưng ý mới công khai."/>
        <c:set var="emUrl"   value="/story?action=create"/>
        <c:set var="emBtn"   value="Đăng truyện đầu tiên"/>
        <%@ include file="/WEB-INF/views/_partials/_empty.jsp" %>
    </c:otherwise>
</c:choose>

<c:if test="${googleDriveEnabled}">
    <script>
        window.APP_CONTEXT = '${pageContext.request.contextPath}';
        window.GOOGLE_CLIENT_ID = '<c:out value="${googleClientId}"/>';
        window.CSRF_TOKEN = '<c:out value="${csrfToken}"/>';
    </script>
    <script src="${pageContext.request.contextPath}/assets/js/drive-backup.js" defer></script>
</c:if>
