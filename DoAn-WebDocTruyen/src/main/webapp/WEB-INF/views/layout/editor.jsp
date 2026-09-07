<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
================================================================================
  layout/editor.jsp — KHUNG SOẠN THẢO                    LAYOUT 5 / 5
================================================================================
  DÙNG Ở: trang 24 (thêm / sửa chương)

  VÌ SAO CẦN MỘT KHUNG RIÊNG CHO VIỆC SOẠN CHƯƠNG

    Trang soạn chương khác mọi trang khác ở một điểm: người dùng ở lại rất
    lâu và chỉ nhìn vào MỘT ô nhập liệu. Khung `main` có thanh menu dính trên
    cùng, chân trang bốn cột, chiều rộng 1200px — tất cả đều là thứ tranh chỗ
    với ô soạn thảo.

    Khung này bỏ hết:
      · không chân trang        — không có gì để bấm khi đang viết
      · thanh trên gọn, không dính — cuộn xuống là biến mất, trả lại màn hình
      · chiều rộng 860px        — đủ rộng cho một cột chữ, không rộng hơn

    Đây cũng là lý do trong bảng quyết định chọn layout, câu hỏi "người dùng
    có đang TẠO nội dung không?" được hỏi RIÊNG, không gộp vào "trang thành
    viên" — tạo nội dung là một trạng thái khác hẳn của người dùng.

  Nhận: contentPage · pageTitle · editorBack (đường dẫn nút thoát)
================================================================================
--%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <c:set var="layoutCss" value="layout-editor" scope="request"/>
    <%@ include file="parts/head.jsp" %>
</head>
<body class="editor-body">

<%--
  Thanh trên tối giản — KHÔNG dùng parts/nav.jsp.

  Đây là lần duy nhất trong dự án một layout không dùng lại nav chung. Có lý
  do: nav chung có bảy mục điều hướng, mà rời trang giữa chừng lúc đang soạn
  là mất bài. Ở đây chỉ để đúng một lối ra, và nó ghi rõ sẽ đi đâu.
--%>
<header class="editor-bar">
    <a class="editor-back"
       href="${pageContext.request.contextPath}${empty editorBack ? '/story?action=mine' : editorBack}">
        ← Thoát
    </a>
    <span class="editor-title"><c:out value="${pageTitle}"/></span>
    <span class="editor-hint">Nhớ bấm Lưu trước khi thoát</span>
</header>

<main class="editor-shell">
    <c:if test="${not empty message}">
        <div class="panel panel-warn"><c:out value="${message}"/></div>
    </c:if>

    <jsp:include page="${contentPage}" />
</main>

</body>
</html>
