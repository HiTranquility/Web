<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--
================================================================================
  story/mine.jsp — MẢNH NỘI DUNG: Truyện của tôi                TRANG 12
================================================================================
  TẦNG: views/

  Nhận: stories (List<Story>, gồm cả DRAFT)

  VÌ SAO ĐỔI TỪ LƯỚI THẺ SANG HÀNG NGANG
    Lưới thẻ (_card) hợp với người đang LƯỚT TÌM: bìa to, bắt mắt, mỗi ô một
    quyết định độc lập.
    Trang này thì khác — tác giả đã biết rõ mình có truyện gì, họ vào để QUẢN
    LÝ: cái nào còn nháp, cái nào bao nhiêu chương, cái nào cần thêm chương.
    Đó là việc SO SÁNH theo cột, và hàng ngang đọc nhanh hơn hẳn.

  Cùng dữ liệu, hai cách đọc khác nhau — đúng lý do có cả _card lẫn _story-row.
================================================================================
--%>
<div class="section-head">
    <h2>Truyện của tôi</h2>
    <div style="display:flex;gap:8px">
        <a class="btn btn-ghost btn-sm"
           href="${pageContext.request.contextPath}/story?action=stats">📊 Thống kê</a>
        <a class="btn btn-primary btn-sm"
           href="${pageContext.request.contextPath}/story?action=create">+ Đăng truyện mới</a>
    </div>
</div>

<c:choose>
    <c:when test="${not empty stories}">
        <%--
          Đếm nháp / công khai ngay trong JSP.

          Đây là phép đếm trên danh sách ĐÃ nằm sẵn trong bộ nhớ, không phải
          truy vấn thêm. Ranh giới cần giữ là "view không được gọi DAO", chứ
          không phải "view không được đếm" — bắt servlet đếm hộ hai con số này
          rồi truyền sang chỉ làm nặng thêm hợp đồng giữa hai tầng.
        --%>
        <c:set var="nDraft" value="0"/>
        <c:forEach var="s" items="${stories}">
            <c:if test="${s.status eq 'DRAFT'}">
                <c:set var="nDraft" value="${nDraft + 1}"/>
            </c:if>
        </c:forEach>

        <p class="muted-note" style="margin-bottom:18px">
            ${stories.size()} truyện
            <c:if test="${nDraft gt 0}">
                &middot; <b>${nDraft} bản nháp</b> — bản nháp chỉ mình bạn thấy,
                chưa xuất hiện trong kho truyện.
            </c:if>
        </p>

        <div class="story-rows">
            <c:forEach var="story" items="${stories}">
                <%--
                  MẢNH _story-row cần biến srStory. srMeta là dòng phụ do
                  trang gọi tự quyết định — ở đây dùng để nói rõ trạng thái.
                --%>
                <c:set var="srStory" value="${story}"/>
                <c:set var="srMeta">
                    <c:choose>
                        <c:when test="${story.status eq 'DRAFT'}">Bản nháp — chưa công khai</c:when>
                        <c:when test="${story.status eq 'DELETED'}">Đã gỡ khỏi kho truyện</c:when>
                        <c:otherwise>Đang công khai</c:otherwise>
                    </c:choose>
                </c:set>

                <div class="mine-row">
                    <%@ include file="/WEB-INF/views/_partials/_story-row.jsp" %>

                    <div class="mine-actions">
                        <a class="btn btn-ghost btn-sm"
                           href="${pageContext.request.contextPath}/chapter?action=create&amp;storyId=${story.id}">
                            + Chương</a>
                        <a class="btn btn-ghost btn-sm"
                           href="${pageContext.request.contextPath}/story?action=edit&amp;id=${story.id}">
                            Sửa</a>

                        <%-- Gỡ truyện là XOÁ MỀM (status = DELETED), khôi phục
                             được ở khu quản trị. Vẫn hỏi lại — độc giả đang
                             đọc dở sẽ mất chỗ. --%>
                        <form method="post" style="display:inline"
                              action="${pageContext.request.contextPath}/story"
                              onsubmit="return confirm('Gỡ truyện này khỏi kho truyện?')">
                            <input type="hidden" name="_csrf" value="${csrfToken}">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="id" value="${story.id}">
                            <button type="submit" class="btn btn-danger btn-sm">Gỡ</button>
                        </form>
                    </div>
                </div>
            </c:forEach>
        </div>
    </c:when>

    <c:otherwise>
        <%-- MẢNH: trạng thái rỗng --%>
        <c:set var="emIcon"  value="✍️"/>
        <c:set var="emTitle" value="Bạn chưa đăng truyện nào"/>
        <c:set var="emText"  value="Bắt đầu viết câu chuyện đầu tiên. Có thể lưu nháp trước, khi nào ưng ý mới công khai."/>
        <c:set var="emUrl"   value="/story?action=create"/>
        <c:set var="emBtn"   value="Đăng truyện đầu tiên"/>
        <%@ include file="/WEB-INF/views/_partials/_empty.jsp" %>
    </c:otherwise>
</c:choose>
