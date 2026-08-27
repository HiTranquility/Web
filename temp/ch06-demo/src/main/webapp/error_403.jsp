<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%--
================================================================================
  error_403.jsp — HTTP Status 403, Forbidden
================================================================================
  ⚠️ TRANG NÀY KHÔNG CÓ TRONG SLIDE.
     Slide 35 chỉ liệt kê 404 và 500. Thêm 403 vào vì đề bài yêu cầu, và vì
     nó là mã lỗi thứ ba hay gặp nhất.

  PHÂN BIỆT 401, 403, 404 — hay bị nhầm
    401 Unauthorized : CHƯA đăng nhập. "Anh là ai?"
    403 Forbidden    : ĐÃ đăng nhập rồi nhưng KHÔNG ĐỦ QUYỀN.
                       "Biết anh là ai, nhưng anh không được vào."
    404 Not Found    : không có cái gì ở đây cả.

  KHI NÀO DÙNG 403 TRONG THỰC TẾ
    - user thường mở URL trang quản trị
    - user A sửa URL để cố sửa bài viết của user B
    (Đúng phần "kiểm tra quyền sở hữu" ở CASE 05 của đồ án.)

  MẸO BẢO MẬT: nhiều web cố ý trả 404 thay vì 403 cho tài nguyên nhạy cảm —
  vì 403 vô tình xác nhận "chỗ này CÓ tồn tại", còn 404 thì không tiết lộ gì.
================================================================================
--%>
<%@ include file="/WEB-INF/includes/header.html" %>

<h1>403 &mdash; Không có quyền truy cập</h1>
<p>Bạn đã đăng nhập, nhưng tài khoản của bạn không được phép mở trang này.</p>

<table>
    <tr><th>URL đã yêu cầu</th>
        <td><code>${requestScope['javax.servlet.error.request_uri']}</code></td></tr>
    <tr><th>Mã trạng thái</th>
        <td><code>${requestScope['javax.servlet.error.status_code']}</code></td></tr>
</table>

<div class="note">
    <b>403 khác 404 ở chỗ nào:</b> 404 nghĩa là <i>không có gì ở đây</i>;
    403 nghĩa là <i>có, nhưng không phải phần của bạn</i>.
    Gặp 403 thì đăng nhập bằng tài khoản khác có quyền cao hơn,
    hoặc liên hệ quản trị viên.
</div>

<p><a href="${pageContext.request.contextPath}/">Về trang chủ</a></p>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
