<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %><%@
    taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@
    taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %><%@
    taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %><%--
================================================================================
  story/recommend.jsp — TRẢ VỀ TRẦN, không qua layout nào   ?action=recommend
================================================================================
  Trang đọc gọi bằng fetch() khi người đọc tới cuối chương cuối cùng, rồi
  chèn thẳng vào dưới dòng "Hết truyện". Cùng kiểu với chapter/raw.jsp:
  không <!DOCTYPE>, không <html> — chỉ đúng khối gợi ý.

  Dùng lại _recommend.jsp nên thẻ ở đây GIỐNG HỆT thẻ ở trang chi tiết.
================================================================================
--%><%@ include file="/WEB-INF/views/_partials/_recommend.jsp" %>
