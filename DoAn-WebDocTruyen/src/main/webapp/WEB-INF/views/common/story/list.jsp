<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--
================================================================================
  story/list.jsp — MẢNH NỘI DUNG: Kho truyện                      TRANG 2
================================================================================
  TẦNG: views/  — chỉ hiển thị. Dữ liệu do StoryServlet chuẩn bị sẵn.

  Nhận từ servlet:
      stories · tags · currentTag · keyword · sort · progress · storyType
      keywordUrl · tagUrl   (bản đã mã hoá URL — CHỈ dùng hai biến này trong href)
      page · totalPages · totalStories

  MỘT TRANG GÁNH NĂM VIỆC: duyệt, lọc thể loại, lọc tình trạng, lọc loại
  truyện (chữ / tranh), tìm kiếm. Không tách trang riêng cho kết quả tìm kiếm
  — cùng giao diện, chỉ khác tham số URL. Tách ra là nhân đôi code mà không
  được gì.
================================================================================
--%>
<div class="section-head">
    <h2>
        <c:choose>
            <c:when test="${not empty keyword}">Tìm: "<c:out value='${keyword}'/>"</c:when>
            <c:when test="${not empty currentTag}">Lọc theo thể loại</c:when>
            <c:when test="${storyType eq 'comic'}">Truyện tranh</c:when>
            <c:when test="${storyType eq 'text'}">Truyện chữ</c:when>
            <c:otherwise>Kho truyện</c:otherwise>
        </c:choose>
    </h2>
    <span class="more">${totalStories} truyện</span>
</div>

<%--
  keepXxx = "&ten=giatri" của mọi bộ lọc ĐANG bật, để bấm đổi một bộ lọc
  không làm mất các bộ lọc còn lại.

  PHẢI ĐẶT TRƯỚC MỌI CHỖ DÙNG. Trước đây khối này nằm SAU hàng "Tìm theo",
  nên hai link ở đó luôn nhận chuỗi rỗng — đổi chế độ tìm là mất sạch bộ lọc.

  Giá trị người dùng gõ (từ khoá, thể loại) chỉ được ghép bản ĐÃ MÃ HOÁ URL
  (keywordUrl, tagUrl — servlet chuẩn bị). Ghép thẳng ${keyword} vào href là
  lỗ XSS: từ khoá chứa dấu " phá được thuộc tính href.
  progress / sort / storyType thì an toàn: chỉ được so với chuỗi hằng số, và
  servlet đã lọc storyType qua Story.filterType().
--%>
<c:set var="keepTag"  value="${not empty tagUrl ? '&tag='.concat(tagUrl) : ''}"/>
<c:set var="keepQ"    value="${not empty keywordUrl ? '&q='.concat(keywordUrl) : ''}"/>
<c:set var="keepSort" value="${sort eq 'popular' or sort eq 'rating' or sort eq 'updated' ? '&sort='.concat(sort) : ''}"/>
<c:set var="keepProg" value="${progress eq 'ongoing' or progress eq 'completed' ? '&progress='.concat(progress) : ''}"/>
<c:set var="keepType" value="${not empty storyType ? '&type='.concat(storyType) : ''}"/>
<c:set var="keepChap" value="${chapters eq 'short' or chapters eq 'medium' or chapters eq 'long' ? '&chapters='.concat(chapters) : ''}"/>

<div class="filter-panel">
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
        <c:if test="${not empty storyType}">
            <input type="hidden" name="type" value="${storyType}">
        </c:if>
        <c:if test="${not empty chapters}">
            <input type="hidden" name="chapters" value="${chapters}">
        </c:if>

        <input type="text" name="q" placeholder="Tìm theo tên truyện hoặc tên tác giả…"
               value="<c:out value='${keyword}'/>">
        <button type="submit" class="btn btn-primary btn-sm">Tìm kiếm</button>
    </form>

    <%-- Hai chế độ tìm. Đặt ngay dưới ô nhập, không giấu trong menu: người
         không tìm thấy bằng tên truyện cần nhìn thấy lối thứ hai NGAY LÚC ĐÓ. --%>
    <div class="filter-bar" style="margin-top:-4px">
        <div class="filter-group">
            <span class="filter-label">Tìm theo</span>
            <a class="chip ${empty searchIn or searchIn eq 'title' ? 'is-on' : ''}"
            href="${pageContext.request.contextPath}/story?action=list${keepTag}${keepProg}${keepSort}${keepType}${keepChap}${keepQ}">
                Tên truyện / tác giả</a>
            <%-- Tìm trong nội dung chỉ có nghĩa với truyện CHỮ — truyện tranh
                 là ảnh, không có chữ để tìm. --%>
            <a class="chip ${searchIn eq 'content' ? 'is-on' : ''}"
            href="${pageContext.request.contextPath}/story?action=search${keepQ}">
                🔎 Nội dung chương</a>
        </div>
    </div>

    <%-- MẢNH: bộ lọc thể loại. Giữ loại truyện và độ dài khi đổi thể loại. --%>
    <c:set var="tfBase" value="story?action=list${keepType}${keepChap}" scope="request"/>
    <%@ include file="/WEB-INF/views/_partials/_tag-filter.jsp" %>

    <%--
      CÁC HÀNG LỌC PHỤ: loại truyện, tình trạng, độ dài và cách sắp xếp.
    --%>
    <div class="filter-bar" style="margin-bottom:0">
        <div class="filter-group">
            <span class="filter-label">Loại truyện</span>
            <a class="chip ${empty storyType ? 'is-on' : ''}"
               href="${pageContext.request.contextPath}/story?action=list${keepTag}${keepQ}${keepSort}${keepProg}${keepChap}">Tất cả</a>
            <a class="chip ${storyType eq 'text' ? 'is-on' : ''}"
               href="${pageContext.request.contextPath}/story?action=list&amp;type=text${keepTag}${keepQ}${keepSort}${keepProg}${keepChap}">📖 Truyện chữ</a>
            <a class="chip ${storyType eq 'comic' ? 'is-on' : ''}"
               href="${pageContext.request.contextPath}/story?action=list&amp;type=comic${keepTag}${keepQ}${keepSort}${keepProg}${keepChap}">🎨 Truyện tranh</a>
        </div>

        <div class="filter-group">
            <span class="filter-label">Tình trạng</span>
            <a class="chip ${empty progress ? 'is-on' : ''}"
               href="${pageContext.request.contextPath}/story?action=list${keepTag}${keepQ}${keepSort}${keepType}${keepChap}">Tất cả</a>
            <a class="chip ${progress eq 'ongoing' ? 'is-on' : ''}"
               href="${pageContext.request.contextPath}/story?action=list&amp;progress=ongoing${keepTag}${keepQ}${keepSort}${keepType}${keepChap}">Đang ra</a>
            <a class="chip ${progress eq 'completed' ? 'is-on' : ''}"
               href="${pageContext.request.contextPath}/story?action=list&amp;progress=completed${keepTag}${keepQ}${keepSort}${keepType}${keepChap}">Hoàn thành</a>
        </div>

        <div class="filter-group">
            <span class="filter-label">Độ dài</span>
            <a class="chip ${empty chapters ? 'is-on' : ''}"
               href="${pageContext.request.contextPath}/story?action=list${keepTag}${keepQ}${keepSort}${keepProg}${keepType}">Tất cả</a>
            <a class="chip ${chapters eq 'short' ? 'is-on' : ''}"
               href="${pageContext.request.contextPath}/story?action=list&amp;chapters=short${keepTag}${keepQ}${keepSort}${keepProg}${keepType}">&lt; 10 chương</a>
            <a class="chip ${chapters eq 'medium' ? 'is-on' : ''}"
               href="${pageContext.request.contextPath}/story?action=list&amp;chapters=medium${keepTag}${keepQ}${keepSort}${keepProg}${keepType}">10 - 50 chương</a>
            <a class="chip ${chapters eq 'long' ? 'is-on' : ''}"
               href="${pageContext.request.contextPath}/story?action=list&amp;chapters=long${keepTag}${keepQ}${keepSort}${keepProg}${keepType}">&gt; 50 chương</a>
        </div>

        <div class="filter-group">
            <span class="filter-label">Sắp xếp</span>
            <a class="chip ${empty sort or sort eq 'updated' ? 'is-on' : ''}"
               href="${pageContext.request.contextPath}/story?action=list&amp;sort=updated${keepTag}${keepQ}${keepProg}${keepType}${keepChap}">Mới cập nhật</a>
            <a class="chip ${sort eq 'popular' ? 'is-on' : ''}"
               href="${pageContext.request.contextPath}/story?action=list&amp;sort=popular${keepTag}${keepQ}${keepProg}${keepType}${keepChap}">Đọc nhiều</a>
            <a class="chip ${sort eq 'rating' ? 'is-on' : ''}"
               href="${pageContext.request.contextPath}/story?action=list&amp;sort=rating${keepTag}${keepQ}${keepProg}${keepType}${keepChap}">Điểm cao</a>
        </div>
    </div>
</div>

<%--
================================================================================
  HÀNG "ĐANG LỌC" — mỗi điều kiện là một thẻ có dấu × để gỡ riêng
================================================================================
  VÌ SAO CẦN dù các chip ở trên đã tô sáng cái đang chọn
    Năm điều kiện nằm ở năm chỗ khác nhau trên trang. Muốn biết "mình đang lọc
    những gì" phải đưa mắt quét cả năm chỗ rồi tự ghép lại. Hàng này gom tất cả
    về một dòng, và cho gỡ TỪNG CÁI.

  Chỉ hiện khi CÓ lọc. Mỗi dấu × là một LINK dựng sẵn, bỏ đúng một tham số và
  giữ nguyên phần còn lại — dùng lại các biến keepXxx đã tính ở trên.
================================================================================
--%>
<c:if test="${not empty keyword or not empty currentTag or not empty storyType
              or not empty progress or not empty sort or not empty chapters}">
    <div class="active-filters">
        <span class="filter-label">Đang lọc</span>

        <c:if test="${not empty keyword}">
            <a class="chip chip-clear"
               href="${pageContext.request.contextPath}/story?action=list${keepTag}${keepSort}${keepProg}${keepType}${keepChap}">
                Từ khoá: <b><c:out value="${keyword}"/></b> <span class="chip-x">×</span></a>
        </c:if>

        <c:if test="${not empty currentTag}">
            <a class="chip chip-clear"
               href="${pageContext.request.contextPath}/story?action=list${keepQ}${keepSort}${keepProg}${keepType}${keepChap}">
                Thể loại: <b><c:out value="${currentTag}"/></b> <span class="chip-x">×</span></a>
        </c:if>

        <c:if test="${not empty storyType}">
            <a class="chip chip-clear"
               href="${pageContext.request.contextPath}/story?action=list${keepTag}${keepQ}${keepSort}${keepProg}${keepChap}">
                <b>${storyType eq 'comic' ? 'Truyện tranh' : 'Truyện chữ'}</b>
                <span class="chip-x">×</span></a>
        </c:if>

        <c:if test="${not empty progress}">
            <a class="chip chip-clear"
               href="${pageContext.request.contextPath}/story?action=list${keepTag}${keepQ}${keepSort}${keepType}${keepChap}">
                <b>${progress eq 'completed' ? 'Hoàn thành' : 'Đang ra'}</b>
                <span class="chip-x">×</span></a>
        </c:if>

        <c:if test="${not empty chapters}">
            <a class="chip chip-clear"
               href="${pageContext.request.contextPath}/story?action=list${keepTag}${keepQ}${keepSort}${keepProg}${keepType}">
                <b>${chapters eq 'short' ? '&lt; 10 chương' : (chapters eq 'medium' ? '10 - 50 chương' : '&gt; 50 chương')}</b>
                <span class="chip-x">×</span></a>
        </c:if>

        <%-- Sắp xếp chỉ hiện khi KHÁC mặc định: "Mới cập nhật" là thứ tự vốn
             có, gọi nó là một bộ lọc đang bật thì gây hiểu nhầm. --%>
        <c:if test="${not empty sort and sort ne 'updated'}">
            <a class="chip chip-clear"
               href="${pageContext.request.contextPath}/story?action=list${keepTag}${keepQ}${keepProg}${keepType}${keepChap}">
                <b>${sort eq 'popular' ? 'Đọc nhiều' : 'Điểm cao'}</b>
                <span class="chip-x">×</span></a>
        </c:if>

        <a class="link-clear-all" style="margin-left:auto"
           href="${pageContext.request.contextPath}/story?action=list">Xoá tất cả</a>
    </div>
</c:if>

<c:choose>
    <c:when test="${not empty stories}">
        <div class="story-grid">
            <c:forEach var="story" items="${stories}">
                <%@ include file="/WEB-INF/views/_partials/_card.jsp" %>
            </c:forEach>
        </div>

        <%--
          MẢNH: phân trang.
          pgQuery giữ lại mọi bộ lọc khi chuyển trang — không thì bấm sang
          trang 2 là mất sạch bộ lọc.
        --%>
        <c:set var="pgBase" value="story?action=list" scope="request"/>
        <c:set var="pgQuery" value="${keepTag}${keepQ}${keepSort}${keepProg}${keepType}${keepChap}" scope="request"/>
        <%@ include file="/WEB-INF/views/_partials/_pagination.jsp" %>
    </c:when>

    <c:otherwise>
        <%-- MẢNH: trạng thái rỗng --%>
        <c:set var="emIcon"  value="🔍"/>
        <c:set var="emTitle" value="${storyType eq 'comic' ? 'Chưa có truyện tranh nào' : 'Không tìm thấy truyện nào'}"/>
        <c:set var="emText"  value="${storyType eq 'comic' ? 'Kho truyện tranh đang chờ tác giả đầu tiên. Đăng truyện mới và chọn loại Truyện tranh nhé.' : 'Thử bỏ bớt bộ lọc, hoặc tìm bằng từ khoá khác.'}"/>
        <c:set var="emUrl"   value="/story?action=list"/>
        <c:set var="emBtn"   value="Xem tất cả truyện"/>
        <%@ include file="/WEB-INF/views/_partials/_empty.jsp" %>
    </c:otherwise>
</c:choose>
