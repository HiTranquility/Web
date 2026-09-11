<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--
================================================================================
  story/list.jsp — MẢNH NỘI DUNG: Kho truyện                      TRANG 2
================================================================================
  TẦNG: views/  — chỉ hiển thị. Dữ liệu do StoryServlet chuẩn bị sẵn.

  Nhận từ servlet:
      stories · tags · currentTag · keyword · sort · progress
      page · totalPages · totalStories

  MỘT TRANG GÁNH BỐN VIỆC: duyệt, lọc thể loại, lọc tình trạng, tìm kiếm.
  Không tách trang riêng cho kết quả tìm kiếm — cùng giao diện, chỉ khác tham
  số URL. Tách ra là nhân đôi code mà không được gì.
================================================================================
--%>
<div class="section-head">
    <h2>
        <c:choose>
            <c:when test="${not empty keyword}">Tìm: "<c:out value='${keyword}'/>"</c:when>
            <c:when test="${not empty currentTag}">Lọc theo thể loại</c:when>
            <c:otherwise>Kho truyện</c:otherwise>
        </c:choose>
    </h2>
    <span class="more">${totalStories} truyện</span>
</div>

<%-- Ô tìm kiếm. method="get" vì tìm kiếm chỉ ĐỌC — nhờ vậy URL kết quả
     chia sẻ và bookmark được, nút Back của trình duyệt cũng chạy đúng. --%>
<form action="${pageContext.request.contextPath}/story" method="get" class="search-bar">
    <input type="hidden" name="action" value="list">

    <%-- Giữ lại các bộ lọc khác khi tìm — không thì gõ từ khoá là mất thể loại
         đang chọn, người dùng tưởng bộ lọc hỏng. --%>
    <c:if test="${not empty currentTag}">
        <input type="hidden" name="tag" value="<c:out value='${currentTag}'/>">
    </c:if>
    <c:if test="${not empty progress}">
        <input type="hidden" name="progress" value="<c:out value='${progress}'/>">
    </c:if>
    <c:if test="${not empty sort}">
        <input type="hidden" name="sort" value="<c:out value='${sort}'/>">
    </c:if>

    <input type="text" name="q" placeholder="Tìm theo tên truyện hoặc tên tác giả…"
           value="<c:out value='${keyword}'/>">
    <button type="submit" class="btn btn-primary btn-sm">Tìm</button>
</form>

<%-- Hai chế độ tìm. Đặt ngay dưới ô nhập, không giấu trong menu: người
     không tìm thấy bằng tên truyện cần nhìn thấy lối thứ hai NGAY LÚC ĐÓ. --%>
<div class="filter-bar" style="margin-top:-6px">
    <div class="filter-group">
        <span class="filter-label">Tìm theo</span>
        <a class="chip is-on"
           href="${pageContext.request.contextPath}/story?action=list&amp;q=${keyword}">
            Tên truyện / tác giả</a>
        <a class="chip"
           href="${pageContext.request.contextPath}/story?action=search&amp;q=${keyword}">
            🔎 Nội dung chương</a>
    </div>
</div>

<%-- MẢNH: bộ lọc thể loại --%>
<c:set var="tfBase" value="story?action=list" scope="request"/>
<%@ include file="/WEB-INF/views/_partials/_tag-filter.jsp" %>

<%--
  HAI HÀNG LỌC PHỤ: tình trạng và cách sắp xếp.

  Là thẻ <a> chứ không phải <select> + JavaScript. Mỗi tổ hợp lọc có một URL
  riêng — chia sẻ được, lưu được, nút Back chạy đúng. Đây là ưu điểm lớn nhất
  của trang dựng ở máy chủ, bỏ đi để lấy một cái menu đổ xuống là lỗ vốn.

  filterQ gom mọi tham số ĐANG bật trừ cái sắp đổi, để bấm lọc tình trạng
  không làm mất từ khoá và ngược lại.
--%>
<c:set var="keepTag"  value="${not empty currentTag ? '&tag='.concat(currentTag) : ''}"/>
<c:set var="keepQ"    value="${not empty keyword ? '&q='.concat(keyword) : ''}"/>
<c:set var="keepSort" value="${not empty sort ? '&sort='.concat(sort) : ''}"/>
<c:set var="keepProg" value="${not empty progress ? '&progress='.concat(progress) : ''}"/>

<div class="filter-bar">
    <div class="filter-group">
        <span class="filter-label">Tình trạng</span>
        <a class="chip ${empty progress ? 'is-on' : ''}"
           href="${pageContext.request.contextPath}/story?action=list${keepTag}${keepQ}${keepSort}">Tất cả</a>
        <a class="chip ${progress eq 'ongoing' ? 'is-on' : ''}"
           href="${pageContext.request.contextPath}/story?action=list&amp;progress=ongoing${keepTag}${keepQ}${keepSort}">Đang ra</a>
        <a class="chip ${progress eq 'completed' ? 'is-on' : ''}"
           href="${pageContext.request.contextPath}/story?action=list&amp;progress=completed${keepTag}${keepQ}${keepSort}">Hoàn thành</a>
    </div>

    <div class="filter-group">
        <span class="filter-label">Sắp xếp</span>
        <a class="chip ${empty sort or sort eq 'updated' ? 'is-on' : ''}"
           href="${pageContext.request.contextPath}/story?action=list&amp;sort=updated${keepTag}${keepQ}${keepProg}">Mới cập nhật</a>
        <a class="chip ${sort eq 'popular' ? 'is-on' : ''}"
           href="${pageContext.request.contextPath}/story?action=list&amp;sort=popular${keepTag}${keepQ}${keepProg}">Đọc nhiều</a>
        <a class="chip ${sort eq 'rating' ? 'is-on' : ''}"
           href="${pageContext.request.contextPath}/story?action=list&amp;sort=rating${keepTag}${keepQ}${keepProg}">Điểm cao</a>
    </div>
</div>

<%--
================================================================================
  HÀNG "ĐANG LỌC" — mỗi điều kiện là một thẻ có dấu × để gỡ riêng
================================================================================
  VÌ SAO CẦN dù các chip ở trên đã tô sáng cái đang chọn
    Bốn điều kiện nằm ở BỐN chỗ khác nhau trên trang: từ khoá ở ô nhập, thể
    loại ở hàng thẻ loại, tình trạng và sắp xếp ở hai nhóm chip. Muốn biết
    "mình đang lọc những gì" phải đưa mắt quét cả bốn chỗ rồi tự ghép lại.

    Hàng này gom tất cả về một dòng, và quan trọng hơn: cho gỡ TỪNG CÁI. Trước
    đây muốn bỏ mỗi thể loại mà giữ nguyên từ khoá thì phải bấm lại chip "Tất
    cả" ở đúng nhóm của nó — mà nhóm đó nằm cách xa nơi đang nhìn.

  Chỉ hiện khi CÓ lọc. Không lọc gì mà vẫn chừa một hàng trống là chiếm chỗ vô ích.

  Mỗi dấu × là một LINK dựng sẵn, bỏ đúng một tham số và giữ nguyên phần còn
  lại — dùng lại các biến keepXxx đã tính ở trên, không tính lại lần nữa.
================================================================================
--%>
<c:if test="${not empty keyword or not empty currentTag
              or not empty progress or not empty sort}">
    <div class="active-filters">
        <span class="filter-label">Đang lọc</span>

        <c:if test="${not empty keyword}">
            <a class="chip chip-clear"
               href="${pageContext.request.contextPath}/story?action=list${keepTag}${keepSort}${keepProg}">
                Từ khoá: <b><c:out value="${keyword}"/></b> <span class="chip-x">×</span></a>
        </c:if>

        <c:if test="${not empty currentTag}">
            <a class="chip chip-clear"
               href="${pageContext.request.contextPath}/story?action=list${keepQ}${keepSort}${keepProg}">
                Thể loại: <b><c:out value="${currentTag}"/></b> <span class="chip-x">×</span></a>
        </c:if>

        <c:if test="${not empty progress}">
            <a class="chip chip-clear"
               href="${pageContext.request.contextPath}/story?action=list${keepTag}${keepQ}${keepSort}">
                <b>${progress eq 'completed' ? 'Hoàn thành' : 'Đang ra'}</b>
                <span class="chip-x">×</span></a>
        </c:if>

        <%-- Sắp xếp chỉ hiện khi KHÁC mặc định: "Mới cập nhật" là thứ tự vốn
             có, gọi nó là một bộ lọc đang bật thì gây hiểu nhầm. --%>
        <c:if test="${not empty sort and sort ne 'updated'}">
            <a class="chip chip-clear"
               href="${pageContext.request.contextPath}/story?action=list${keepTag}${keepQ}${keepProg}">
                <b>${sort eq 'popular' ? 'Đọc nhiều' : 'Điểm cao'}</b>
                <span class="chip-x">×</span></a>
        </c:if>

        <a class="link-clear-all"
           href="${pageContext.request.contextPath}/story?action=list">Xoá tất cả</a>
    </div>
</c:if>

<c:choose>
    <c:when test="${not empty stories}">
        <div class="story-grid">
            <c:forEach var="story" items="${stories}">
                <%@ include file="/WEB-INF/views/story/_card.jsp" %>
            </c:forEach>
        </div>

        <%--
          MẢNH: phân trang.
          pgQuery giữ lại tag / từ khoá / sắp xếp / tình trạng khi chuyển
          trang — không thì bấm sang trang 2 là mất sạch bộ lọc.
        --%>
        <c:set var="pgBase" value="story?action=list" scope="request"/>
        <c:set var="pgQuery" value="${keepTag}${keepQ}${keepSort}${keepProg}" scope="request"/>
        <%@ include file="/WEB-INF/views/_partials/_pagination.jsp" %>
    </c:when>

    <c:otherwise>
        <%-- MẢNH: trạng thái rỗng --%>
        <c:set var="emIcon"  value="🔍"/>
        <c:set var="emTitle" value="Không tìm thấy truyện nào"/>
        <c:set var="emText"  value="Thử bỏ bớt bộ lọc, hoặc tìm bằng từ khoá khác."/>
        <c:set var="emUrl"   value="/story?action=list"/>
        <c:set var="emBtn"   value="Xem tất cả truyện"/>
        <%@ include file="/WEB-INF/views/_partials/_empty.jsp" %>
    </c:otherwise>
</c:choose>
