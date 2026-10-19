<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
================================================================================
  user/appeal.jsp — MẢNH NỘI DUNG: Gửi thư khiếu nại kiểm duyệt tới Admin
================================================================================
  TẦNG: views/ · layout: main
  Nhận: myStories (List<Story>) · selectedStory (Story) · storyId (int)
        appealTitle (String) · appealReason (String) · message (String)
================================================================================
--%>
<div class="form-studio-wrapper" style="max-width: 800px; margin: 0 auto; padding: 24px 16px;">
    <div class="form-header-banner" style="margin-bottom: 24px;">
        <h1 style="display: flex; align-items: center; gap: 10px; font-size: 1.75rem; margin-bottom: 8px;">
            <span>🛡️</span> Gửi thư Khiếu nại Kiểm duyệt
        </h1>
        <p class="form-subtitle" style="color: var(--text-mut); font-size: 0.95rem; line-height: 1.5;">
            Khi tác phẩm của bạn bị Quản trị viên gỡ bỏ, ẩn hoặc điều chỉnh kiểm duyệt, bạn có quyền gửi giải trình chính thức để Ban Quản Trị xem xét lại.
        </p>
    </div>

    <%-- Hộp thông tin hướng dẫn & đảm bảo quyền lợi --%>
    <div style="background: rgba(230, 81, 0, 0.08); border: 1px solid rgba(230, 81, 0, 0.3); border-radius: var(--r-md, 10px); padding: 18px 20px; margin-bottom: 24px;">
        <h3 style="color: var(--ember, #e65100); font-size: 1rem; margin-top: 0; margin-bottom: 8px; display: flex; align-items: center; gap: 8px;">
            <span>ℹ️</span> Quy trình tiếp nhận &amp; Phản hồi khiếu nại
        </h3>
        <ul style="margin: 0; padding-left: 20px; color: var(--text); font-size: 0.9rem; line-height: 1.6;">
            <li>Đơn khiếu nại sẽ được đưa vào <b>Hàng đợi Báo cáo Quản trị</b> và đánh dấu ưu tiên.</li>
            <li>Một bản sao thư giải trình sẽ được gửi trực tiếp tới <b>Hòm thư điện tử (Admin Mail)</b> của Ban Quản Trị.</li>
            <li>Admin sẽ đối soát lại nội dung, lịch sử can thiệp và liên hệ phản hồi với bạn qua email hoặc thông báo hệ thống trong vòng 24 - 48 giờ.</li>
        </ul>
    </div>

    <form id="appealForm" action="${pageContext.request.contextPath}/appeal" method="post" class="wide-form">
        <input type="hidden" name="_csrf" value="${csrfToken}">

        <fieldset class="form-group" style="background: var(--ink-900, #18181b); border: 1px solid var(--ink-700, #27272a); border-radius: var(--r-md, 10px); padding: 24px; margin-bottom: 24px;">
            <legend style="font-weight: 600; padding: 0 8px; color: var(--ember, #e65100);">📋 Chi tiết Đơn khiếu nại</legend>

            <div style="margin-bottom: 20px;">
                <label for="storyIdSelect" style="display: block; font-weight: 600; margin-bottom: 8px;">
                    Truyện khiếu nại <span style="color: var(--danger, #ef4444);">*</span>
                </label>
                <c:choose>
                    <c:when test="${not empty myStories}">
                        <select id="storyIdSelect" name="storyId" required
                                style="width: 100%; padding: 10px 14px; border-radius: var(--r-sm, 6px); border: 1px solid var(--ink-700, #3f3f46); background: var(--ink-800, #202024); color: var(--text, #fff); font-size: 0.95rem;">
                            <option value="">-- Chọn tác phẩm cần khiếu nại --</option>

                            <%-- Nhóm 1: Truyện đã bị gỡ (cần khiếu nại nhất) --%>
                            <c:set var="hasDeleted" value="false"/>
                            <c:forEach var="s" items="${myStories}">
                                <c:if test="${s.status eq 'DELETED'}"><c:set var="hasDeleted" value="true"/></c:if>
                            </c:forEach>
                            <c:if test="${hasDeleted}">
                                <optgroup label="🚫 Truyện đã bị gỡ bỏ">
                                    <c:forEach var="s" items="${myStories}">
                                        <c:if test="${s.status eq 'DELETED'}">
                                            <option value="${s.id}" ${s.id eq storyId ? 'selected="selected"' : ''}>
                                                🚫 <c:out value="${s.title}"/> — ${s.chapterCount} chương
                                            </option>
                                        </c:if>
                                    </c:forEach>
                                </optgroup>
                            </c:if>

                            <%-- Nhóm 2: Truyện đang công khai / bản nháp --%>
                            <optgroup label="📚 Truyện đang hoạt động">
                                <c:forEach var="s" items="${myStories}">
                                    <c:if test="${s.status ne 'DELETED'}">
                                        <option value="${s.id}" ${s.id eq storyId ? 'selected="selected"' : ''}>
                                            ${s.status eq 'DRAFT' ? '📝' : '🌐'} <c:out value="${s.title}"/> — ${s.chapterCount} chương
                                        </option>
                                    </c:if>
                                </c:forEach>
                            </optgroup>
                        </select>

                        <%-- Thẻ thông tin truyện được chọn --%>
                        <c:if test="${not empty selectedStory}">
                            <div id="storyInfoCard" style="margin-top: 12px; background: var(--ink-800, #202024); border: 1px solid ${selectedStory.status eq 'DELETED' ? 'rgba(239,68,68,0.4)' : 'var(--ink-700,#3f3f46)'}; border-radius: var(--r-sm, 6px); padding: 12px 16px; display: flex; align-items: center; gap: 12px;">
                                <span style="font-size: 1.5rem;">${selectedStory.status eq 'DELETED' ? '🚫' : (selectedStory.status eq 'DRAFT' ? '📝' : '📚')}</span>
                                <div>
                                    <div style="font-weight: 600; color: var(--text);">
                                        <c:out value="${selectedStory.title}"/>
                                    </div>
                                    <div style="font-size: 0.84rem; color: var(--text-mut); margin-top: 3px;">
                                        ${selectedStory.chapterCount} chương ·
                                        <c:choose>
                                            <c:when test="${selectedStory.status eq 'DELETED'}">
                                                <span style="color: #ef4444; font-weight: 600;">Đã bị gỡ bỏ</span> — Đây là tác phẩm cần khiếu nại
                                            </c:when>
                                            <c:when test="${selectedStory.status eq 'DRAFT'}">
                                                <span style="color: #a1a1aa;">Bản nháp (chưa công khai)</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span style="color: #4ade80;">Đang công khai</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </div>
                        </c:if>
                    </c:when>
                    <c:otherwise>
                        <p style="color: var(--text-mut); font-size: 0.9rem;">
                            Bạn hiện chưa có tác phẩm nào do mình sáng tác trong hệ thống.
                        </p>
                    </c:otherwise>
                </c:choose>
            </div>

            <div style="margin-bottom: 20px;">
                <label for="appealTitle" style="display: block; font-weight: 600; margin-bottom: 8px;">
                    Tiêu đề khiếu nại <span style="color: var(--text-mut); font-weight: normal; font-size: 0.85rem;">(Tùy chọn)</span>
                </label>
                <input type="text" id="appealTitle" name="appealTitle" maxlength="200"
                       placeholder="Ví dụ: Khiếu nại gỡ nhầm truyện do trùng tên / Giải trình bản quyền"
                       value="<c:out value='${appealTitle}'/>"
                       style="width: 100%; padding: 10px 14px; border-radius: var(--r-sm, 6px); border: 1px solid var(--ink-700, #3f3f46); background: var(--ink-800, #202024); color: var(--text, #fff); font-size: 0.95rem;">
            </div>

            <div style="margin-bottom: 12px;">
                <label for="appealReason" style="display: block; font-weight: 600; margin-bottom: 8px;">
                    Nội dung giải trình &amp; Lý do khiếu nại <span style="color: var(--danger, #ef4444);">*</span>
                </label>
                <textarea id="appealReason" name="appealReason" rows="8" required maxlength="3000"
                          placeholder="Trình bày cụ thể:&#10;1. Vì sao bạn cho rằng quyết định can thiệp của Quản trị viên là chưa chính xác hoặc nhầm lẫn?&#10;2. Nguồn gốc tác phẩm, quyền tác giả hoặc đường dẫn dẫn chứng (nếu có)?&#10;3. Đề xuất của bạn đối với tác phẩm..."
                          style="width: 100%; padding: 12px 14px; border-radius: var(--r-sm, 6px); border: 1px solid var(--ink-700, #3f3f46); background: var(--ink-800, #202024); color: var(--text, #fff); font-size: 0.95rem; line-height: 1.6; resize: vertical;"><c:out value="${appealReason}"/></textarea>
                <div style="display: flex; justify-content: flex-end; margin-top: 6px;">
                    <span id="appealReasonCounter" style="font-size: 0.82rem; color: var(--text-mut);">0 / 3.000 ký tự</span>
                </div>
            </div>
        </fieldset>

        <div style="display: flex; align-items: center; justify-content: flex-end; gap: 12px; margin-top: 24px;">
            <a href="${pageContext.request.contextPath}/notification" class="btn btn-ghost" style="padding: 10px 20px;">
                Hủy bỏ
            </a>
            <button type="submit" class="btn btn-primary" style="padding: 10px 24px; font-weight: 600; display: inline-flex; align-items: center; gap: 8px;">
                <span>✉️</span> Gửi thư khiếu nại tới Admin
            </button>
        </div>
    </form>
</div>

<script>
(function() {
    var reasonInput = document.getElementById('appealReason');
    var counterEl = document.getElementById('appealReasonCounter');
    if (reasonInput && counterEl) {
        function updateCount() {
            var len = reasonInput.value.length;
            counterEl.textContent = len.toLocaleString('vi-VN') + ' / 3.000 ký tự';
            if (len < 15) {
                counterEl.style.color = 'var(--danger, #ef4444)';
            } else {
                counterEl.style.color = 'var(--text-mut)';
            }
        }
        reasonInput.addEventListener('input', updateCount);
        updateCount();
    }
})();
</script>

