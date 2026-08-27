<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
================================================================================
  thanks.jsp — SLIDE 30: trang thứ hai dùng CHUNG header và footer
================================================================================
  Slide 30 là ảnh chụp màn hình, không in code. Nội dung dưới đây dựng lại
  đúng theo ảnh đó: tiêu đề, ba dòng thông tin, đoạn hướng dẫn, và nút Return.

  Ý nghĩa của slide này: cùng bộ include, hai trang khác nhau, giao diện đồng
  nhất. Sửa header.html một lần là CẢ HAI trang đổi theo — đó là toàn bộ lý do
  của include file (slide 33).

  ⚠️ BÀI HỌC TỪ SLIDE 34 — đọc kỹ chỗ này
     Slide 34 chụp trang lỗi 500 của Tomcat, và lỗi đó là:

         javax.el.PropertyNotFoundException:
         Property 'emailAddress' not found on type murach.business.User

     Nguyên nhân: JSP viết ${user.emailAddress} nhưng lớp User chỉ có
     getEmail(), nên tên property là "email" chứ không phải "emailAddress".

     Đây là lỗi EL kinh điển: TÊN PROPERTY LẤY TỪ TÊN GET METHOD, không phải
     từ tên biến, cũng không phải từ tên cột trong database. getEmail() ->
     ${user.email}. Viết sai một chữ là 500 ngay, không phải in ra rỗng.

     (Khác với trường hợp bean là null — lúc đó EL in ra rỗng, không lỗi.
      Bean CÓ nhưng property KHÔNG CÓ thì mới ném exception.)
================================================================================
--%>
<c:import url="/WEB-INF/includes/header.html" />

<h1>Thanks for joining our email list</h1>

<p>Here is the information that you entered:</p>

<%-- Slide 21: EL đọc property của bean.
     Tên property phải khớp get method — xem cảnh báo ở đầu file. --%>
<label>Email:</label>
<span>${user.email}</span><br>
<label>First Name:</label>
<span>${user.firstName}</span><br>
<label>Last Name:</label>
<span>${user.lastName}</span><br>

<p>To enter another email address, click on the Back
   button in your browser or the Return button shown below.</p>

<%--
  Nút Return, đúng như ảnh slide 30.

  Dùng <form> chứ không phải thẻ <a> vì cần gửi kèm tham số action=join cho
  servlet. method="get" là đúng: nút này chỉ ĐỌC (hiện lại form), không GHI
  gì cả — quy tắc chọn GET/POST đã học ở chương 5.
--%>
<form action="emailList" method="get">
    <input type="hidden" name="action" value="join">
    <input type="submit" value="Return">
</form>

<c:import url="/WEB-INF/includes/footer.jsp" />
