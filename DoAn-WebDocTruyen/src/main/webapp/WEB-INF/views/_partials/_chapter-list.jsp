<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
================================================================================
  _chapter-list.jsp — MẢNH TÁI DÙNG: mục lục chương
================================================================================
  TẦNG: views/

  DÙNG Ở: trang 3 (chi tiết truyện).

          KHÔNG dùng ở trang 12 (ghi chú cũ ghi sai). Trang "Truyện của tôi"
          liệt kê TRUYỆN chứ không phải chương, nên nó dùng _story-row.

  Mảnh này chỉ vẽ DANH SÁCH. Thanh sắp xếp và phân trang nằm ở trang gọi nó
  (detail.jsp) — chúng phụ thuộc vào tham số URL của riêng trang đó.

  CÁCH DÙNG:
      <c:set var="clChapters" value="${chapters}"/>
      <c:set var="clStoryId"  value="${story.id}"/>
      <c:set var="clCanEdit"  value="${canEdit}"/>   (tuỳ chọn)
      <%@ include file="/WEB-INF/views/_partials/_chapter-list.jsp" %>

  clCanEdit bật thì mỗi hàng có thêm nút Sửa / Xoá. Cùng một danh sách, độc
  giả và tác giả nhìn thấy hai thứ khác nhau — nhưng KHÔNG phải hai file.
================================================================================
--%>
<c:choose>
    <c:when test="${not empty clChapters}">
        <div class="chapter-list">
            <c:forEach var="ch" items="${clChapters}">
                <div class="chapter-item">
                    <a class="chapter-link"
                       href="${pageContext.request.contextPath}/chapter?action=read&amp;id=${ch.id}">
                        <span class="chapter-no">Chương ${ch.chapterNo}</span>
                        <span class="chapter-title"><c:out value="${ch.title}"/></span>
                    </a>

                    <c:if test="${clCanEdit}">
                        <span class="chapter-actions">
                            <a class="btn btn-ghost btn-sm"
                               href="${pageContext.request.contextPath}/chapter?action=edit&amp;id=${ch.id}">Sửa</a>

                            <%-- Xoá là POST, và hỏi lại trước khi làm.
                                 Xoá chương KHÔNG khôi phục được — đây là chỗ
                                 duy nhất trong dự án xoá thật, vì chương không
                                 có cột status như truyện và bình luận. --%>
                            <form method="post" style="display:inline"
                                  action="${pageContext.request.contextPath}/chapter"
                                  onsubmit="return confirm('Xoá chương này? Không khôi phục được.')">
                                <input type="hidden" name="_csrf" value="${csrfToken}">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="id" value="${ch.id}">
                                <button type="submit" class="btn btn-danger btn-sm">Xoá</button>
                            </form>
                        </span>
                    </c:if>
                </div>
            </c:forEach>
        </div>
    </c:when>

    <c:otherwise>
        <c:set var="emIcon"  value="📄"/>
        <c:set var="emTitle" value="Chưa có chương nào"/>
        <c:set var="emText"  value="Truyện này chưa đăng chương đầu tiên."/>
        <%@ include file="/WEB-INF/views/_partials/_empty.jsp" %>
    </c:otherwise>
</c:choose>
