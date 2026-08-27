<%@ page pageEncoding="UTF-8" %>
<%-- pageEncoding la BAT BUOC cho moi file .jsp duoc include tinh:
     Tomcat doc TUNG FILE theo encoding rieng cua no. Thieu dong nay
     thi file duoc doc bang ISO-8859-1 va tieng Viet thanh ky tu la,
     du trang cha da khai UTF-8. --%>
<%--
================================================================================
  _card.jsp — MỘT thẻ truyện trong lưới
================================================================================
  Dấu gạch dưới ở đầu tên file là quy ước: "đây là mảnh, không phải trang".

  Cần biến `story` đã được đặt sẵn, thường là bởi <c:forEach var="story">.

  VÌ SAO TÁCH RA FILE RIÊNG
    Thẻ truyện xuất hiện ở trang chủ, kho truyện, kết quả lọc theo tag, trang
    bookmark, trang cá nhân tác giả. Chép đi chép lại 5 lần thì đổi thiết kế
    một cái phải sửa 5 chỗ và chắc chắn sót. Sửa ở đây là cả 5 nơi đổi theo.

  LƯU Ý: dùng <%@ include %> (include tĩnh) nên file này thấy được biến `story`
  của vòng lặp bên trang cha. Nếu dùng <jsp:include> thì không, phải truyền
  tham số tường minh.
================================================================================
--%>
<a class="story-card"
   href="${pageContext.request.contextPath}/story?action=detail&amp;id=${story.id}">

    <div class="story-cover">
        <c:choose>
            <c:when test="${not empty story.coverUrl}">
                <%-- alt lấy từ tiêu đề: cần cho trình đọc màn hình, và hiện ra
                     khi ảnh lỗi. Vẫn phải escape vì tiêu đề do người dùng nhập. --%>
                <img src="<c:out value='${story.coverUrl}'/>"
                     alt="<c:out value='${story.title}'/>" loading="lazy">
            </c:when>
            <c:otherwise>
                <%-- Không có bìa thì lấy chữ cái đầu. Chữ này do model tính
                     (story.getInitial()), không tính trong JSP. --%>
                <div class="cover-fallback">${story.initial}</div>
            </c:otherwise>
        </c:choose>

        <span class="badge ${story.completed ? 'badge-done' : 'badge-going'}">
            ${story.completed ? 'Hoàn thành' : 'Đang ra'}
        </span>
    </div>

    <div class="story-body">
        <%-- <c:out> escape HTML -> chống XSS. Xem ghi chú ở home.jsp. --%>
        <div class="story-title"><c:out value="${story.title}"/></div>

        <div class="story-meta">
            <span>✍️ <c:out value="${story.authorName}"/></span>
        </div>
        <div class="story-meta">
            <span>📄 ${story.chapterCount} chương</span>
            <span>👁️ ${story.viewCount}</span>
        </div>
    </div>
</a>
