<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
================================================================================
  layout/auth.jsp — KHUNG TRANG ĐĂNG NHẬP / ĐĂNG KÝ          (dùng ở CASE 01)
================================================================================
  Khác main.jsp ở ba điểm, và đó chính là lý do phải có layout riêng:
    - KHÔNG có thanh menu (người chưa đăng nhập thì menu để làm gì)
    - KHÔNG có footer nhiều cột (gây phân tâm khỏi ô nhập)
    - Nội dung nằm giữa màn hình trong một thẻ card, không phải lưới rộng

  Nếu nhét mấy khác biệt này vào main.jsp bằng <c:if> thì file đó sẽ đầy điều
  kiện và ngày càng khó đọc. Tách file là rẻ hơn.

  Cách dùng giống hệt main.jsp:
      request.setAttribute("contentPage", "/WEB-INF/views/auth/login.jsp");
      forward("/WEB-INF/views/layout/auth.jsp");
================================================================================
--%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <c:set var="layoutCss" value="layout-auth" scope="request"/>
    <%@ include file="parts/head.jsp" %>
</head>
<body class="auth-body">

<div class="auth-wrap">
    <a href="${pageContext.request.contextPath}/" class="brand auth-brand">
        <span class="brand-mark">📖</span>
        <span>Đọc<em>Truyện</em></span>
    </a>

    <div class="auth-card">
        <jsp:include page="${contentPage}" />
    </div>

    <p class="auth-foot">
        <a href="${pageContext.request.contextPath}/page?name=rules">Nội quy</a>
        &nbsp;·&nbsp;
        <a href="${pageContext.request.contextPath}/">Về trang chủ</a>
    </p>
</div>

</body>
</html>
