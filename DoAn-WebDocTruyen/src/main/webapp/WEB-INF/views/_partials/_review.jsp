<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--
================================================================================
  _review.jsp — MẢNH TÁI DÙNG: một bài đánh giá chi tiết (ISSUE-022)
================================================================================
  Nhận: r (đối tượng Review) · currentUser
================================================================================
--%>
<div class="review-card ${r.hasSpoiler ? 'has-spoiler' : ''}" id="review-${r.id}">
    <div class="review-header">
        <div class="review-author">
            <span class="avatar avatar-sm">${empty r.userAvatar ? r.username.substring(0, 1).toUpperCase() : ''}</span>
            <div class="review-meta">
                <strong class="review-author-name"><c:out value="${empty r.userDisplayName ? r.username : r.userDisplayName}"/></strong>
                <span class="review-date muted small">
                    <c:choose>
                        <c:when test="${not empty r.createdAt}">
                            ${r.createdAt.toLocalDate()}
                        </c:when>
                        <c:otherwise>Vừa xong</c:otherwise>
                    </c:choose>
                </span>
            </div>
        </div>
        <div class="review-stars text-amber">
            <c:forEach begin="1" end="5" var="s">
                <span class="star">${s <= r.score ? '★' : '☆'}</span>
            </c:forEach>
            <span class="score-text">(${r.score}/5)</span>
        </div>
    </div>

    <h4 class="review-title"><c:out value="${r.title}"/></h4>

    <c:choose>
        <c:when test="${r.hasSpoiler}">
            <div class="spoiler-wrapper">
                <div class="spoiler-warning">
                    <span>⚠️ Bài đánh giá có tiết lộ tình tiết truyện (Spoiler)</span>
                    <button type="button" class="btn btn-sm btn-outline spoiler-toggle-btn"
                            onclick="var p = this.closest('.spoiler-wrapper'); p.classList.add('revealed'); this.remove();">
                        Hiển thị nội dung
                    </button>
                </div>
                <div class="review-content spoiler-body">
                    <p><c:out value="${r.content}"/></p>
                </div>
            </div>
        </c:when>
        <c:otherwise>
            <div class="review-content">
                <p><c:out value="${r.content}"/></p>
            </div>
        </c:otherwise>
    </c:choose>

    <div class="review-footer">
        <form action="${pageContext.request.contextPath}/rating" method="post" class="review-vote-form" style="display:inline">
            <input type="hidden" name="_csrf" value="${csrfToken}">
            <input type="hidden" name="action" value="vote">
            <input type="hidden" name="reviewId" value="${r.id}">
            <input type="hidden" name="storyId" value="${r.storyId}">
            <button type="submit" class="btn btn-sm ${r.votedByMe ? 'btn-primary' : 'btn-ghost'} btn-helpful-vote"
                    title="${r.votedByMe ? 'Bỏ bấm hữu ích' : 'Bấm nếu bạn thấy đánh giá này có ích'}">
                👍 Có ích (<c:out value="${r.helpfulCount}"/>)
            </button>
        </form>
    </div>
</div>
