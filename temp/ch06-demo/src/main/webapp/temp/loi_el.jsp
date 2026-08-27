<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="murach.business.User" %>
<%--
================================================================================
  temp/loi_el.jsp — DỰNG LẠI ĐÚNG LỖI Ở SLIDE 34
================================================================================
  Slide 34 chụp màn hình trang lỗi 500 của Tomcat với nội dung:

      javax.el.PropertyNotFoundException:
      Property 'emailAddress' not found on type murach.business.User

  Nguyên nhân: JSP viết ${user.emailAddress} nhưng lớp User chỉ có getEmail(),
  nên tên property đúng là "email".

  Trang này cố ý viết sai y hệt để bạn thấy lỗi thật.

  BÀI HỌC: tên property trong EL lấy từ TÊN GET METHOD, không phải tên biến,
  cũng không phải tên cột database.
      getEmail()      -> ${user.email}
      getFirstName()  -> ${user.firstName}

  VÀ PHÂN BIỆT HAI TRƯỜNG HỢP — nhiều người nhớ nhầm là "EL không bao giờ lỗi":
      bean là null                -> EL in ra RỖNG, không lỗi
      bean có, sai tên property   -> NÉM EXCEPTION -> 500
================================================================================
--%>
<%
    // Bean có thật, không null.
    User user = new User("John", "Smith", "jsmith@gmail.com");
    request.setAttribute("user", user);
%>
<%@ include file="/WEB-INF/includes/header.html" %>

<h1>Thử lỗi EL sai tên property</h1>

<p><b>Đúng</b> &mdash; <code>${'$'}{user.email}</code> khớp với
   <code>getEmail()</code>:</p>
<p>Email: ${user.email}</p>

<p><b>Bean null thì sao</b> &mdash; <code>${'$'}{khongCoBean.email}</code>
   in ra rỗng, KHÔNG lỗi:</p>
<p>Kết quả: "${khongCoBean.email}" &larr; rỗng, trang vẫn chạy</p>

<p><b>Sai tên property</b> &mdash; dòng dưới đây gọi
   <code>${'$'}{user.emailAddress}</code> trong khi User không có
   <code>getEmailAddress()</code>. Trang sẽ dừng ngay tại đây và nhảy sang
   trang lỗi 500:</p>

<p>Email address: ${user.emailAddress}</p>

<p>Bạn sẽ KHÔNG bao giờ đọc được dòng này.</p>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
