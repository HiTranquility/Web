<%@ page pageEncoding="UTF-8" %>
<%--
  parts/head.jsp — nội dung thẻ <head>, dùng chung cho MỌI layout.

  BA TẦNG CSS, nạp theo thứ tự — thứ tự này quan trọng vì file sau ghi đè file trước:

    1. base.css        biến màu, reset, typography, body     — MỌI trang
    2. components.css  nút, thẻ, tag, form, bảng, panel      — MỌI trang
    3. ${layoutCss}    riêng của layout (main / auth / reader)
    4. ${pageCss}      riêng của MỘT trang, nếu cần          — tuỳ chọn

  Nhờ chia vậy mà trang đăng nhập không phải tải CSS của thanh menu và lưới
  truyện — những thứ nó không dùng tới.

  pageEncoding BẮT BUỘC ở đây: file này được include TĨNH, mà Tomcat đọc từng
  file theo encoding riêng của nó. Thiếu dòng này là tiếng Việt thành ký tự lạ.
--%>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>${empty pageTitle ? 'ĐọcTruyện' : pageTitle}</title>

<%-- contextPath: đổi tên lúc deploy vẫn chạy, không cần sửa link --%>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/components.css">
<c:if test="${not empty layoutCss}">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/${layoutCss}.css">
</c:if>
<c:if test="${not empty pageCss}">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/${pageCss}.css">
</c:if>
