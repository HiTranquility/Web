<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
================================================================================
  _avatar.jsp — MẢNH TÁI DÙNG: Ảnh đại diện người dùng (hoặc chữ cái đầu thay thế)
================================================================================
  TẦNG: views/_partials/
  Nhận các tham số:
    - avUrl: đường dẫn ảnh đại diện (User.avatarUrl)
    - avAlt: tên hiển thị / username dùng cho thẻ alt
    - avInitial: chữ cái đầu (User.initial)
    - avClass: CSS class (mặc định 'user-avatar', hoặc 'user-avatar-lg', 'profile-avatar')
================================================================================
--%>
<c:set var="_avatarClass" value="${empty avClass ? 'user-avatar' : avClass}"/>
<c:set var="_avatarSize" value="width:28px;height:28px;min-width:28px;min-height:28px;max-width:28px;max-height:28px;"/>
<c:if test="${fn:contains(_avatarClass, 'profile-avatar')}">
    <c:set var="_avatarSize" value="width:84px;height:84px;min-width:84px;min-height:84px;max-width:84px;max-height:84px;"/>
</c:if>
<c:if test="${fn:contains(_avatarClass, 'user-avatar-lg')}">
    <c:set var="_avatarSize" value="width:38px;height:38px;min-width:38px;min-height:38px;max-width:38px;max-height:38px;"/>
</c:if>
<c:choose>
    <c:when test="${not empty avUrl}">
        <c:set var="_avatarSrc" value="${fn:startsWith(avUrl, '/') ? pageContext.request.contextPath.concat(avUrl) : avUrl}"/>
        <span class="${_avatarClass}" style="${_avatarSize} overflow:hidden !important; display:inline-grid; place-items:center; position:relative; border-radius:50%; flex-shrink:0;">
            <img src="<c:out value='${_avatarSrc}'/>" alt="<c:out value='${avAlt}'/>" loading="lazy"
                 style="width:100% !important; height:100% !important; max-width:100% !important; max-height:100% !important; object-fit:cover !important; border-radius:inherit; display:block;"
                 onerror="this.style.display='none'; if(this.nextElementSibling) this.nextElementSibling.style.display='grid';">
            <span class="avatar-fallback" style="display:none;width:100%;height:100%;place-items:center;">${avInitial}</span>
        </span>
    </c:when>
    <c:otherwise>
        <span class="${_avatarClass}" style="${_avatarSize} overflow:hidden; border-radius:50%; flex-shrink:0;">${avInitial}</span>
    </c:otherwise>
</c:choose>

