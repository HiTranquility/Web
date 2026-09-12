<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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

<c:choose>
    <c:when test="${not empty reports}">
        <div class="report-list">
            <c:forEach var="r" items="${reports}">
                <div class="report-item ${r.pending ? 'report-pending' : ''}">

                    <div class="report-head">
                        <span class="pill ${r.story ? 'pill-muted' : 'pill-warn'}">
                            ${r.story ? '📚 Truyện' : '💬 Bình luận'}
                        </span>
                        <span class="pill ${r.pending ? 'pill-danger'
                                          : (r.status eq 'RESOLVED' ? 'pill-ok' : 'pill-muted')}">
                            ${r.statusLabel}
                        </span>
                        <span class="report-time">
                            <c:if test="${not empty r.createdAt}">
                                ${r.createdAt.dayOfMonth}/${r.createdAt.monthValue}/${r.createdAt.year}
                            </c:if>
                        </span>
                    </div>

                    <p class="report-target">
                        <b>Nội dung bị báo cáo:</b>
                        <c:choose>
                            <c:when test="${r.story}">
                                <a href="${pageContext.request.contextPath}/story?action=detail&amp;id=${r.targetId}">
                                    <c:out value="${r.targetTitle}"/></a>
                            </c:when>
                            <c:otherwise>
                                <%-- Bình luận: chỉ có 80 ký tự đầu do câu SQL cắt
                                     bằng LEFT(). Đủ để nhận ra, không chiếm chỗ. --%>
                                <span class="quote"><c:out value="${r.targetTitle}"/>…</span>
                            </c:otherwise>
                        </c:choose>
                    </p>

                    <p class="report-reason">
                        <b>Lý do:</b> <c:out value="${r.reason}"/>
                    </p>

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
