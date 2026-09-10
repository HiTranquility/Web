<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--
================================================================================
  user/profile.jsp — MẢNH NỘI DUNG: Trang tác giả              TRANG 4
================================================================================
  TẦNG: views/ — chỉ hiển thị. Dữ liệu do UserServlet chuẩn bị sẵn.

  Nhận: author (User) · stories · totalStories · totalViews · page · totalPages
        followerCount · isFollowing

  Dùng khung layout/main.jsp — vẫn có nav và footer như trang thường.
  Đúng nhánh 5 của bảng quyết định chọn layout: không rơi vào khung đặc biệt
  nào thì dùng main, không tạo layout mới.
================================================================================
--%>
<div class="profile-head">
    <span class="profile-avatar">${author.initial}</span>

    <div class="profile-info">
        <h1><c:out value="${author.name}"/></h1>
        <p class="profile-username">@<c:out value="${author.username}"/></p>

        <c:if test="${not empty author.bio}">
            <p class="profile-bio"><c:out value="${author.bio}"/></p>
        </c:if>

        <div class="profile-stats">
            <span><b>${totalStories}</b> truyện</span>
            <span><b><fmt:formatNumber pattern="#,##0" value="${totalViews}"/></b> lượt xem</span>
            <span><b>${followerCount}</b> người theo dõi</span>
            <c:if test="${author.admin}">
                <span class="pill pill-warn">Quản trị viên</span>
            </c:if>
        </div>

        <%--
          NÚT THEO DÕI.

          Không hiện với khách chưa đăng nhập (theo dõi cần biết ai theo dõi
          ai) và không hiện trên hồ sơ của chính mình — tự theo dõi mình thì
          thông báo chương mới sẽ báo lại chính truyện mình vừa đăng.

          isFollowing do UserServlet.profile() đặt, và CHỈ đặt trong đúng hai
          trường hợp trên. Ở các trường hợp khác biến này rỗng, mà EL coi rỗng
          là false — nên điều kiện c:if bên ngoài mới là chỗ quyết định, không
          phải giá trị của isFollowing.

          back="profile": FollowServlet cần biết đường quay về. Thiếu tham số
          này thì bấm xong vẫn về đúng trang hồ sơ (đó là nhánh mặc định),
          nhưng ghi rõ ra thì sau này đổi mặc định không làm hỏng chỗ này.
        --%>
        <c:if test="${not empty currentUser and currentUser.id ne author.id}">
            <form method="post" class="profile-follow"
                  action="${pageContext.request.contextPath}/follow">
                <input type="hidden" name="do" value="${isFollowing ? 'unfollow' : 'follow'}">
                <input type="hidden" name="authorId" value="${author.id}">
                <input type="hidden" name="back" value="profile">
                <button type="submit" class="btn ${isFollowing ? 'btn-ghost' : 'btn-primary'}">
                    ${isFollowing ? '✓ Đang theo dõi' : '+ Theo dõi'}
                </button>
            </form>
        </c:if>
    </div>
</div>

<div class="section-head">
    <h2>Truyện đã đăng</h2>
    <span class="more">${totalStories} truyện</span>
</div>

<c:choose>
    <c:when test="${not empty stories}">
        <div class="story-grid">
            <c:forEach var="story" items="${stories}">
                <%@ include file="/WEB-INF/views/story/_card.jsp" %>
            </c:forEach>
        </div>

        <%-- MẢNH: phân trang. pgQuery giữ id tác giả khi chuyển trang. --%>
        <c:set var="pgBase"  value="user?action=profile" scope="request"/>
        <c:set var="pgQuery" value="&id=${author.id}" scope="request"/>
        <%@ include file="/WEB-INF/views/_partials/_pagination.jsp" %>
    </c:when>

    <c:otherwise>
        <%-- MẢNH: trạng thái rỗng --%>
        <c:set var="emIcon"  value="📖"/>
        <c:set var="emTitle" value="Tác giả chưa đăng truyện nào"/>
        <c:set var="emText"  value="Quay lại sau nhé, biết đâu sẽ có truyện mới."/>
        <c:set var="emUrl"   value="/story?action=list"/>
        <c:set var="emBtn"   value="Xem kho truyện"/>
        <%@ include file="/WEB-INF/views/_partials/_empty.jsp" %>
    </c:otherwise>
</c:choose>
