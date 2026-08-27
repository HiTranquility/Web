<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
================================================================================
  home.jsp — MẢNH NỘI DUNG của trang chủ
================================================================================
  Nhận từ HomeServlet:
      latest        List<Story>  truyện mới cập nhật
      popular       List<Story>  truyện xem nhiều
      totalStories  int
      dbError       String, khác null nếu chưa kết nối được database

  ĐÂY LÀ MẢNH, KHÔNG PHẢI TRANG HOÀN CHỈNH
    File này KHÔNG có <html>, <head>, <body> — layout/main.jsp lo phần đó.
    Nó chỉ chứa phần ruột, được chèn vào giữa khung bằng <jsp:include>.
    Mở thẳng file này bằng URL cũng không được: nó nằm trong WEB-INF.

  VÌ SAO DÙNG JSTL (thẻ <c:...>) Ở ĐÂY
    Chương 5 chỉ dùng EL thuần vì chưa học JSTL. Nhưng trang này phải LẶP qua
    danh sách truyện, mà EL thuần không lặp được — không có <c:forEach> thì
    buộc phải viết scriptlet <% for(...) %>, tức là nhét Java vào JSP, đúng cái
    MVC muốn tránh. JSTL là chương 9 của sách, dùng sớm hơn một chút là đáng.

  VÌ SAO MỌI CHỖ IN DỮ LIỆU ĐỀU LÀ <c:out> CHỨ KHÔNG PHẢI ${...}
    ${story.title} KHÔNG tự escape HTML. Người dùng đặt tên truyện là
    <script>...</script> thì đoạn đó chạy thật trên trình duyệt người khác —
    đó là lỗ hổng XSS mình đã chứng minh được ở dự án chương 5.
    <c:out> escape sẵn, nên mặc định là an toàn.
    QUY TẮC: dữ liệu do NGƯỜI DÙNG nhập -> luôn <c:out>.
================================================================================
--%>


    <section class="hero">
        <h1>Đọc, viết và <em>chia sẻ</em> những câu chuyện</h1>
        <p>Kho truyện do cộng đồng đóng góp. Tìm theo thể loại bạn thích,
           đánh dấu để đọc tiếp, hoặc tự đăng truyện của riêng mình.</p>
    </section>

    <%-- ---- Truyện xem nhiều -------------------------------------------- --%>
    <c:if test="${not empty popular}">
        <div class="section-head">
            <h2>🔥 Đang được đọc nhiều</h2>
            <a class="more" href="${pageContext.request.contextPath}/story?action=list&amp;sort=popular">
                Xem tất cả →</a>
        </div>
        <div class="story-grid">
            <c:forEach var="story" items="${popular}">
                <%@ include file="/WEB-INF/views/story/_card.jsp" %>
            </c:forEach>
        </div>
    </c:if>

    <%-- ---- Truyện mới cập nhật ----------------------------------------- --%>
    <div class="section-head">
        <h2>Mới cập nhật</h2>
        <c:if test="${not empty totalStories}">
            <span class="more">${totalStories} truyện</span>
        </c:if>
    </div>

    <c:choose>
        <c:when test="${not empty latest}">
            <div class="story-grid">
                <c:forEach var="story" items="${latest}">
                    <%@ include file="/WEB-INF/views/story/_card.jsp" %>
                </c:forEach>
            </div>
        </c:when>
        <c:otherwise>
            <%-- Trạng thái rỗng cũng phải được thiết kế: trang trắng trơn
                 trông như web hỏng, ô rỗng có thiết kế thì trông như chủ ý. --%>
            <div class="empty">
                <div class="empty-icon">📚</div>
                <h3>Chưa có truyện nào</h3>
                <p>Kho truyện đang trống. Hãy là người đầu tiên đăng truyện,
                   hoặc chạy <code>database/sample_data.sql</code> để nạp dữ liệu mẫu.</p>
                <a class="btn btn-primary"
                   href="${pageContext.request.contextPath}/story?action=create">Đăng truyện đầu tiên</a>
            </div>
        </c:otherwise>
    </c:choose>

