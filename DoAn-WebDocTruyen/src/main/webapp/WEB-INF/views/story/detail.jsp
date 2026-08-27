<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
  story/detail.jsp — MẢNH nội dung. Chi tiết truyện.        CASE 04 + 07 + 08
  Nhận: story · tags · chapters · comments · canEdit · bookmarked
--%>
<div class="story-detail">
    <div class="detail-cover">
        <c:choose>
            <c:when test="${not empty story.coverUrl}">
                <img src="<c:out value='${story.coverUrl}'/>"
                     alt="<c:out value='${story.title}'/>">
            </c:when>
            <c:otherwise>
                <div class="cover-fallback">${story.initial}</div>
            </c:otherwise>
        </c:choose>
    </div>

    <div class="detail-info">
        <h1><c:out value="${story.title}"/></h1>

        <p class="detail-author">
            ✍️ <c:out value="${story.authorName}"/>
            <c:if test="${story.status eq 'DRAFT'}">
                <span class="pill pill-warn" style="margin-left:8px">Bản nháp</span>
            </c:if>
        </p>

        <div class="tag-row" style="margin:14px 0">
            <c:forEach var="t" items="${tags}">
                <a class="tag"
                   href="${pageContext.request.contextPath}/story?action=list&amp;tag=${t.slug}">
                    <c:out value="${t.name}"/></a>
            </c:forEach>
        </div>

        <div class="detail-stats">
            <span>📄 ${story.chapterCount} chương</span>
            <span>👁️ ${story.viewCount} lượt xem</span>
            <span class="${story.completed ? 'yes' : ''}">
                ${story.completed ? '✓ Hoàn thành' : '⏳ Đang ra'}</span>
        </div>

        <p class="detail-desc"><c:out value="${story.description}"/></p>

        <div class="detail-actions">
            <c:if test="${not empty chapters}">
                <a class="btn btn-primary"
                   href="${pageContext.request.contextPath}/chapter?action=read&amp;id=${chapters[0].id}">
                    Đọc từ đầu</a>
            </c:if>

            <%-- Nút lưu/bỏ lưu. Chỉ hiện khi đã đăng nhập.
                 Dùng <form method="post"> vì đây là hành động GHI. --%>
            <c:if test="${not empty currentUser}">
                <form action="${pageContext.request.contextPath}/bookmark" method="post"
                      style="display:inline">
                    <input type="hidden" name="action" value="${bookmarked ? 'remove' : 'add'}">
                    <input type="hidden" name="storyId" value="${story.id}">
                    <button type="submit" class="btn btn-ghost">
                        ${bookmarked ? '★ Đã lưu' : '☆ Lưu truyện'}
                    </button>
                </form>
            </c:if>

            <a class="btn btn-ghost"
               href="${pageContext.request.contextPath}/download?storyId=${story.id}">
                ⬇ Tải .txt</a>

            <%-- canEdit do servlet tính: chủ truyện HOẶC admin --%>
            <c:if test="${canEdit}">
                <a class="btn btn-ghost"
                   href="${pageContext.request.contextPath}/story?action=edit&amp;id=${story.id}">Sửa</a>
                <a class="btn btn-ghost"
                   href="${pageContext.request.contextPath}/chapter?action=create&amp;storyId=${story.id}">+ Thêm chương</a>
            </c:if>
        </div>
    </div>
</div>

<%-- ---- Mục lục chương ---- --%>
<div class="section-head"><h2>Danh sách chương</h2></div>
<c:choose>
    <c:when test="${not empty chapters}">
        <ol class="chapter-list">
            <c:forEach var="ch" items="${chapters}">
                <li>
                    <a href="${pageContext.request.contextPath}/chapter?action=read&amp;id=${ch.id}">
                        <span class="ch-no">Chương ${ch.chapterNo}</span>
                        <span class="ch-title"><c:out value="${ch.title}"/></span>
                    </a>
                    <c:if test="${canEdit}">
                        <a class="ch-edit"
                           href="${pageContext.request.contextPath}/chapter?action=edit&amp;id=${ch.id}">sửa</a>
                    </c:if>
                </li>
            </c:forEach>
        </ol>
    </c:when>
    <c:otherwise>
        <div class="empty" style="padding:36px">
            <p>Truyện chưa có chương nào.</p>
        </div>
    </c:otherwise>
</c:choose>

<%-- ---- Bình luận ---- --%>
<div class="section-head" id="comments">
    <h2>Bình luận (${fn:length(comments)})</h2>
</div>

<c:choose>
    <c:when test="${not empty currentUser}">
        <form action="${pageContext.request.contextPath}/comment" method="post"
              class="comment-form">
            <input type="hidden" name="action" value="add">
            <input type="hidden" name="storyId" value="${story.id}">
            <textarea name="content" rows="3" maxlength="1000" required
                      placeholder="Viết bình luận… Nhớ giữ lời lẽ văn minh."></textarea>
            <button type="submit" class="btn btn-primary btn-sm">Gửi bình luận</button>
        </form>
    </c:when>
    <c:otherwise>
        <p class="muted-note">
            <a href="${pageContext.request.contextPath}/auth?action=login">Đăng nhập</a>
            để bình luận.
        </p>
    </c:otherwise>
</c:choose>

<c:forEach var="cm" items="${comments}">
    <div class="comment">
        <span class="user-avatar">${cm.initial}</span>
        <div class="comment-body">
            <div class="comment-head">
                <b><c:out value="${cm.name}"/></b>
                <%-- Chủ bình luận hoặc admin mới thấy nút gỡ --%>
                <c:if test="${not empty currentUser and (currentUser.id eq cm.userId or currentUser.admin)}">
                    <form action="${pageContext.request.contextPath}/comment" method="post"
                          style="display:inline">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="id" value="${cm.id}">
                        <input type="hidden" name="storyId" value="${story.id}">
                        <button type="submit" class="link-danger">gỡ</button>
                    </form>
                </c:if>
            </div>
            <%-- <c:out> BẮT BUỘC — bình luận là chữ người dùng nhập --%>
            <p><c:out value="${cm.content}"/></p>
        </div>
    </div>
</c:forEach>
