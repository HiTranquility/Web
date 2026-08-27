<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
================================================================================
  layout/reader.jsp — KHUNG TRANG ĐỌC CHƯƠNG              (CASE 06)
================================================================================
  Đây là ví dụ rõ nhất cho luật "layout mới CHỈ khi KHUNG khác".

  Khác main.jsp ở ba điểm, và cả ba đều phục vụ đúng một mục tiêu — không có
  gì phân tán khi đang đọc:
    - Thanh trên tối giản: chỉ nút quay lại + tên truyện. KHÔNG có menu.
    - KHÔNG có footer nhiều cột.
    - Nội dung hẹp (~38em) và chữ to hơn — mắt không phải quét ngang quá dài.

  Nhét ba khác biệt này vào main.jsp bằng <c:if> thì file đó đầy điều kiện.
  Tách file rẻ hơn nhiều.
================================================================================
--%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <c:set var="layoutCss" value="layout-reader" scope="request"/>
    <%@ include file="parts/head.jsp" %>
</head>
<body class="reader-body">

<header class="reader-bar">
    <a class="reader-back"
       href="${pageContext.request.contextPath}/story?action=detail&amp;id=${story.id}">
        &larr; <c:out value="${story.title}"/>
    </a>
    <span class="reader-meta">
        Chương ${chapter.chapterNo} &middot; ~${chapter.readMinutes} phút đọc
    </span>
</header>

<main class="reader-wrap">
    <jsp:include page="${contentPage}" />
</main>

</body>
</html>
