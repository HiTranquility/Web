<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
================================================================================
  _pagination.jsp — MẢNH TÁI DÙNG: thanh phân trang
================================================================================
  TẦNG: views/  (chỉ hiển thị, không có logic nghiệp vụ, không gọi DAO)

  DÙNG Ở: story/list.jsp · user/profile.jsp · story/rank.jsp
          admin/stories.jsp · admin/users.jsp

  CÁCH DÙNG — đặt biến rồi include:

      <c:set var="pgBase"  value="story?action=list" scope="request"/>
      <c:set var="pgQuery" value="&tag=tien-hiep"    scope="request"/>
      <%@ include file="/WEB-INF/views/_partials/_pagination.jsp" %>

  BIẾN CẦN CÓ (servlet hoặc trang cha đặt):
      page        int     trang hiện tại, bắt đầu từ 1
      totalPages  int     tổng số trang
      pgBase      String  phần URL trước dấu ? — ví dụ "story?action=list"
      pgQuery     String  tham số cần GIỮ khi chuyển trang (có thể rỗng)

  VÌ SAO PHẢI CÓ pgQuery:
    Không giữ lại tag/từ khoá/sắp xếp thì bấm sang trang 2 là MẤT bộ lọc —
    người dùng đang lọc "Tiên hiệp" bỗng thấy toàn bộ kho truyện. Đây là lỗi
    rất hay gặp và rất khó chịu.
================================================================================
--%>
<c:if test="${totalPages > 1}">
    <nav class="pager" aria-label="Phân trang">

        <%-- Trang đầu: chỉ hiện khi đang ở trang 3 trở đi, để nhảy nhanh về --%>
        <c:if test="${page > 2}">
            <a class="btn btn-ghost btn-sm"
               href="${pageContext.request.contextPath}/${pgBase}${pgQuery}&amp;page=1">« Đầu</a>
        </c:if>

        <c:if test="${page > 1}">
            <a class="btn btn-ghost btn-sm"
               href="${pageContext.request.contextPath}/${pgBase}${pgQuery}&amp;page=${page - 1}">‹ Trước</a>
        </c:if>

        <span class="pager-info">Trang <b>${page}</b> / ${totalPages}</span>

        <c:if test="${page < totalPages}">
            <a class="btn btn-ghost btn-sm"
               href="${pageContext.request.contextPath}/${pgBase}${pgQuery}&amp;page=${page + 1}">Sau ›</a>
        </c:if>

        <c:if test="${page < totalPages - 1}">
            <a class="btn btn-ghost btn-sm"
               href="${pageContext.request.contextPath}/${pgBase}${pgQuery}&amp;page=${totalPages}">Cuối »</a>
        </c:if>
    </nav>
</c:if>
