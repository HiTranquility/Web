<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
================================================================================
  user/notifications.jsp — MẢNH NỘI DUNG: Thông báo            TRANG 18
================================================================================
  TẦNG: views/

  Nhận: notifications (List<Notification>)

  Trạng thái "chưa đọc" trong danh sách này là ảnh chụp LÚC MỞ TRANG.
  NotificationServlet đọc danh sách trước rồi mới đánh dấu đã đọc, nên lần
  đầu vào vẫn thấy cái nào mới; tải lại trang thì tất cả đã xám.
================================================================================
--%>
<div class="section-head">
    <h2>🔔 Thông báo</h2>
    <span class="more">${empty notifications ? 0 : notifications.size()} thông báo</span>
</div>

<c:choose>
    <c:when test="${not empty notifications}">
        <div class="notif-list">
            <c:forEach var="n" items="${notifications}">
                <%--
                  Thông báo có chương thì bấm vào đọc luôn chương đó.
                  Không có (loại SYSTEM) thì là một khối tĩnh, không phải link
                  dẫn đi đâu cả — link chết còn khó chịu hơn không có link.
                --%>
                <c:choose>
                    <c:when test="${not empty n.chapterId}">
                        <a class="notif ${n.read ? '' : 'notif-new'}"
                           href="${pageContext.request.contextPath}/chapter?action=read&amp;id=${n.chapterId}">
                            <span class="notif-icon">${n.icon}</span>
                            <span class="notif-body">
                                <span class="notif-text"><c:out value="${n.message}"/></span>
                                <span class="notif-time">
                                    <c:if test="${not empty n.createdAt}">
                                        ${n.createdAt.dayOfMonth}/${n.createdAt.monthValue}
                                        lúc ${n.createdAt.hour}:${n.createdAt.minute lt 10 ? '0' : ''}${n.createdAt.minute}
                                    </c:if>
                                </span>
                            </span>
                            <c:if test="${not n.read}"><span class="notif-dot"></span></c:if>
                        </a>
                    </c:when>
                    <c:when test="${not empty n.storyId}">
                        <a class="notif ${n.read ? '' : 'notif-new'}"
                           href="${pageContext.request.contextPath}/story?action=detail&amp;id=${n.storyId}#comments">
                            <span class="notif-icon">${n.icon}</span>
                            <span class="notif-body">
                                <span class="notif-text"><c:out value="${n.message}"/></span>
                                <span class="notif-time">
                                    <c:if test="${not empty n.createdAt}">
                                        ${n.createdAt.dayOfMonth}/${n.createdAt.monthValue}
                                        lúc ${n.createdAt.hour}:${n.createdAt.minute lt 10 ? '0' : ''}${n.createdAt.minute}
                                    </c:if>
                                </span>
                            </span>
                            <c:if test="${not n.read}"><span class="notif-dot"></span></c:if>
                        </a>
                    </c:when>
                    <c:otherwise>
                        <div class="notif ${n.read ? '' : 'notif-new'}">
                            <span class="notif-icon">${n.icon}</span>
                            <span class="notif-body">
                                <span class="notif-text"><c:out value="${n.message}"/></span>
                                <span class="notif-time">
                                    <c:if test="${not empty n.createdAt}">
                                        ${n.createdAt.dayOfMonth}/${n.createdAt.monthValue}
                                    </c:if>
                                </span>
                            </span>
                        </div>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
        </div>
    </c:when>

    <c:otherwise>
        <c:set var="emIcon"  value="🔔"/>
        <c:set var="emTitle" value="Chưa có thông báo nào"/>
        <c:set var="emText"  value="Theo dõi một tác giả để được báo khi họ đăng chương mới."/>
        <c:set var="emUrl"   value="/follow?action=list"/>
        <c:set var="emBtn"   value="Xem đang theo dõi"/>
        <%@ include file="/WEB-INF/views/_partials/_empty.jsp" %>
    </c:otherwise>
</c:choose>
