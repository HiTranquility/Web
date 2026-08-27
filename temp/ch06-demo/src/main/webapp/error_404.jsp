<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%--
================================================================================
  error_404.jsp — HTTP Status 404, File Not Found        (slide 34-35)
================================================================================
  KHI NÀO CHẠY VÀO ĐÂY
    - URL không tồn tại
    - hoặc code gọi response.sendError(404)

  KHAI Ở ĐÂU
    WEB-INF/web.xml:
        <error-page>
            <error-code>404</error-code>
            <location>/error_404.jsp</location>
        </error-page>

  SLIDE 35 — MẸO SỬA LỖI 404
    1. Kiểm tra URL có đúng không, có trỏ đúng vị trí trang cần mở không
    2. Kiểm tra các file HTML, JSP, .class đã nằm đúng thư mục chưa
    3. Đọc kỹ trang lỗi để lấy hết thông tin

  LƯU Ý: trang này KHÔNG cần isErrorPage="true", vì nó chỉ đọc các attribute
  thường do Tomcat đặt, không dùng tới pageContext.exception.
  Trang error_500.jsp thì cần — xem file đó.
================================================================================
--%>
<%@ include file="/WEB-INF/includes/header.html" %>

<h1>404 &mdash; Không tìm thấy trang</h1>
<p>Máy chủ không tìm thấy trang bạn yêu cầu.</p>

<%--
  Tomcat đặt sẵn một loạt attribute tên bắt đầu bằng "javax.servlet.error.".
  Phải đọc bằng NGOẶC VUÔNG vì tên có dấu chấm — viết
  ${requestScope.javax.servlet.error.request_uri} thì EL hiểu nhầm là đi vào
  property "javax", rồi "servlet"... và không tìm thấy gì.
--%>
<table>
    <tr><th>URL đã yêu cầu</th>
        <td><code>${requestScope['javax.servlet.error.request_uri']}</code></td></tr>
    <tr><th>Mã trạng thái</th>
        <td><code>${requestScope['javax.servlet.error.status_code']}</code></td></tr>
</table>

<div class="note">
    <b>Kiểm tra theo thứ tự này (slide 35):</b>
    <ol>
        <li>URL gõ có đúng chính tả không, có thiếu tên ứng dụng không</li>
        <li>File JSP có nằm đúng thư mục không</li>
        <li>Nếu là servlet: <code>url-pattern</code> trong web.xml có khớp không</li>
    </ol>
</div>

<p><a href="${pageContext.request.contextPath}/">Về trang chủ</a></p>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
