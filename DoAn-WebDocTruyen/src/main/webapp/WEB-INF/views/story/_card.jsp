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

        <%--
          NHÃN "HOT" — ngưỡng đặt cứng 10.000 lượt xem.

          Số này là quy ước, không phải phép tính. Cách "đúng" hơn là so với
          trung bình của cả kho, nhưng như vậy thì lúc kho mới có ba truyện,
          truyện nào cũng thành Hot — nhãn mất hết ý nghĩa. Một mốc cố định
          dễ giải thích và không đổi theo ngày.

          Đặt trong _card.jsp nên năm trang dùng thẻ này đều có nhãn, không
          phải nhớ chép lại chỗ nào.
        --%>
        <c:if test="${story.viewCount ge 10000}">
            <span class="badge badge-hot">🔥 Hot</span>
        </c:if>

        <%--
          NHÃN "MỚI" — truyện đăng trong 14 ngày gần đây.

          isNew() tính trong model chứ không tính ở đây. EL không có phép trừ
          ngày tháng: so sánh LocalDateTime trong JSP sẽ phải viết một chuỗi
          biểu thức dài và không ai đọc nổi. Model có sẵn java.time thì để
          model làm.

          Một truyện có thể vừa Mới vừa Hot — hiếm, nhưng nếu xảy ra thì đó
          đúng là truyện đáng chú ý nhất, cho hiện cả hai.

          Getter tên isRecent() chứ không phải isNew(): `new` là từ khoá của
          EL, ${story.new} làm cả trang lỗi 500. Xem ghi chú ở model/Story.java.
        --%>
        <c:if test="${story.recent}">
            <span class="badge badge-new">✨ Mới</span>
        </c:if>
    </div>

    <div class="story-body">
        <%-- <c:out> escape HTML -> chống XSS. Xem ghi chú ở home.jsp. --%>
        <div class="story-title"><c:out value="${story.title}"/></div>

        <div class="story-meta">
            <span>✍️ <c:out value="${story.authorName}"/></span>
        </div>
        <div class="story-meta">
            <span>📄 ${story.chapterCount} chương</span>
            <%-- fmt:formatNumber dat dau phan cach hang nghin: "31.200" de doc
                 luot hon "31200". Dinh dang o tang view, KHONG bien so
                 thanh String trong model — model giu so nguyen thi con
                 cong tru, so sanh va sap xep duoc. --%>
            <span>👁️ <fmt:formatNumber pattern="#,##0" value="${story.viewCount}"/></span>
        </div>

        <%--
          MẢNH: sao đánh giá (chỉ hiển thị, không chấm được từ đây).

          Chấm sao ngay trên thẻ nghe tiện, nhưng thẻ này nằm trong một thẻ
          <a> bọc cả ô — nhét form vào trong link là HTML không hợp lệ và
          trình duyệt xử lý mỗi nơi một kiểu. Muốn chấm thì vào trang chi tiết.
        --%>
        <c:set var="rsStory" value="${story}"/>
        <c:set var="rsForm"  value="${false}"/>
        <%@ include file="/WEB-INF/views/_partials/_rating-stars.jsp" %>
    </div>
</a>
