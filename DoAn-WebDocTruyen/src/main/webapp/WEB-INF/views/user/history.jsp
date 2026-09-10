<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
================================================================================
  user/history.jsp — MẢNH NỘI DUNG: Lịch sử đọc                  TRANG 31
================================================================================
  TẦNG: views/ · layout: main

  Nhận: history (List<ReadHistory>) · totalRead (int)

  KHÁC TRANG 11 (TRUYỆN ĐÃ LƯU) CHỖ NÀO
      Trang 11 là danh sách người dùng TỰ CHỌN — nên có nút "Bỏ lưu" cho từng
      truyện. Trang này là dấu vết TỰ ĐỘNG, không ai chọn gì cả, nên không có
      nút xoá từng dòng: xoá một dòng trong một danh sách mình không tạo ra thì
      chẳng để làm gì. Chỉ có một nút xoá sạch, đặt ở cuối trang.

  Dùng lại lớp CSS .bookmark-list / .bookmark-item của trang 11 — hai trang
  hiển thị cùng một dạng dữ liệu (bìa · tên · tiến độ · nút), không có lý do
  gì để trông khác nhau.
================================================================================
--%>
<div class="section-head">
    <h2>Lịch sử đọc</h2>
    <span class="more">${totalRead} truyện</span>
</div>

<c:choose>
    <c:when test="${not empty history}">
        <p class="muted-note" style="margin:0 0 16px">
            Tự động ghi lại khi bạn mở truyện — không cần bấm lưu.
            Chỉ hiện <b>${fn:length(history)}</b> truyện gần nhất.
        </p>

        <div class="bookmark-list">
            <c:forEach var="h" items="${history}">
                <div class="bookmark-item">
                    <div class="bm-cover">
                        <c:choose>
                            <c:when test="${not empty h.coverUrl}">
                                <img src="<c:out value='${h.coverUrl}'/>"
                                     alt="<c:out value='${h.storyTitle}'/>">
                            </c:when>
                            <c:otherwise>
                                <div class="cover-fallback">${h.initial}</div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div class="bm-info">
                        <a class="bm-title"
                           href="${pageContext.request.contextPath}/story?action=detail&amp;id=${h.storyId}">
                            <c:out value="${h.storyTitle}"/></a>

                        <p class="bm-progress">
                            <%-- viewedLabel do model tính: "Hôm nay" / "Hôm qua"
                                 / "3 ngày trước" / "12/9". Mắt tìm chữ "hôm qua"
                                 nhanh hơn dò một cột ngày tháng. --%>
                            <b>${h.viewedLabel}</b>
                            &middot; <c:out value="${h.authorName}"/>

                            <c:if test="${h.viewTimes gt 1}">
                                &middot; đã mở ${h.viewTimes} lần
                            </c:if>
                        </p>

                        <p class="bm-progress">
                            <c:choose>
                                <c:when test="${h.resumable}">
                                    Đang đọc chương ${h.lastChapterNo} / ${h.totalChapters}
                                </c:when>
                                <c:otherwise>
                                    <%-- Có trong lịch sử mà không có vị trí đọc:
                                         mở truyện ra xem nhưng chưa lưu, nên
                                         bookmarks không có dòng nào. --%>
                                    ${h.totalChapters} chương &middot; chưa lưu
                                </c:otherwise>
                            </c:choose>
                        </p>
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

                        <%-- Lưu ngay từ đây: gặp lại truyện hay trong lịch sử thì
                             lưu được luôn, khỏi phải sang trang truyện rồi quay
                             lại. from=history để servlet biết đường về. --%>
                        <form action="${pageContext.request.contextPath}/bookmark"
                              method="post" style="display:inline">
                            <input type="hidden" name="_csrf" value="${csrfToken}">
                            <input type="hidden" name="action" value="add">
                            <input type="hidden" name="storyId" value="${h.storyId}">
                            <input type="hidden" name="from" value="history">
                            <button type="submit" class="btn btn-ghost btn-sm">☆ Lưu</button>
                        </form>
                    </div>
                </div>
            </c:forEach>
        </div>

        <%--
          XOÁ LỊCH SỬ.

          Đặt ở CUỐI trang, không phải cạnh tiêu đề: người vào đây là để tìm
          truyện đã đọc, không phải để xoá. Nút xoá nằm trên đầu thì suốt ngày
          chắn đường thứ người ta thực sự cần.

          <details> gấp lại — bấm một lần để mở, một lần nữa mới xoá được. Hai
          bước cho một việc không hoàn tác được, mà không cần một dòng
          JavaScript nào.
        --%>
        <details class="danger-zone">
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
                    <button type="submit" class="btn btn-danger btn-sm">
                        Xoá toàn bộ lịch sử</button>
                </form>
            </div>
        </details>
    </c:when>

    <c:otherwise>
        <%-- MẢNH: trạng thái rỗng, dùng chung với năm trang danh sách khác --%>
        <c:set var="emIcon"  value="🕘"/>
        <c:set var="emTitle" value="Chưa có lịch sử đọc"/>
        <c:set var="emText"
               value="Mở một truyện bất kỳ là nó tự xuất hiện ở đây. Khác với mục Đã lưu, bạn không phải bấm gì cả."/>
        <c:set var="emUrl"   value="/story?action=list"/>
        <c:set var="emBtn"   value="Tìm truyện để đọc"/>
        <%@ include file="/WEB-INF/views/_partials/_empty.jsp" %>
    </c:otherwise>
</c:choose>
