<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%--
================================================================================
  temp/index.jsp — TRANG DEMO TẠM
================================================================================
  Folder /temp là chỗ để thử nghiệm và demo cho giảng viên.
  KHÔNG phải phần chính của bài — xoá cả folder này đi thì app vẫn chạy đủ.

  Mở: http://localhost:8080/temp/
================================================================================
--%>
<%@ include file="/WEB-INF/includes/header.html" %>

<h1>Trang demo &mdash; thử các lỗi</h1>
<p>Bấm từng nút để xem trang lỗi tương ứng. Mở tab <b>Network</b> trong
   DevTools (F12) để thấy <b>mã HTTP thật</b> chứ không phải 200 giả.</p>

<h2>1. Lỗi 404 &mdash; Không tìm thấy</h2>
<p>
    <a href="/404">Gọi thẳng /404</a> &nbsp;|&nbsp;
    <a href="/khong-co-trang-nay">Gõ URL bịa</a> &nbsp;|&nbsp;
    <a href="/truyen/sau/hon/nua">URL nhiều cấp</a>
</p>
<p style="color:#5b6b7c;font-size:.9em">Link thứ ba dùng để kiểm tra CSS: trước
   đây trang lỗi ở URL nhiều cấp bị mất định dạng vì đường dẫn CSS tương đối.
   Giờ phải hiện đúng kiểu.</p>

<h2>2. Lỗi 403 &mdash; Không có quyền</h2>
<p><a href="/403">Gọi /403</a></p>

<h2>3. Lỗi 500 &mdash; Lỗi máy chủ</h2>
<p>Hai nguyên nhân khác nhau, cùng ra trang 500:</p>
<p>
    <a href="/500">Exception cố ý ném ra</a> &nbsp;|&nbsp;
    <a href="loi_el.jsp"><b>Lỗi EL sai tên property (đúng như slide 34)</b></a>
</p>

<h2>4. File include có bị lộ không</h2>
<p>Hai link này <b>phải ra 404</b>. Nếu ra 200 là file include đang nằm sai chỗ:</p>
<p>
    <a href="/WEB-INF/includes/header.html">/WEB-INF/includes/header.html</a> &nbsp;|&nbsp;
    <a href="/includes/header.html">/includes/header.html (chỗ cũ)</a>
</p>

<h2>5. Trang chính</h2>
<p><a href="/">Join our email list</a> &mdash; điền form rồi bấm Join Now</p>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
