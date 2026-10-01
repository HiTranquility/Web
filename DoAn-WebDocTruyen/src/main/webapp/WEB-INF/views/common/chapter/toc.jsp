<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
================================================================================
  chapter/toc.jsp — MỤC LỤC TRẦN, không khung trang
================================================================================
  TẦNG: views/

  DÙNG Ở: ChapterServlet action=toc — JavaScript ở trang đọc gọi bằng fetch()
          khi người dùng bấm mở bảng "Mục lục".

  Nhận: story · chapters · currentId

  KHÔNG CÓ <html>, <head>, <body>, KHÔNG LAYOUT
    Kết quả được nhét vào giữa một trang đang mở. Trả về nguyên trang HTML là
    có <html> lồng trong <html> — đúng cái bẫy đã mắc ở raw.jsp lần trước.

  contentType Ở ĐÂY LÀ BẮT BUỘC
    File này được trả thẳng cho fetch(), không đi qua head.jsp của layout —
    nơi khai charset cho các trang thường. Thiếu dòng này thì trình duyệt tự
    đoán bảng mã và tên chương tiếng Việt biến thành ký tự lạ.
================================================================================
--%>
<ul class="toc-list">
    <c:forEach var="ch" items="${chapters}">
        <%--
          Chương ĐANG đọc được đánh dấu và bỏ link.

          Bỏ link vì bấm vào chính chương đang đọc thì tải lại đúng trang đó —
          trông như bấm hụt. Đánh dấu để mắt tìm được ngay mình đang ở đâu
          trong một danh sách hàng trăm dòng.
        --%>
        <li class="toc-item ${ch.id eq currentId ? 'is-current' : ''}">
            <c:set var="isUnlocked" value="false" />
            <c:if test="${not empty unlockedChapterIds}">
                <c:forEach var="uId" items="${unlockedChapterIds}">
                    <c:if test="${uId eq ch.id}">
                        <c:set var="isUnlocked" value="true" />
                    </c:if>
                </c:forEach>
            </c:if>
            <c:choose>
                <c:when test="${ch.id eq currentId}">
                    <span class="toc-link">
                        <b class="toc-no">Chương ${ch.chapterNo}</b>
                        <span class="toc-title"><c:out value="${ch.title}"/></span>
                        <c:if test="${ch.vip}">
                            <c:choose>
                                <c:when test="${isUnlocked or (not empty sessionScope.currentUser and (sessionScope.currentUser.id eq story.authorId or sessionScope.currentUser.role eq 'ADMIN'))}">
                                    <span class="badge-unlocked">🔓 Đã mở</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge-vip">🔒 VIP <c:out value="${ch.coinPrice}"/> xu</span>
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                        <span class="toc-here">đang đọc</span>
                    </span>
                </c:when>
                <c:otherwise>
                    <a class="toc-link"
                       href="${pageContext.request.contextPath}/chapter?action=read&amp;id=${ch.id}">
                        <b class="toc-no">Chương ${ch.chapterNo}</b>
                        <span class="toc-title"><c:out value="${ch.title}"/></span>
                        <c:if test="${ch.vip}">
                            <c:choose>
                                <c:when test="${isUnlocked or (not empty sessionScope.currentUser and (sessionScope.currentUser.id eq story.authorId or sessionScope.currentUser.role eq 'ADMIN'))}">
                                    <span class="badge-unlocked">🔓 Đã mở</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge-vip">🔒 VIP <c:out value="${ch.coinPrice}"/> xu</span>
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                    </a>
                </c:otherwise>
            </c:choose>
        </li>
    </c:forEach>
</ul>
