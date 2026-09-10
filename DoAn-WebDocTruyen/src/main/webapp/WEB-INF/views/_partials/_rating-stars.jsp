<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
================================================================================
  _rating-stars.jsp — MẢNH TÁI DÙNG: sao đánh giá
================================================================================
  TẦNG: views/

  DÙNG Ở: trang 2 (kho truyện), 3 (chi tiết), 5 (xếp hạng), 25 (bảng điều khiển)

  CÁCH DÙNG — chỉ HIỂN THỊ:
      <c:set var="rsStory" value="${story}"/>
      <%@ include file="/WEB-INF/views/_partials/_rating-stars.jsp" %>

  CÁCH DÙNG — cho phép CHẤM (thêm rsForm):
      <c:set var="rsStory" value="${story}"/>
      <c:set var="rsForm"  value="true"/>
      <c:set var="rsMine"  value="${myRating}"/>
      <%@ include file="/WEB-INF/views/_partials/_rating-stars.jsp" %>

  MỘT MẢNH CHO CẢ HAI VIỆC, VÌ SAO
    Tách thành _rating-show và _rating-form thì hai file phải vẽ ngôi sao
    giống hệt nhau. Ngày nào đó đổi biểu tượng sao là phải nhớ sửa cả hai —
    và chắc chắn sẽ quên một chỗ.

  VÌ SAO NÚT CHẤM LÀ FORM POST CHỨ KHÔNG PHẢI THẺ <a>
    Chấm sao là thay đổi dữ liệu. Thẻ <a> chạy bằng GET, mà GET thì bất kỳ
    trang nào cũng gọi được bằng một thẻ <img> ẩn — người dùng bị chấm sao hộ
    mà không hay. Xem thêm ghi chú trong RatingServlet.
================================================================================
--%>
<div class="rating">

    <%-- Phần hiển thị: năm ngôi sao, tô đặc theo điểm trung bình đã làm tròn --%>
    <span class="rating-stars" title="${rsStory.ratingAvg} / 5">
        <c:forEach begin="1" end="5" var="i">
            <span class="star ${i le rsStory.ratingStars ? 'star-on' : ''}">★</span>
        </c:forEach>
    </span>

    <c:choose>
        <c:when test="${rsStory.rated}">
            <span class="rating-text">
                <b>${rsStory.ratingAvg}</b>
                <span class="muted">(${rsStory.ratingCount} lượt)</span>
            </span>
        </c:when>
        <c:otherwise>
            <span class="rating-text muted">Chưa có đánh giá</span>
        </c:otherwise>
    </c:choose>
</div>

<%-- Phần chấm điểm — chỉ hiện khi trang yêu cầu VÀ người dùng đã đăng nhập --%>
<c:if test="${rsForm and not empty currentUser}">
    <form class="rating-form" method="post" id="rating"
          action="${pageContext.request.contextPath}/rating">
        <input type="hidden" name="_csrf" value="${csrfToken}">
        <input type="hidden" name="storyId" value="${rsStory.id}">

        <span class="rating-label">
            <c:choose>
                <c:when test="${rsMine gt 0}">Bạn đã chấm ${rsMine} sao — chấm lại:</c:when>
                <c:otherwise>Bạn chấm mấy sao?</c:otherwise>
            </c:choose>
        </span>

        <%--
          Năm nút submit, mỗi nút mang một giá trị.
          Cách này không cần một dòng JavaScript nào và vẫn chạy đúng khi
          người dùng tắt JS — nguyên tắc "hoạt động được trước, đẹp sau".
        --%>
        <span class="rating-pick">
            <c:forEach begin="1" end="5" var="i">
                <button type="submit" name="score" value="${i}"
                        class="star-btn ${i le rsMine ? 'star-on' : ''}"
                        title="${i} sao">★</button>
            </c:forEach>
        </span>
    </form>
</c:if>
