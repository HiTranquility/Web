<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
  user/bookmarks.jsp — MẢNH nội dung: Tủ sách độc giả              CASE 08
  Nhận: bookmarks (List<Bookmark>) · marks (List<ChapterBookmark>)
        tab = "stories" | "marks"
--%>
<div class="section-head">
    <h2>Tủ sách cá nhân</h2>
    <span class="more">Không gian lưu trữ &amp; theo dõi tiến độ đọc</span>
</div>

<%-- Thống kê tổng quan Tủ sách (Hero Stats) --%>
<c:set var="cntTotal" value="${fn:length(bookmarks)}"/>
<c:set var="cntReading" value="0"/>
<c:set var="cntCompleted" value="0"/>
<c:set var="cntUnread" value="0"/>
<c:forEach var="b" items="${bookmarks}">
    <c:choose>
        <c:when test="${b.readingStatus eq 'completed'}">
            <c:set var="cntCompleted" value="${cntCompleted + 1}"/>
        </c:when>
        <c:when test="${b.readingStatus eq 'reading'}">
            <c:set var="cntReading" value="${cntReading + 1}"/>
        </c:when>
        <c:otherwise>
            <c:set var="cntUnread" value="${cntUnread + 1}"/>
        </c:otherwise>
    </c:choose>
</c:forEach>

<div class="bookshelf-hero-stats">
    <div class="bs-stat-card">
        <div class="bs-stat-icon">📚</div>
        <div>
            <div class="bs-stat-val" id="stat-total">${cntTotal}</div>
            <div class="bs-stat-lbl">Truyện trong tủ</div>
        </div>
    </div>
    <div class="bs-stat-card">
        <div class="bs-stat-icon">📖</div>
        <div>
            <div class="bs-stat-val" id="stat-reading">${cntReading}</div>
            <div class="bs-stat-lbl">Đang đọc dở</div>
        </div>
    </div>
    <div class="bs-stat-card">
        <div class="bs-stat-icon">✅</div>
        <div>
            <div class="bs-stat-val" id="stat-completed">${cntCompleted}</div>
            <div class="bs-stat-lbl">Đã đọc xong</div>
        </div>
    </div>
    <div class="bs-stat-card">
        <div class="bs-stat-icon">🔖</div>
        <div>
            <div class="bs-stat-val" id="stat-marks">${fn:length(marks)}</div>
            <div class="bs-stat-lbl">Đoạn đánh dấu</div>
        </div>
    </div>
</div>

<div class="tab-bar bookshelf-tabs">
    <a class="tab-btn ${tab ne 'marks' ? 'is-on' : ''}"
       href="${pageContext.request.contextPath}/bookmark?action=list">
        📚 Truyện đã lưu <span class="tab-count" id="tab-count-stories">${fn:length(bookmarks)}</span></a>
    <a class="tab-btn ${tab eq 'marks' ? 'is-on' : ''}"
       href="${pageContext.request.contextPath}/bookmark?action=list&amp;tab=marks">
        🔖 Đoạn đã đánh dấu <span class="tab-count" id="tab-count-marks">${fn:length(marks)}</span></a>
</div>

<c:choose>
<%-- =========================================================== ĐOẠN ĐÃ ĐÁNH DẤU --%>
<c:when test="${tab eq 'marks'}">
    <c:choose>
        <c:when test="${not empty marks}">
            <%-- Thanh tìm kiếm nhanh đoạn trích --%>
            <div class="bookshelf-toolbar">
                <div class="bs-search-box" style="width:100%; max-width:320px;">
                    <span class="bs-search-icon">🔍</span>
                    <input type="text" id="bs-mark-search" placeholder="Lọc theo tên truyện hoặc đoạn trích…">
                    <button type="button" class="bs-search-clear" id="bs-mark-clear">&times;</button>
                </div>
            </div>

            <div class="bookmark-list" id="mark-list-container">
                <c:forEach var="m" items="${marks}">
                    <div class="bookmark-item mark-item"
                         data-id="${m.id}"
                         data-title="${fn:toLowerCase(m.storyTitle)}"
                         data-snippet="${fn:toLowerCase(m.snippet)}">
                        <div class="bm-cover">
                            <c:set var="cvUrl"     value="${m.coverUrl}"/>
                            <c:set var="cvAlt"     value="${m.storyTitle}"/>
                            <c:set var="cvInitial" value="${m.initial}"/>
                            <%@ include file="/WEB-INF/views/_partials/_cover.jsp" %>
                        </div>

                        <div class="bm-info">
                            <a class="bm-title"
                               href="${pageContext.request.contextPath}/story?action=detail&amp;id=${m.storyId}">
                                <c:out value="${m.storyTitle}"/></a>
                            <p class="bm-progress">
                                Chương ${m.chapterNo} · <c:out value="${m.chapterTitle}"/>
                                <c:if test="${m.paraNo gt 0}"> · ${m.unitLabel} ${m.paraNo}</c:if>
                            </p>
                            <c:if test="${m.comic and not empty m.pageUrl}">
                                <img class="mark-thumb" src="${pageContext.request.contextPath}${m.pageUrl}"
                                     alt="Trang ${m.paraNo}" loading="lazy">
                            </c:if>
                            <c:if test="${not empty m.snippet}">
                                <blockquote class="mark-snippet"><c:out value="${m.snippet}"/></blockquote>
                            </c:if>
                        </div>

                        <div class="bm-actions">
                            <a class="btn btn-primary btn-sm"
                               href="${pageContext.request.contextPath}/chapter?action=read&amp;id=${m.chapterId}${m.paraNo gt 0 ? '#doan-' : ''}${m.paraNo gt 0 ? m.paraNo : ''}">
                                Đọc ${m.unitLabel} này</a>
                            <form action="${pageContext.request.contextPath}/bookmark" method="post"
                                  class="form-ajax-unmark" style="display:inline">
                                <input type="hidden" name="_csrf" value="${csrfToken}">
                                <input type="hidden" name="action" value="unmark">
                                <input type="hidden" name="id" value="${m.id}">
                                <button type="submit" class="btn btn-ghost btn-sm">Bỏ đánh dấu</button>
                            </form>
                        </div>
                    </div>
                </c:forEach>
            </div>
            <div id="mark-empty-filter" style="display:none; padding:30px 10px; text-align:center; color:var(--text-mut);">
                Không tìm thấy đoạn đánh dấu nào khớp với từ khoá.
            </div>
        </c:when>
        <c:otherwise>
            <c:set var="emIcon"  value="🔖"/>
            <c:set var="emTitle" value="Chưa đánh dấu đoạn nào"/>
            <c:set var="emText"
                   value="Đang đọc gặp đoạn (hoặc trang truyện tranh) hay? Bấm 🔖 trên thanh công cụ (hoặc phím B) để đánh dấu chỗ đang ở đầu màn hình, rồi quay lại đây đọc lại bất cứ lúc nào."/>
            <c:set var="emUrl"   value="/history"/>
            <c:set var="emBtn"   value="Mở lịch sử đọc"/>
            <%@ include file="/WEB-INF/views/_partials/_empty.jsp" %>
        </c:otherwise>
    </c:choose>
</c:when>

<%-- =========================================================== TRUYỆN ĐÃ LƯU --%>
<c:otherwise>
    <c:choose>
        <c:when test="${not empty bookmarks}">
            <%-- Thanh công cụ: Lọc trạng thái, Tìm kiếm cục bộ và Chuyển chế độ xem --%>
            <div class="bookshelf-toolbar">
                <div class="bookshelf-filters" id="bs-status-filters">
                    <button type="button" class="bs-filter-chip is-active" data-status="all">
                        Tất cả <span class="bs-chip-count">${cntTotal}</span>
                    </button>
                    <button type="button" class="bs-filter-chip" data-status="reading">
                        📖 Đang đọc <span class="bs-chip-count">${cntReading}</span>
                    </button>
                    <button type="button" class="bs-filter-chip" data-status="unread">
                        ⏳ Chưa đọc <span class="bs-chip-count">${cntUnread}</span>
                    </button>
                    <button type="button" class="bs-filter-chip" data-status="completed">
                        ✅ Đã xong <span class="bs-chip-count">${cntCompleted}</span>
                    </button>
                </div>

                <div class="bookshelf-tools-right">
                    <div class="bs-search-box">
                        <span class="bs-search-icon">🔍</span>
                        <input type="text" id="bs-story-search" placeholder="Lọc theo tên truyện…">
                        <button type="button" class="bs-search-clear" id="bs-search-clear">&times;</button>
                    </div>

                    <div class="bs-view-toggle">
                        <button type="button" class="bs-view-btn is-active" id="btn-view-list" title="Chế độ danh sách">📋</button>
                        <button type="button" class="bs-view-btn" id="btn-view-grid" title="Chế độ lưới bìa">🔲</button>
                    </div>
                </div>
            </div>

            <div class="bookmark-list" id="story-list-container">
                <c:forEach var="b" items="${bookmarks}">
                    <div class="bookmark-item"
                         data-story-id="${b.storyId}"
                         data-status="${b.readingStatus}"
                         data-title="${fn:toLowerCase(b.storyTitle)}">
                        <div class="bm-cover">
                            <c:set var="cvUrl"     value="${b.coverUrl}"/>
                            <c:set var="cvAlt"     value="${b.storyTitle}"/>
                            <c:set var="cvInitial" value="${b.initial}"/>
                            <%@ include file="/WEB-INF/views/_partials/_cover.jsp" %>
                        </div>

                        <div class="bm-info">
                            <a class="bm-title"
                               href="${pageContext.request.contextPath}/story?action=detail&amp;id=${b.storyId}">
                                <c:out value="${b.storyTitle}"/></a>

                            <p class="bm-progress">
                                <c:choose>
                                    <c:when test="${b.started}">
                                        Đang đọc chương <b>${b.lastChapterNo}</b> / ${b.totalChapters}
                                    </c:when>
                                    <c:otherwise>
                                        Chưa đọc &middot; ${b.totalChapters} chương
                                    </c:otherwise>
                                </c:choose>
                            </p>

                            <%-- Thanh tiến độ trực quan với % và badge trạng thái --%>
                            <div class="bm-progress-row">
                                <div class="bm-progress-bar" title="Tiến độ: ${b.progressPercent}%">
                                    <div class="bm-progress-fill ${b.readingStatus}" style="width: ${b.progressPercent}%;"></div>
                                </div>
                                <span class="bm-progress-pct">${b.progressPercent}%</span>
                                <span class="bm-status-tag ${b.readingStatus}">
                                    <c:out value="${b.statusBadge}"/>
                                </span>
                            </div>
                        </div>

                        <div class="bm-actions">
                            <c:choose>
                                <c:when test="${b.started}">
                                    <a class="btn btn-primary btn-sm"
                                       href="${pageContext.request.contextPath}/chapter?action=read&amp;id=${b.lastChapterId}">
                                        Đọc tiếp</a>
                                </c:when>
                                <c:otherwise>
                                    <a class="btn btn-primary btn-sm"
                                       href="${pageContext.request.contextPath}/story?action=detail&amp;id=${b.storyId}">
                                        Bắt đầu đọc</a>
                                </c:otherwise>
                            </c:choose>

                            <form action="${pageContext.request.contextPath}/bookmark" method="post"
                                  class="form-ajax-unbookmark" style="display:inline">
                                <input type="hidden" name="_csrf" value="${csrfToken}">
                                <input type="hidden" name="action" value="remove">
                                <input type="hidden" name="storyId" value="${b.storyId}">
                                <input type="hidden" name="from" value="list">
                                <button type="submit" class="btn btn-ghost btn-sm">Bỏ lưu</button>
                            </form>
                        </div>
                    </div>
                </c:forEach>
            </div>
            <div id="story-empty-filter" style="display:none; padding:36px 12px; text-align:center; color:var(--text-mut);">
                Không tìm thấy tác phẩm nào phù hợp với bộ lọc hiện tại.
            </div>
        </c:when>
        <c:otherwise>
            <c:set var="emIcon"  value="📚"/>
            <c:set var="emTitle" value="Chưa lưu truyện nào"/>
            <c:set var="emText"
                   value="Bấm ☆ Lưu truyện ở trang truyện (hoặc 📚 khi đang đọc) để cất vào tủ sách. Vị trí đọc thì hệ thống tự nhớ, không cần lưu."/>
            <c:set var="emUrl"   value="/story?action=list"/>
            <c:set var="emBtn"   value="Tìm truyện để đọc"/>
            <%@ include file="/WEB-INF/views/_partials/_empty.jsp" %>
        </c:otherwise>
    </c:choose>
</c:otherwise>
</c:choose>

<%-- Script tương tác cho Tủ sách --%>
<script>
(function() {
    // 1. Chế độ xem Lưới vs Danh sách
    var btnList = document.getElementById('btn-view-list');
    var btnGrid = document.getElementById('btn-view-grid');
    var listContainer = document.getElementById('story-list-container');
    if (btnList && btnGrid && listContainer) {
        var savedMode = localStorage.getItem('truyen_bookshelf_view') || 'list';
        if (savedMode === 'grid') {
            listContainer.classList.add('is-grid');
            btnGrid.classList.add('is-active');
            btnList.classList.remove('is-active');
        }
        btnList.addEventListener('click', function() {
            listContainer.classList.remove('is-grid');
            btnList.classList.add('is-active');
            btnGrid.classList.remove('is-active');
            localStorage.setItem('truyen_bookshelf_view', 'list');
        });
        btnGrid.addEventListener('click', function() {
            listContainer.classList.add('is-grid');
            btnGrid.classList.add('is-active');
            btnList.classList.remove('is-active');
            localStorage.setItem('truyen_bookshelf_view', 'grid');
        });
    }

    // 2. Lọc trạng thái & tìm kiếm cục bộ truyện đã lưu
    var statusFilter = 'all';
    var searchKeyword = '';
    var items = document.querySelectorAll('#story-list-container .bookmark-item');
    var emptyNotice = document.getElementById('story-empty-filter');
    var searchInput = document.getElementById('bs-story-search');
    var searchClear = document.getElementById('bs-search-clear');

    function applyFilter() {
        var visibleCount = 0;
        items.forEach(function(item) {
            var st = item.getAttribute('data-status') || '';
            var title = item.getAttribute('data-title') || '';
            var matchStatus = (statusFilter === 'all') || (st === statusFilter);
            var matchSearch = (!searchKeyword) || (title.indexOf(searchKeyword) !== -1);
            if (matchStatus && matchSearch) {
                item.style.display = '';
                visibleCount++;
            } else {
                item.style.display = 'none';
            }
        });
        if (emptyNotice) {
            emptyNotice.style.display = (visibleCount === 0 && items.length > 0) ? 'block' : 'none';
        }
    }

    document.querySelectorAll('#bs-status-filters .bs-filter-chip').forEach(function(btn) {
        btn.addEventListener('click', function() {
            document.querySelectorAll('#bs-status-filters .bs-filter-chip').forEach(function(b) {
                b.classList.remove('is-active');
            });
            this.classList.add('is-active');
            statusFilter = this.getAttribute('data-status');
            applyFilter();
        });
    });

    if (searchInput) {
        searchInput.addEventListener('input', function() {
            searchKeyword = (this.value || '').trim().toLowerCase();
            if (searchClear) searchClear.style.display = searchKeyword ? 'block' : 'none';
            applyFilter();
        });
    }
    if (searchClear && searchInput) {
        searchClear.addEventListener('click', function() {
            searchInput.value = '';
            searchKeyword = '';
            searchClear.style.display = 'none';
            applyFilter();
        });
    }

    // 3. Lọc tab đoạn trích
    var markSearch = document.getElementById('bs-mark-search');
    var markClear = document.getElementById('bs-mark-clear');
    var markItems = document.querySelectorAll('#mark-list-container .bookmark-item');
    var markEmpty = document.getElementById('mark-empty-filter');
    if (markSearch) {
        markSearch.addEventListener('input', function() {
            var q = (this.value || '').trim().toLowerCase();
            if (markClear) markClear.style.display = q ? 'block' : 'none';
            var visible = 0;
            markItems.forEach(function(item) {
                var t = item.getAttribute('data-title') || '';
                var s = item.getAttribute('data-snippet') || '';
                if (!q || t.indexOf(q) !== -1 || s.indexOf(q) !== -1) {
                    item.style.display = '';
                    visible++;
                } else {
                    item.style.display = 'none';
                }
            });
            if (markEmpty) markEmpty.style.display = (visible === 0 && markItems.length > 0) ? 'block' : 'none';
        });
    }
    if (markClear && markSearch) {
        markClear.addEventListener('click', function() {
            markSearch.value = '';
            markClear.style.display = 'none';
            markItems.forEach(function(item) { item.style.display = ''; });
            if (markEmpty) markEmpty.style.display = 'none';
        });
    }

    // 4. AJAX Bỏ lưu truyện với hiệu ứng mượt mà
    document.querySelectorAll('.form-ajax-unbookmark').forEach(function(form) {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            var item = this.closest('.bookmark-item');
            var formData = new FormData(this);
            var params = new URLSearchParams(formData);

            fetch(this.action, {
                method: 'POST',
                headers: { 'X-Requested-With': 'XMLHttpRequest' },
                body: params
            })
            .then(function(res) { return res.json(); })
            .then(function(data) {
                if (data && data.success) {
                    if (item) {
                        item.classList.add('is-removing');
                        setTimeout(function() {
                            item.remove();
                            // Cập nhật lại số đếm
                            var tabCount = document.getElementById('tab-count-stories');
                            var statTotal = document.getElementById('stat-total');
                            var currentTotal = parseInt(tabCount ? tabCount.textContent : '0', 10);
                            if (currentTotal > 0) {
                                if (tabCount) tabCount.textContent = (currentTotal - 1);
                                if (statTotal) statTotal.textContent = (currentTotal - 1);
                            }
                        }, 250);
                    }
                }
            })
            .catch(function() {
                form.submit(); // Fallback gửi form truyền thống nếu lỗi fetch
            });
        });
    });

    // 5. AJAX Bỏ đánh dấu đoạn với hiệu ứng mượt mà
    document.querySelectorAll('.form-ajax-unmark').forEach(function(form) {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            var item = this.closest('.bookmark-item');
            var formData = new FormData(this);
            var params = new URLSearchParams(formData);

            fetch(this.action, {
                method: 'POST',
                headers: { 'X-Requested-With': 'XMLHttpRequest' },
                body: params
            })
            .then(function(res) { return res.json(); })
            .then(function(data) {
                if (data && data.success) {
                    if (item) {
                        item.classList.add('is-removing');
                        setTimeout(function() {
                            item.remove();
                            var tabMarks = document.getElementById('tab-count-marks');
                            var statMarks = document.getElementById('stat-marks');
                            var cur = parseInt(tabMarks ? tabMarks.textContent : '0', 10);
                            if (cur > 0) {
                                if (tabMarks) tabMarks.textContent = (cur - 1);
                                if (statMarks) statMarks.textContent = (cur - 1);
                            }
                        }, 250);
                    }
                }
            })
            .catch(function() {
                form.submit();
            });
        });
    });
})();
</script>
