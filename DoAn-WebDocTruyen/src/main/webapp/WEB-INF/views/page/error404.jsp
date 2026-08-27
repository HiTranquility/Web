<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  error404.jsp — TRANG PHÓNG, chỉ 3 dòng.

  Trang lỗi do Tomcat forward thẳng tới (khai trong web.xml), không đi qua
  servlet nào cả — nên không ai đặt hộ contentPage. File này tự đặt rồi gọi
  layout, để trang lỗi dùng CHUNG khung với mọi trang khác.

  Nội dung thật nằm ở _error404.jsp.
--%>
<c:set var="pageTitle" value="Không tìm thấy trang" scope="request"/>
<c:set var="contentPage" value="/WEB-INF/views/page/_error404.jsp" scope="request"/>
<jsp:include page="/WEB-INF/views/layout/main.jsp"/>
