<%@ page pageEncoding="UTF-8" %>
<%--
================================================================================
  _recommend.jsp — KHỐI GỢI Ý TRUYỆN, mẫu DUY NHẤT cho cả web
================================================================================
  DÙNG Ở: common/story/detail.jsp   (cuối trang chi tiết, 8 thẻ)
          common/story/recommend.jsp (cuối trang đọc, nạp bằng fetch, 4 thẻ)

  Nhận:   recommendations  List<Story>          — StoryDAO.findRecommendations()
          recReasons       Map<Integer,String>  — lý do gợi ý theo id truyện
          recTitle         (tuỳ chọn) tiêu đề khối, mặc định "Có thể bạn cũng thích"

  VÌ SAO GỘP THÀNH MỘT
    Trước đây trang chi tiết có hai khối riêng, mỗi khối một tiêu đề, một
    nhãn, 0–4 thẻ, tự biến mất khi rỗng, và có thể lặp lại cùng một truyện.
    Giờ: một tiêu đề, một lưới cố định 4 cột (2 cột trên điện thoại), mỗi
    thẻ có nhãn nhỏ cho biết VÌ SAO được gợi ý.

  CẨN THẬN BIẾN `story`
    _card.jsp đọc biến `story`. forEach var="story" che biến `story` của
    trang chi tiết trong lúc lặp và trả lại khi hết vòng — KHÔNG dùng
    <c:set var="story"> ở đây, vì c:set không tự trả lại, phần còn lại của
    trang chi tiết sẽ thấy truyện gợi ý cuối cùng thay vì truyện đang xem.
================================================================================
--%>
<c:if test="${not empty recommendations}">
    <section class="rec-section" aria-labelledby="rec-heading">
        <div class="section-head">
            <h2 id="rec-heading">${empty recTitle ? 'Có thể bạn cũng thích' : recTitle}</h2>
            <a class="more" href="${pageContext.request.contextPath}/rank">Bảng xếp hạng →</a>
        </div>

        <div class="story-grid rec-grid">
            <c:forEach var="story" items="${recommendations}">
                <c:set var="cardReason" value="${recReasons[story.id]}"/>
                <%@ include file="/WEB-INF/views/_partials/_card.jsp" %>
            </c:forEach>
            <c:remove var="cardReason"/>
        </div>
    </section>
</c:if>
