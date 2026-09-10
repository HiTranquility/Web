<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
================================================================================
  user/following.jsp — MẢNH NỘI DUNG: Đang theo dõi            TRANG 17
================================================================================
  TẦNG: views/

  Nhận: following (List<Follow>)
================================================================================
--%>
<div class="section-head">
    <h2>👥 Đang theo dõi</h2>
    <span class="more">${empty following ? 0 : following.size()} tác giả</span>
</div>

<p class="muted-note" style="margin-bottom:20px">
    Tác giả bạn theo dõi đăng chương mới thì bạn nhận
    <a href="${pageContext.request.contextPath}/notification">thông báo</a>.
</p>

<c:choose>
    <c:when test="${not empty following}">
        <div class="author-list">
            <c:forEach var="f" items="${following}">
                <div class="author-row">
                    <a class="author-avatar"
                       href="${pageContext.request.contextPath}/user?action=profile&amp;id=${f.authorId}">
                        ${f.initial}</a>

                    <div class="author-body">
                        <a class="author-name"
                           href="${pageContext.request.contextPath}/user?action=profile&amp;id=${f.authorId}">
                            <c:out value="${f.authorName}"/></a>
                        <p class="author-meta">
                            @<c:out value="${f.authorUsername}"/>
                            &middot; ${f.authorStoryCount} truyện
                        </p>
                    </div>

                    <%-- back=list để FollowServlet biết đường trả về đúng đây,
                         thay vì ném sang trang hồ sơ tác giả --%>
                    <form method="post" class="author-action"
                          action="${pageContext.request.contextPath}/follow">
                        <input type="hidden" name="_csrf" value="${csrfToken}">
                        <input type="hidden" name="do" value="unfollow">
                        <input type="hidden" name="authorId" value="${f.authorId}">
                        <input type="hidden" name="back" value="list">
                        <button type="submit" class="btn btn-ghost btn-sm">Bỏ theo dõi</button>
                    </form>
                </div>
            </c:forEach>
        </div>
    </c:when>

    <c:otherwise>
        <c:set var="emIcon"  value="👥"/>
        <c:set var="emTitle" value="Chưa theo dõi ai"/>
        <c:set var="emText"  value="Vào trang một tác giả và bấm Theo dõi để nhận thông báo khi họ đăng chương mới."/>
        <c:set var="emUrl"   value="/story?action=list"/>
        <c:set var="emBtn"   value="Tìm truyện hay"/>
        <%@ include file="/WEB-INF/views/_partials/_empty.jsp" %>
    </c:otherwise>
</c:choose>
