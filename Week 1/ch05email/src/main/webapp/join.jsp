<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%--
================================================================================
  join.jsp — trang nhập liệu của CASE 11                       (slide 28-29)
================================================================================

  AI GỌI TRANG NÀY
    EmailListServlet forward tới đây trong hai tình huống:
      1. action=join            -> hiện form trống
      2. action=add nhưng thiếu -> hiện lại form KÈM thông báo lỗi và dữ liệu cũ
    Người dùng không bao giờ gõ thẳng URL này.

  HAI THỨ LÀM NÊN CẢ CASE 11

  1. ${message}
     Servlet đặt "" khi mọi thứ ổn, đặt câu báo lỗi khi thiếu dữ liệu. EL in
     chuỗi rỗng ra thành không có gì, nên MỘT trang phục vụ được cả hai tình
     huống mà không cần một câu if nào trong JSP. Đây là lý do servlet gán
     message = "" ở nhánh thành công thay vì bỏ trống không gán.

  2. value="${user.email}"
     Servlet forward luôn object User vừa dựng về đây, kể cả khi dữ liệu sai.
     Nhờ vậy các ô giữ lại chữ người dùng đã gõ.

     Lần vào đầu tiên thì attribute "user" chưa tồn tại. EL gặp null KHÔNG ném
     lỗi — nó in ra chuỗi rỗng, tức là value="" , đúng bằng một ô trống. Java
     mà viết user.getEmail() với user null là NullPointerException ngay; EL
     được thiết kế "dễ tính" chính vì tình huống này.

  KHÁC SLIDE
    Slide gọi file này là index.jsp. Ở đây index.jsp đã là trang mục lục 16
    case nên đổi tên thành join.jsp. Servlet forward tới "/join.jsp".

  VÌ SAO KHÔNG CÒN required TRONG CÁC THẺ input
    Chương 2 có required, chương 5 bỏ đi — cố ý, để bạn submit được form rỗng
    và thấy phần kiểm tra phía server hoạt động. Trong ứng dụng thật thì nên
    có CẢ HAI: required cho trải nghiệm mượt, kiểm tra ở servlet để đảm bảo đúng.
================================================================================
--%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Murach's Java Servlets and JSP</title>
    <link rel="stylesheet" href="styles/main.css" type="text/css"/>
</head>
<body>
<div class="wrap">
    <div class="topbar">
        <a href="${pageContext.request.contextPath}/">&larr; All 16 cases</a>
        <span class="slides">slides 27-31</span>
    </div>

    <h1>Join our email list</h1>
    <p>To join our email list, enter your name and
       email address below.</p>

    <p><i style="color:#9f1239">${message}</i></p>

    <form action="emailList" method="post" class="stack">
        <input type="hidden" name="action" value="add">

        <p><label>Email:</label>
        <input type="email" name="email" value="${user.email}"></p>

        <p><label>First Name:</label>
        <input type="text" name="firstName" value="${user.firstName}"></p>

        <p><label>Last Name:</label>
        <input type="text" name="lastName" value="${user.lastName}"></p>

        <p><label>&nbsp;</label>
        <input type="submit" value="Join Now"></p>
    </form>

    <div class="problem">
        <strong>Case 11 &mdash; the problem</strong>
        In chapter 2 these boxes had <code>required</code> on them, so the browser
        refused to submit an empty form. That check runs on the user's machine and
        anyone can switch it off. Here the attributes are gone: submit the form empty
        and the <em>servlet</em> catches it.
    </div>

    <h3 style="color:#5b6b7c">The two things that make this work</h3>
    <table>
        <tr><th style="width:9em"><code>&#36;{message}</code></th>
            <td>empty on the first visit, filled in by the servlet when validation
                fails &mdash; so one JSP serves both cases with no <code>if</code>
                anywhere in the page.</td></tr>
        <tr><th><code>value="&#36;{user.email}"</code></th>
            <td>the servlet forwards the <code>User</code> it just built back to this
                page, so the boxes come back filled with what you typed instead of
                blank. On the first visit <code>user</code> is null and EL prints
                nothing &mdash; which is exactly what an empty box needs.</td></tr>
    </table>

    <h3 style="color:#5b6b7c">The validation, from slide 31</h3>
    <pre>String message;
if (firstName == null || lastName == null ||
    email == null ||
    firstName.isEmpty() || lastName.isEmpty() ||
    email.isEmpty()) {

    message = "Please fill out all three text boxes.";
    url = "/join.jsp";
} else {
    message = "";
    url = "/thanks.jsp";
    UserIO.add(user, path);
}
request.setAttribute("user", user);
request.setAttribute("message", message);</pre>

    <div class="note">
        <strong>Why both null and isEmpty.</strong> They are different failures.
        <code>null</code> means the parameter was never sent &mdash; someone posted to
        <code>/emailList</code> without the form. <code>isEmpty()</code> means the box
        was sent but left blank. Check <code>null</code> first, or
        <code>isEmpty()</code> throws a <code>NullPointerException</code> on the same
        line that was supposed to be validating.
    </div>

    <div class="footnav"><a href="${pageContext.request.contextPath}/">&larr; Back to the case index</a></div>
</div>
</body>
</html>
