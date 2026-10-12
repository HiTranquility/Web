<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
================================================================================
  user/following.jsp — MẢNH NỘI DUNG: Tác giả đang theo dõi            TRANG 17
================================================================================
--%>
<div class="section-head">
    <div>
        <h2>👥 Đang theo dõi</h2>
        <span class="more">Nhận thông báo tự động mỗi khi tác giả yêu thích xuất bản chương mới</span>
    </div>
    <span class="more" id="following-count-badge"><b>${empty following ? 0 : following.size()}</b> tác giả</span>
</div>

<c:choose>
    <c:when test="${not empty following}">
        <div class="bookshelf-toolbar" style="margin-bottom: 16px;">
            <p class="muted-note" style="margin: 0;">
                Tác giả bạn theo dõi khi đăng chương mới sẽ xuất hiện trên chuông
                <a href="${pageContext.request.contextPath}/notification">thông báo</a>.
            </p>
            <div class="bs-search-box" style="margin-left: auto;">
                <span class="bs-search-icon">🔍</span>
                <input type="text" id="follow-search-input" placeholder="Tìm theo tên tác giả…">
                <button type="button" class="bs-search-clear" id="follow-search-clear">&times;</button>
            </div>
        </div>

        <div class="author-grid" id="following-grid" style="display:grid; grid-template-columns:repeat(auto-fill, minmax(280px, 1fr)); gap:14px;">
            <c:forEach var="f" items="${following}">
                <div class="author-card-box"
                     data-author-id="${f.authorId}"
                     data-name="${fn:toLowerCase(f.authorName)}"
                     data-user="${fn:toLowerCase(f.authorUsername)}"
                     style="background:var(--ink-850); border:1px solid var(--ink-700); border-radius:var(--r-md); padding:16px; display:flex; align-items:center; gap:14px; transition:border-color .18s var(--ease), transform .18s var(--ease);">
                    <a class="author-avatar"
                       href="${pageContext.request.contextPath}/user?action=profile&amp;id=${f.authorId}"
                       style="width:52px; height:52px; border-radius:50%; background:linear-gradient(135deg, var(--ember), #d97706); display:flex; align-items:center; justify-content:center; color:#fff; font-size:1.3rem; font-weight:700; text-decoration:none; flex-shrink:0; box-shadow:0 3px 8px rgba(0,0,0,0.15);">
                        ${f.initial}
                    </a>

                    <div class="author-body" style="flex:1; min-width:0;">
                        <a class="author-name"
                           href="${pageContext.request.contextPath}/user?action=profile&amp;id=${f.authorId}"
                           style="font-weight:700; font-size:1rem; color:var(--text); text-decoration:none; display:block; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">
                            <c:out value="${f.authorName}"/>
                        </a>
                        <p class="author-meta" style="margin:2px 0 0; font-size:0.8rem; color:var(--text-mut);">
                            @<c:out value="${f.authorUsername}"/> &middot; 📚 ${f.authorStoryCount} truyện
                        </p>
                    </div>

                    <div class="author-actions" style="flex-shrink:0;">
                        <form method="post" class="form-ajax-unfollow"
                              action="${pageContext.request.contextPath}/follow" style="margin:0;">
                            <input type="hidden" name="_csrf" value="${csrfToken}">
                            <input type="hidden" name="do" value="unfollow">
                            <input type="hidden" name="authorId" value="${f.authorId}">
                            <input type="hidden" name="back" value="list">
                            <button type="submit" class="btn btn-ghost btn-sm" title="Hủy theo dõi tác giả này">
                                Bỏ theo dõi
                            </button>
                        </form>
                    </div>
                </div>
            </c:forEach>
        </div>

        <div id="follow-empty-notice" style="display:none; padding:32px 10px; text-align:center; color:var(--text-mut);">
            Không tìm thấy tác giả nào phù hợp với từ khoá tìm kiếm.
        </div>

        <script>
        (function() {
            var searchInput = document.getElementById('follow-search-input');
            var searchClear = document.getElementById('follow-search-clear');
            var items = document.querySelectorAll('#following-grid .author-card-box');
            var emptyNotice = document.getElementById('follow-empty-notice');

            if (searchInput) {
                searchInput.addEventListener('input', function() {
                    var q = (this.value || '').trim().toLowerCase();
                    if (searchClear) searchClear.style.display = q ? 'block' : 'none';
                    var visible = 0;
                    items.forEach(function(el) {
                        var name = el.getAttribute('data-name') || '';
                        var user = el.getAttribute('data-user') || '';
                        if (!q || name.indexOf(q) !== -1 || user.indexOf(q) !== -1) {
                            el.style.display = 'flex';
                            visible++;
                        } else {
                            el.style.display = 'none';
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
                    items.forEach(function(el) { el.style.display = 'flex'; });
                    if (emptyNotice) emptyNotice.style.display = 'none';
                });
            }

            // AJAX Bỏ theo dõi tác giả mượt mà
            document.querySelectorAll('.form-ajax-unfollow').forEach(function(form) {
                form.addEventListener('submit', function(e) {
                    e.preventDefault();
                    var card = this.closest('.author-card-box');
                    var formData = new FormData(this);
                    var params = new URLSearchParams(formData);

                    fetch(this.action, {
                        method: 'POST',
                        headers: { 'X-Requested-With': 'XMLHttpRequest' },
                        body: params
                    })
                    .then(function(res) { return res.json(); })
                    .then(function(data) {
                        if (data && data.success && card) {
                            card.style.opacity = '0';
                            card.style.transform = 'scale(0.95)';
                            setTimeout(function() {
                                card.remove();
                                var badge = document.getElementById('following-count-badge');
                                var remaining = document.querySelectorAll('#following-grid .author-card-box').length;
                                if (badge) badge.innerHTML = '<b>' + remaining + '</b> tác giả';
                            }, 220);
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
        <c:set var="emIcon"  value="👥"/>
        <c:set var="emTitle" value="Chưa theo dõi ai"/>
        <c:set var="emText"  value="Vào trang một tác giả và bấm Theo dõi để nhận thông báo khi họ đăng chương mới."/>
        <c:set var="emUrl"   value="/story?action=list"/>
        <c:set var="emBtn"   value="Tìm truyện hay"/>
        <%@ include file="/WEB-INF/views/_partials/_empty.jsp" %>
    </c:otherwise>
</c:choose>
