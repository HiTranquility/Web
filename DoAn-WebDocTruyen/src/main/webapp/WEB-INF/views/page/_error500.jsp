<%@ page pageEncoding="UTF-8" %>
<%-- Mảnh nội dung trang 500. Nhận errType/errMsg do error500.jsp đặt hộ,
     vì pageContext.exception không xuyên qua được <jsp:include>. --%>
<div class="empty" style="margin-top:60px">
    <div class="empty-icon">⚠️</div>
    <h3>Hệ thống gặp sự cố</h3>
    <p>Đã có lỗi khi xử lý yêu cầu của bạn. Vui lòng thử lại,
       hoặc quay về trang chủ.</p>
    <a class="btn btn-primary" href="${pageContext.request.contextPath}/">Về trang chủ</a>
</div>

<%--
  ĐÃ GỠ KHỐI "chi tiết kỹ thuật".

  Trước đây chỗ này in ra tên lớp ngoại lệ và thông điệp lỗi. Tiện lúc làm,
  nhưng đó là RÒ RỈ THÔNG TIN: tên lớp cho biết dự án dùng thư viện gì phiên
  bản nào, thông điệp của SQLException còn lộ cả tên bảng và tên cột.

  Người dùng thật không làm gì được với hai dòng đó; người muốn tấn công thì
  có. Lỗi vẫn được ghi đầy đủ vào log máy chủ qua log() trong servlet — đúng
  chỗ của nó.
--%>
