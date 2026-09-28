<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
================================================================================
  admin/reports.jsp — MẢNH NỘI DUNG: Xử lý báo cáo vi phạm      TRANG 30
================================================================================
  TẦNG: views/ · layout: admin

  Nhận: reports (List<Report>) · status (bộ lọc hiện tại) · pending (số chờ)
================================================================================
--%>
<h1>Xử lý báo cáo</h1>
<p>Báo cáo do người đọc gửi từ trang truyện hoặc dưới bình luận.
   <b>Đánh dấu ở đây không tự gỡ gì cả</b> — gỡ truyện làm ở
   <a href="${pageContext.request.contextPath}/admin/story">Quản trị truyện</a>,
   ẩn bình luận làm ở
   <a href="${pageContext.request.contextPath}/admin/comment">Quản trị bình luận</a>.
   Tách ra để một cú bấm nhầm không xoá mất nội dung của người khác.</p>

<div class="tag-row" style="margin-bottom:20px">
    <a class="tag ${empty status ? 'is-on' : ''}"
       href="${pageContext.request.contextPath}/admin/report">Tất cả</a>
    <a class="tag ${status eq 'PENDING' ? 'is-on' : ''}"
       href="${pageContext.request.contextPath}/admin/report?status=PENDING">
        Chờ xử lý<c:if test="${pending gt 0}"> (${pending})</c:if></a>
    <a class="tag ${status eq 'RESOLVED' ? 'is-on' : ''}"
       href="${pageContext.request.contextPath}/admin/report?status=RESOLVED">Đã xử lý</a>
    <a class="tag ${status eq 'DISMISSED' ? 'is-on' : ''}"
       href="${pageContext.request.contextPath}/admin/report?status=DISMISSED">Bỏ qua</a>
</div>

<%--
  Lọc theo LOẠI VI PHẠM (ISSUE-025). Giữ nguyên ?status đang chọn để hai bộ
  lọc chồng được lên nhau — bỏ status đi thì mỗi lần đổi loại là mất bộ lọc kia.
--%>
<div class="tag-row" style="margin-bottom:20px">
    <span class="report-filter-label">Loại:</span>
    <a class="tag ${empty category ? 'is-on' : ''}"
       href="${pageContext.request.contextPath}/admin/report?status=${status}">Tất cả</a>
    <%-- Lặp thẳng cả 8 loại từ map. LinkedHashMap nên thứ tự đúng như khai
         báo trong Report.CATEGORIES — nặng trước, "Khác" cuối. --%>
    <c:forEach var="e" items="${reportCategoryLabels}">
        <a class="tag ${category eq e.key ? 'is-on' : ''} ${reportSevereCategories.contains(e.key) ? 'tag-severe' : ''}"
           href="${pageContext.request.contextPath}/admin/report?status=${status}&amp;category=${e.key}">
            ${e.value}</a>
    </c:forEach>
</div>

<%-- Ô TÌM KIẾM NHANH BÁO CÁO --%>
<div style="margin-bottom:18px; display:flex; align-items:center; gap:12px; flex-wrap:wrap;">
    <input type="text" id="reportQuickFilter"
           placeholder="🔍 Lọc nhanh theo nội dung, lý do hoặc người báo cáo..."
           style="max-width:400px; width:100%; padding:8px 14px; border-radius:var(--r-sm); border:1px solid var(--ink-700); background:var(--ink-900); color:var(--text); font-size:.9rem;">
    <span id="reportFilterCount" style="font-size:.84rem; color:var(--text-mut);"></span>
</div>

<c:choose>
    <c:when test="${not empty reports}">
        <div class="report-list">
            <c:forEach var="r" items="${reports}">
                <div class="report-item ${r.pending ? 'report-pending' : ''}"
                     data-search="<c:out value='${r.targetTitle} ${r.reason} ${r.reporterName} ${r.categoryLabel}'/>">

                    <div class="report-head">
                        <span class="pill ${r.story ? 'pill-muted' : 'pill-warn'}">
                            ${r.story ? '📚 Truyện' : '💬 Bình luận'}
                        </span>
                        <span class="pill ${r.pending ? 'pill-danger'
                                          : (r.status eq 'RESOLVED' ? 'pill-ok' : 'pill-muted')}">
                            ${r.statusLabel}
                        </span>
                        <%-- Loại nặng tô đỏ: mỗi giờ chậm là thêm người nhìn thấy. --%>
                        <span class="pill report-cat ${r.severe ? 'pill-severe' : 'pill-muted'}">
                            <c:if test="${r.severe}">🔴 </c:if>${r.categoryLabel}
                        </span>
                        <c:if test="${r.hasEvidence}">
                            <span class="pill pill-muted">📎 ${fn:length(r.evidences)} ảnh</span>
                        </c:if>
                        <span class="report-time">
                            <c:if test="${not empty r.createdAt}">
                                ${r.createdAt.dayOfMonth}/${r.createdAt.monthValue}/${r.createdAt.year}
                            </c:if>
                        </span>
                    </div>

                    <p class="report-target">
                        <b>Nội dung bị báo cáo:</b>
                        <c:choose>
                            <%-- Nội dung đã bị xoá hẳn: cả hai LEFT JOIN đều hụt
                                 nên storyId = 0. Hiện chữ thay vì một link chết. --%>
                            <c:when test="${r.targetGone}">
                                <span class="quote is-gone">Nội dung không còn tồn tại</span>
                            </c:when>

                            <c:when test="${r.story}">
                                <a href="${pageContext.request.contextPath}/story?action=detail&amp;id=${r.storyId}">
                                    <c:out value="${r.targetTitle}"/></a>
                            </c:when>

                            <c:otherwise>
                                <%-- Bình luận: mở đúng truyện chứa nó rồi nhảy thẳng
                                     tới khối bình luận. storyId lấy từ
                                     COALESCE(s.id, c.story_id) — xem ISSUE-025.
                                     Chỉ có 80 ký tự đầu do câu SQL cắt bằng LEFT(). --%>
                                <a href="${pageContext.request.contextPath}/story?action=detail&amp;id=${r.storyId}#comment-${r.targetId}">
                                    <span class="quote"><c:out value="${r.targetTitle}"/>…</span>
                                </a>
                            </c:otherwise>
                        </c:choose>
                    </p>

                    <p class="report-reason">
                        <b>Lý do:</b> <c:out value="${r.reason}"/>
                    </p>

                    <%--
                      Ảnh bằng chứng. Đường dẫn đi qua /uploads/ như mọi ảnh
                      khác, nhưng UploadedFileServlet chặn 403 với người không
                      phải admin vì nó nằm trong evidence/ — xem ISSUE-025.
                    --%>
                    <c:if test="${r.hasEvidence}">
                        <div class="report-evidence">
                            <c:forEach var="ev" items="${r.evidences}">
                                <a href="${pageContext.request.contextPath}/uploads/${ev.filePath}"
                                   target="_blank" rel="noopener"
                                   title="Mở ảnh gốc (${ev.sizeLabel})">
                                    <img src="${pageContext.request.contextPath}/uploads/${ev.filePath}"
                                         alt="Ảnh bằng chứng của báo cáo #${r.id}"
                                         class="report-evidence-thumb" loading="lazy">
                                </a>
                            </c:forEach>
                        </div>
                    </c:if>

                    <p class="report-by">
                        Người báo cáo: <c:out value="${r.reporterName}"/>
                    </p>

                    <%-- Đã xử lý rồi thì không hiện nút nữa. --%>
                    <c:if test="${r.pending}">
                        <div class="report-actions">
                            <c:choose>
                                <c:when test="${r.story}">
                                    <form method="post" style="display:inline"
                                          action="${pageContext.request.contextPath}/admin/report">
                                        <input type="hidden" name="_csrf" value="${csrfToken}">
                                        <input type="hidden" name="action" value="resolve_delete">
                                        <input type="hidden" name="id" value="${r.id}">
                                        <input type="hidden" name="targetId" value="${r.targetId}">
                                        <input type="hidden" name="status" value="${status}">
                                        <button type="submit" class="btn btn-danger btn-sm"
                                                onclick="return confirm('Bạn chắc chắn muốn gỡ truyện này và đóng báo cáo?')">
                                            🚫 Gỡ truyện & Đóng báo cáo</button>
                                    </form>
                                </c:when>
                                <c:otherwise>
                                    <form method="post" style="display:inline"
                                          action="${pageContext.request.contextPath}/admin/report">
                                        <input type="hidden" name="_csrf" value="${csrfToken}">
                                        <input type="hidden" name="action" value="resolve_hide">
                                        <input type="hidden" name="id" value="${r.id}">
                                        <input type="hidden" name="targetId" value="${r.targetId}">
                                        <input type="hidden" name="status" value="${status}">
                                        <button type="submit" class="btn btn-danger btn-sm"
                                                onclick="return confirm('Bạn chắc chắn muốn ẩn bình luận này và đóng báo cáo?')">
                                            🚫 Ẩn bình luận & Đóng báo cáo</button>
                                    </form>
                                </c:otherwise>
                            </c:choose>

                            <form method="post" style="display:inline"
                                  action="${pageContext.request.contextPath}/admin/report">
                                <input type="hidden" name="_csrf" value="${csrfToken}">
                                <input type="hidden" name="action" value="resolve">
                                <input type="hidden" name="id" value="${r.id}">
                                <input type="hidden" name="status" value="${status}">
                                <button type="submit" class="btn btn-primary btn-sm">
                                    ✓ Đã xử lý</button>
                            </form>
                            <form method="post" style="display:inline"
                                  action="${pageContext.request.contextPath}/admin/report">
                                <input type="hidden" name="_csrf" value="${csrfToken}">
                                <input type="hidden" name="action" value="dismiss">
                                <input type="hidden" name="id" value="${r.id}">
                                <input type="hidden" name="status" value="${status}">
                                <button type="submit" class="btn btn-ghost btn-sm">
                                    Bỏ qua</button>
                            </form>
                        </div>
                    </c:if>
                </div>
            </c:forEach>
        </div>
    </c:when>

    <c:otherwise>
        <c:set var="emIcon"  value="🛡️"/>
        <c:set var="emTitle" value="Không có báo cáo nào"/>
        <c:set var="emText"  value="Cộng đồng đang yên ổn."/>
        <%@ include file="/WEB-INF/views/_partials/_empty.jsp" %>
    </c:otherwise>
</c:choose>

<script>
(function () {
    var input = document.getElementById('reportQuickFilter');
    var countEl = document.getElementById('reportFilterCount');
    var items = document.querySelectorAll('.report-item');
    if (!input || !items.length) return;

    input.addEventListener('input', function () {
        var q = this.value.trim().toLowerCase();
        var visible = 0;
        for (var i = 0; i < items.length; i++) {
            var item = items[i];
            var text = (item.getAttribute('data-search') || item.textContent).toLowerCase();
            var match = !q || text.indexOf(q) !== -1;
            item.style.display = match ? '' : 'none';
            if (match) visible++;
        }
        if (countEl) {
            countEl.textContent = q ? ('Hiển thị ' + visible + '/' + items.length + ' báo cáo') : '';
        }
    });
})();
</script>

