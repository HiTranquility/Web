<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
================================================================================
  user/history.jsp — MẢNH NỘI DUNG: Lịch sử đọc                  TRANG 31
================================================================================
  TẦNG: views/ · layout: main
  Nhận: history (List<ReadHistory>) · totalRead (int)
================================================================================
--%>
<div class="section-head">
    <h2>Lịch sử đọc</h2>
    <span class="more">${totalRead} tác phẩm</span>
</div>

<c:choose>
    <c:when test="${not empty history}">
        <div class="bookshelf-toolbar" style="margin-bottom: 14px;">
            <p class="muted-note" style="margin: 0;">
                Tự động ghi lại khi mở truyện. Hiển thị <b>${fn:length(history)}</b> truyện gần nhất.
            </p>
            <div class="bs-search-box" style="margin-left: auto;">
                <span class="bs-search-icon">🔍</span>
                <input type="text" id="hist-search-input" placeholder="Lọc theo tên hoặc tác giả…">
                <button type="button" class="bs-search-clear" id="hist-search-clear">&times;</button>
            </div>
        </div>

        <div class="bookmark-list" id="hist-list-container">
            <c:forEach var="h" items="${history}">
                <div class="bookmark-item"
                     data-title="${fn:toLowerCase(h.storyTitle)}"
                     data-author="${fn:toLowerCase(h.authorName)}">
                    <div class="bm-cover">
                        <c:set var="cvUrl"     value="${h.coverUrl}"/>
                        <c:set var="cvAlt"     value="${h.storyTitle}"/>
                        <c:set var="cvInitial" value="${h.initial}"/>
                        <%@ include file="/WEB-INF/views/_partials/_cover.jsp" %>
                    </div>

                    <div class="bm-info">
                        <a class="bm-title"
                           href="${pageContext.request.contextPath}/story?action=detail&amp;id=${h.storyId}">
                            <c:out value="${h.storyTitle}"/></a>

                        <p class="bm-progress">
                            <b>${h.viewedLabel}</b>
                            &middot; <c:out value="${h.authorName}"/>
                            <c:if test="${h.viewTimes gt 1}">
                                &middot; đã mở ${h.viewTimes} lần
                            </c:if>
                        </p>

                        <p class="bm-progress">
                            <c:choose>
                                <c:when test="${h.resumable}">
                                    Đang đọc chương <b>${h.lastChapterNo}</b> / ${h.totalChapters}
                                </c:when>
                                <c:otherwise>
                                    ${h.totalChapters} chương &middot; chưa đọc chương nào
                                </c:otherwise>
                            </c:choose>
                        </p>

                        <%-- Thanh tiến độ đọc trực quan cho lịch sử --%>
                        <c:if test="${h.resumable}">
                            <div class="bm-progress-row">
                                <div class="bm-progress-bar" title="Tiến độ: ${h.progressPercent}%">
                                    <div class="bm-progress-fill ${h.progressPercent ge 100 ? 'completed' : 'reading'}"
                                         style="width: ${h.progressPercent}%;"></div>
                                </div>
                                <span class="bm-progress-pct">${h.progressPercent}%</span>
                                <span class="bm-status-tag ${h.progressPercent ge 100 ? 'completed' : 'reading'}">
                                    ${h.progressPercent ge 100 ? 'Đã xong' : 'Đang đọc'}
                                </span>
                            </div>
                        </c:if>
                    </div>

                    <div class="bm-actions">
                        <c:choose>
                            <c:when test="${h.resumable}">
                                <a class="btn btn-primary btn-sm"
                                   href="${pageContext.request.contextPath}/chapter?action=read&amp;id=${h.lastChapterId}">
                                    Đọc tiếp</a>
                            </c:when>
                            <c:otherwise>
                                <a class="btn btn-ghost btn-sm"
                                   href="${pageContext.request.contextPath}/story?action=detail&amp;id=${h.storyId}">
                                    Mở lại</a>
                            </c:otherwise>
                        </c:choose>

                        <form action="${pageContext.request.contextPath}/bookmark"
                              method="post" class="form-ajax-bookmark" style="display:inline">
                            <input type="hidden" name="_csrf" value="${csrfToken}">
                            <input type="hidden" name="action" value="add">
                            <input type="hidden" name="storyId" value="${h.storyId}">
                            <input type="hidden" name="from" value="history">
                            <button type="submit" class="btn btn-ghost btn-sm btn-bookmark-act">☆ Lưu</button>
                        </form>
                    </div>
                </div>
            </c:forEach>
        </div>

        <div id="hist-empty-filter" style="display:none; padding:30px 10px; text-align:center; color:var(--text-mut);">
            Không tìm thấy truyện phù hợp trong lịch sử đọc.
        </div>

        <%-- VÙNG NGUY HIỂM: XOÁ LỊCH SỬ ĐỌC --%>
        <details class="danger-zone" style="margin-top: 28px;">
            <summary>Xoá lịch sử đọc</summary>
            <div class="danger-body">
                <p class="muted">
                    Gỡ tên bạn khỏi toàn bộ lượt đọc đã ghi. Lượt xem của truyện
                    và bảng xếp hạng <b>giữ nguyên</b> — chỉ không còn gắn với
                    tài khoản của bạn nữa.
                    <b>Không khôi phục được.</b>
                    Truyện đã lưu ở mục <a
                       href="${pageContext.request.contextPath}/bookmark">Đã lưu</a>
                    không bị ảnh hưởng.
                </p>
                <form method="post"
                      action="${pageContext.request.contextPath}/history">
                    <input type="hidden" name="_csrf" value="${csrfToken}">
                    <input type="hidden" name="action" value="clear">
                    <button type="submit" class="btn btn-danger btn-sm"
                            data-confirm="Bạn có chắc chắn muốn xoá toàn bộ lịch sử đọc không? Hành động này không thể hoàn tác!"
                            data-confirm-title="Xác nhận xoá lịch sử"
                            data-confirm-ok="Đồng ý xoá">
                        Xoá toàn bộ lịch sử</button>
                </form>
            </div>
        </details>

        <script>
        (function() {
            var searchInput = document.getElementById('hist-search-input');
            var searchClear = document.getElementById('hist-search-clear');
            var items = document.querySelectorAll('#hist-list-container .bookmark-item');
            var emptyNotice = document.getElementById('hist-empty-filter');

            if (searchInput) {
                searchInput.addEventListener('input', function() {
                    var q = (this.value || '').trim().toLowerCase();
                    if (searchClear) searchClear.style.display = q ? 'block' : 'none';
                    var visible = 0;
                    items.forEach(function(item) {
                        var title = item.getAttribute('data-title') || '';
                        var author = item.getAttribute('data-author') || '';
                        if (!q || title.indexOf(q) !== -1 || author.indexOf(q) !== -1) {
                            item.style.display = '';
                            visible++;
                        } else {
                            item.style.display = 'none';
                        }
                    });
                    if (emptyNotice) {
                        emptyNotice.style.display = (visible === 0 && items.length > 0) ? 'block' : 'none';
                    }
                });
            }

            if (searchClear && searchInput) {
                searchClear.addEventListener('click', function() {
                    searchInput.value = '';
                    searchClear.style.display = 'none';
                    items.forEach(function(item) { item.style.display = ''; });
                    if (emptyNotice) emptyNotice.style.display = 'none';
                });
            }

            // AJAX Bookmark ngay từ lịch sử đọc
            document.querySelectorAll('.form-ajax-bookmark').forEach(function(form) {
                form.addEventListener('submit', function(e) {
                    e.preventDefault();
                    var btn = this.querySelector('.btn-bookmark-act');
                    var formData = new FormData(this);
                    var params = new URLSearchParams(formData);

                    fetch(this.action, {
                        method: 'POST',
                        headers: { 'X-Requested-With': 'XMLHttpRequest' },
                        body: params
                    })
                    .then(function(res) { return res.json(); })
                    .then(function(data) {
                        if (data && data.success && btn) {
                            btn.textContent = '✓ Đã lưu';
                            btn.classList.remove('btn-ghost');
                            btn.classList.add('btn-primary');
                            btn.disabled = true;
                        }
                    })
                    .catch(function() {
                        form.submit();
                    });
                });
            });
        })();
        </script>
    </c:when>

    <c:otherwise>
        <c:set var="emIcon"  value="🕘"/>
        <c:set var="emTitle" value="Chưa có lịch sử đọc"/>
        <c:set var="emText"
               value="Mở một truyện bất kỳ là nó tự xuất hiện ở đây. Khác với mục Đã lưu, bạn không phải bấm gì cả."/>
        <c:set var="emUrl"   value="/story?action=list"/>
        <c:set var="emBtn"   value="Tìm truyện để đọc"/>
        <%@ include file="/WEB-INF/views/_partials/_empty.jsp" %>
    </c:otherwise>
</c:choose>
