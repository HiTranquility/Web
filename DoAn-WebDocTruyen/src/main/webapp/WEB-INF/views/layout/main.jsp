<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
================================================================================
  layout/main.jsp — KHUNG TRANG CHÍNH
================================================================================
  Đây là "layout": nó dựng khung HTML đầy đủ, rồi CHÈN nội dung trang vào giữa.

  CÁCH DÙNG — servlet gọi như sau:
      request.setAttribute("contentPage", "/WEB-INF/views/story/home.jsp");
      forward("/WEB-INF/views/layout/main.jsp");

  Trang nội dung (home.jsp, list.jsp...) chỉ là MẢNH: không có <html>, không có
  <head>, không có <body>. Nó chỉ chứa phần ruột.

  VÌ SAO LÀM KIỂU NÀY THAY VÌ header.jsp + footer.jsp NHƯ TRƯỚC
    Cách cũ: mỗi trang tự include header rồi include footer.
      -> thẻ <body> mở ở file này, đóng ở file kia. Quên một </div> là vỡ layout
         mà không có lỗi nào báo, IDE cũng không kiểm tra giúp được.
      -> mỗi trang phải nhớ include ĐÚNG CẶP. Lỡ dùng header của main với footer
         của auth là hỏng.
      -> thêm layout thứ 3, thứ 4 thì số cặp phải nhớ tăng theo.

    Cách này: thẻ mở và thẻ đóng nằm CÙNG MỘT FILE. Trang nội dung không cần
    biết gì về khung. Thêm layout mới = thêm một file như file này.

  BIẾN TRANG NỘI DUNG CÓ THỂ ĐẶT (đặt TRƯỚC khi forward, trong servlet):
      contentPage  BẮT BUỘC — đường dẫn tới mảnh nội dung
      pageTitle    tiêu đề tab trình duyệt
      activeNav    mục menu đang sáng: home | browse | rules
      pageCss      tên file CSS riêng của trang, không kèm .css
================================================================================
--%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <%-- head.jsp nạp base.css + components.css + layout-main.css --%>
    <c:set var="layoutCss" value="layout-main" scope="request"/>
    <%@ include file="parts/head.jsp" %>
</head>
<body>

<%@ include file="parts/nav.jsp" %>

<main class="shell">

    <%--
      Thong bao chung — hien MOT LAN o day thay vi lap <c:if> trong tung manh.
      Servlet nao dat "message" thi trang do tu co thong bao, khong phai sua gi.
      Them trang moi cung duoc huong san.
    --%>
    <c:if test="${not empty message}">
        <div class="panel panel-warn" style="margin-bottom:22px">
            <c:out value="${message}"/>
        </div>
    </c:if>

    <%--
      Chỗ nội dung được chèn vào.

      Dùng <jsp:include> (include lúc CHẠY) chứ không phải <%@ include %>
      (lúc biên dịch), vì đường dẫn nằm trong biến ${contentPage} — chỉ biết
      được lúc chạy. Include lúc biên dịch cần đường dẫn cố định, viết cứng.
    --%>
    <jsp:include page="${contentPage}" />
</main>

<%@ include file="parts/footer.jsp" %>

</body>
</html>
