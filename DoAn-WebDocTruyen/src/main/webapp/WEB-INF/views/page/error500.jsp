<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  error500.jsp — TRANG PHÓNG cho mọi exception.
  isErrorPage="true" PHẢI ở file này (file phóng), không phải ở mảnh —
  vì pageContext.exception chỉ tồn tại ở trang được container gọi trực tiếp.
--%>
<c:set var="pageTitle" value="Có lỗi xảy ra" scope="request"/>
<c:set var="errType" value="${pageContext.exception['class']}" scope="request"/>
<c:set var="errMsg" value="${pageContext.exception.message}" scope="request"/>
<c:set var="contentPage" value="/WEB-INF/views/page/_error500.jsp" scope="request"/>
<jsp:include page="/WEB-INF/views/layout/main.jsp"/>
