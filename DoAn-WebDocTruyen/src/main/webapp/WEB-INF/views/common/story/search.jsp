<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
================================================================================
  story/search.jsp — MẢNH NỘI DUNG: Tìm sâu trong nội dung chương   TRANG 2b
================================================================================
  TẦNG: views/

  Nhận: keyword (String) · results (List<Chapter> đã cắt sẵn đoạn trích)

  KẾT QUẢ LÀ CHƯƠNG, KHÔNG PHẢI TRUYỆN
    Người gõ một câu văn vào đây đang muốn biết "câu đó nằm ở chương nào".
    Trả về tên truyện thì họ vẫn phải mở từng chương ra dò lại — đúng cái
    việc mà họ nhờ máy làm hộ.

  ĐOẠN TRÍCH DO TẦNG DAO CẮT, không cắt ở đây.
    Nội dung đầy đủ một chương là vài nghìn chữ; 30 kết quả là vài trăm KB
    HTML cho một trang danh sách. ChapterDAO.snippet() cắt ngay khi dữ liệu
    còn chưa rời khỏi tầng dao.
================================================================================
--%>
<div class="section-head">
    <h2>🔎 Tìm trong nội dung truyện</h2>
    <c:if test="${not empty keyword}">
        <span class="more">${results.size()} kết quả</span>
    </c:if>
</div>

<%-- Hai chế độ tìm, cùng một ô nhập. method="get" nên kết quả có URL riêng,
     chia sẻ và lưu lại được. --%>
<form action="${pageContext.request.contextPath}/story" method="get" class="search-bar">
    <input type="hidden" name="action" value="search">
    <input type="text" name="q" placeholder="Gõ một câu hoặc một từ trong truyện…"
           value="<c:out value='${keyword}'/>" autofocus>
    <button type="submit" class="btn btn-primary btn-sm">Tìm</button>
</form>

<div class="filter-bar">
    <div class="filter-group">
        <span class="filter-label">Tìm theo</span>
        <a class="chip"
           href="${pageContext.request.contextPath}/story?action=list&amp;q=${keyword}">
            Tên truyện / tác giả</a>
        <a class="chip is-on"
           href="${pageContext.request.contextPath}/story?action=search&amp;q=${keyword}">
            Nội dung chương</a>
    </div>
</div>

<c:choose>
    <c:when test="${not empty results}">
        <div class="hit-list">
            <c:forEach var="ch" items="${results}">
                <a class="hit"
                   href="${pageContext.request.contextPath}/chapter?action=read&amp;id=${ch.id}">
                    <div class="hit-head">
                        <span class="hit-story"><c:out value="${ch.storyTitle}"/></span>
                        <span class="hit-chapter">
                            Chương ${ch.chapterNo} · <c:out value="${ch.title}"/>
                        </span>
                    </div>

                    <%-- c:out BẮT BUỘC: đây là chữ tác giả viết. Đoạn trích đã
                         bỏ xuống dòng nên hiện gọn trong hai dòng. --%>
                    <p class="hit-snippet"><c:out value="${ch.content}"/></p>
                </a>
            </c:forEach>
        </div>

        <p class="muted-note" style="margin-top:22px">
            Tối đa 30 kết quả, xếp theo mức độ liên quan.
            Không thấy thứ cần tìm thì thử một cụm từ dài hơn — cụm dài lọc
            chính xác hơn một từ đơn.
        </p>
    </c:when>

    <c:when test="${not empty keyword}">
        <%--
          Rỗng ở đây có một nguyên nhân dễ gây hiểu nhầm nên phải nói ra:
          MySQL mặc định không đưa từ ngắn hơn 3 ký tự vào chỉ mục toàn văn.
          ChapterDAO đã tự chuyển sang LIKE cho trường hợp đó, nhưng vẫn nên
          gợi ý người dùng thử cụm dài hơn.
        --%>
        <c:set var="emIcon"  value="🔎"/>
        <c:set var="emTitle" value="Không tìm thấy đoạn nào"/>
        <c:set var="emText"  value="Thử một cụm từ khác, hoặc dài hơn. Cũng có thể thứ bạn tìm nằm ở tên truyện — bấm “Tên truyện / tác giả” phía trên."/>
        <c:set var="emUrl"   value="/story?action=list"/>
        <c:set var="emBtn"   value="Về kho truyện"/>
        <%@ include file="/WEB-INF/views/_partials/_empty.jsp" %>
    </c:when>

    <c:otherwise>
        <c:set var="emIcon"  value="📖"/>
        <c:set var="emTitle" value="Tìm một câu bạn nhớ"/>
        <c:set var="emText"  value="Chức năng này đọc bên trong nội dung từng chương, không chỉ tên truyện. Hợp khi bạn nhớ một câu nhưng quên nó ở chương nào."/>
        <%@ include file="/WEB-INF/views/_partials/_empty.jsp" %>
    </c:otherwise>
</c:choose>
