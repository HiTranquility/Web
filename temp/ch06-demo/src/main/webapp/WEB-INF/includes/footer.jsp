<%@ page pageEncoding="UTF-8" %>
<%@ page import="java.util.GregorianCalendar, java.util.Calendar" %>
<%--
  footer.jsp — slide 27
  ---------------------------------------------------------------------------
  File này CỐ Ý viết bằng scriptlet kiểu cũ, đúng y như slide 27, để bạn thấy
  cách viết mà chương 6 đang so sánh. Cách hiện đại là ${currentYear} lấy từ
  attribute do servlet đặt (slide 7) — xem index.jsp.

  Vì sao là .jsp chứ không phải .html như header: nó phải TÍNH năm hiện tại.
  File .html không chạy code, chỉ được gửi đi nguyên xi.

  pageEncoding ở dòng đầu là BẮT BUỘC cho mọi file được include tĩnh —
  Tomcat đọc từng file theo encoding riêng của nó.
--%>
<%
    GregorianCalendar currentDate = new GregorianCalendar();
    int currentYear = currentDate.get(Calendar.YEAR);
%>
<p class="footer">&copy; Copyright <%= currentYear %> Mike Murach &amp; Associates</p>
</div>
</body>
</html>
