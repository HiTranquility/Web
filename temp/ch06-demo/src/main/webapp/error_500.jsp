<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%--
================================================================================
  error_500.jsp — HTTP Status 500, Internal Server Error  (slide 34-35)
================================================================================
  KHI NÀO CHẠY VÀO ĐÂY
    Bất kỳ exception nào không được bắt trong servlet hoặc JSP.
    Khai bằng <exception-type>java.lang.Throwable</exception-type> nên nó bắt
    TẤT CẢ — vì mọi exception đều kế thừa từ Throwable.

  isErrorPage="true" — THUỘC TÍNH QUAN TRỌNG NHẤT CỦA FILE NÀY
    Nó là thứ làm cho biến ngầm định pageContext.exception có giá trị.
    Thiếu nó thì EL in ra rỗng, trang trông như hỏng, và không có gì để lần ra
    nguyên nhân. Đây là lỗi hay gặp nhất khi tự làm trang lỗi.

  ${pageContext.exception["class"]} — VÌ SAO PHẢI CÓ NGOẶC VUÔNG
    "class" là từ khoá dành riêng trong EL. Viết ${pageContext.exception.class}
    là lỗi cú pháp, trang không biên dịch được.

  ⚠️ KHI NỘP BÀI / CHẠY THẬT: XOÁ KHỐI "Chi tiết kỹ thuật" BÊN DƯỚI.
     Loại exception và thông điệp tiết lộ tên lớp, tên thư viện, đôi khi cả cấu
     trúc database cho kẻ tấn công. Lúc đang code thì cần nó, lúc chạy thật thì
     chi tiết thuộc về log file, không thuộc về màn hình người dùng.
================================================================================
--%>
<%@ include file="/WEB-INF/includes/header.html" %>

<h1>500 &mdash; Lỗi máy chủ</h1>
<p>Đã có lỗi xảy ra khi xử lý yêu cầu của bạn. Vui lòng thử lại.</p>

<%-- ---- chỉ dành cho lúc phát triển, xoá trước khi nộp ---- --%>
<h2>Chi tiết kỹ thuật</h2>
<table>
    <tr><th>Loại lỗi</th>
        <td><code>${pageContext.exception["class"]}</code></td></tr>
    <tr><th>Thông điệp</th>
        <td>${pageContext.exception.message}</td></tr>
    <tr><th>URL đã yêu cầu</th>
        <td><code>${requestScope['javax.servlet.error.request_uri']}</code></td></tr>
</table>

<div class="note">
    <b>Mẹo sửa lỗi 500 (slide 35):</b>
    <ol>
        <li>Đọc kỹ dòng "Loại lỗi" ở trên — nó nói lỗi gì</li>
        <li>Mở terminal đang chạy server, tìm stack trace đầy đủ</li>
        <li>Trong stack trace, tìm dòng ĐẦU TIÊN thuộc package của bạn
            (<code>murach.*</code>) — gần như luôn là chỗ cần sửa</li>
        <li>Kiểm tra file .class đã biên dịch lại chưa (sửa .java phải restart)</li>
    </ol>
</div>

<p><a href="${pageContext.request.contextPath}/">Về trang chủ</a></p>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
