<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
================================================================================
  _tag-filter.jsp — MẢNH TÁI DÙNG: hàng nút lọc thể loại
================================================================================
  TẦNG: views/

  DÙNG Ở: story/list.jsp · story/rank.jsp · user/profile.jsp

  BIẾN CẦN CÓ (servlet đặt):
      tags        List<Tag>  danh sách thể loại, mỗi Tag có storyCount
      currentTag  String     slug đang được chọn, null = "Tất cả"
      tfBase      String     URL gốc, ví dụ "story?action=list"

  DỮ LIỆU TỪ ĐÂU: servlet gọi TagDAO.findAllWithCount() rồi setAttribute.
  Mảnh này KHÔNG tự truy vấn — JSP không được gọi DAO (quy tắc tầng views).
================================================================================
--%>
<c:if test="${not empty tags}">
    <div class="tag-row" style="margin-bottom:22px">
        <a class="tag ${empty currentTag ? 'is-on' : ''}"
           href="${pageContext.request.contextPath}/${tfBase}">Tất cả</a>

        <c:forEach var="t" items="${tags}">
            <a class="tag ${currentTag eq t.slug ? 'is-on' : ''}"
               href="${pageContext.request.contextPath}/${tfBase}&amp;tag=${t.slug}">
                <c:out value="${t.name}"/>
                <c:if test="${t.storyCount > 0}"> (${t.storyCount})</c:if>
            </a>
        </c:forEach>
    </div>
</c:if>
