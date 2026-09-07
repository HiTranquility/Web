<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  error403.jsp — TRANG PHÓNG cho lỗi 403.                    TRANG 9
  TẦNG: views/

  Tomcat forward THẲNG tới đây (khai trong web.xml), không đi qua servlet nào —
  nên không ai đặt hộ contentPage. File này tự đặt rồi gọi layout.
--%>
<c:set var="pageTitle" value="Không có quyền truy cập" scope="request"/>
<c:set var="contentPage" value="/WEB-INF/views/page/_error403.jsp" scope="request"/>
<jsp:include page="/WEB-INF/views/layout/main.jsp"/>
