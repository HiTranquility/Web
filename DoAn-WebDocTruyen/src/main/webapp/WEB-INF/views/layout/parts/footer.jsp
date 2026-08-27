<%@ page pageEncoding="UTF-8" %>
<%-- pageEncoding la BAT BUOC cho moi file .jsp duoc include tinh:
     Tomcat doc TUNG FILE theo encoding rieng cua no. Thieu dong nay
     thi file duoc doc bang ISO-8859-1 va tieng Viet thanh ky tu la,
     du trang cha da khai UTF-8. --%>
<%--
================================================================================
  footer.jsp — phần cuối chung cho mọi trang
================================================================================
  Đóng lại đúng những thẻ header.jsp đã mở (<body>, <html>).

  Khối "Nội quy" ở đây không phải trang trí: mục tiêu đồ án có yêu cầu
  "điều dẫn sử dụng và luật dành cho người sử dụng", nên link tới chúng phải
  xuất hiện ở mọi trang, không giấu trong một góc nào đó.
================================================================================
--%>

<footer class="site-footer">
    <div class="shell">
        <div class="footer-grid">
            <div>
                <h5>Về trang này</h5>
                <p>Nền tảng đăng và đọc truyện do cộng đồng đóng góp.
                   Đồ án môn Lập trình Web — Java Servlet &amp; JSP.</p>
            </div>
            <div>
                <h5>Hướng dẫn</h5>
                <ul>
                    <li><a href="${pageContext.request.contextPath}/page?name=guide">Cách dùng trang</a></li>
                    <li><a href="${pageContext.request.contextPath}/page?name=guide#upload">Cách đăng truyện</a></li>
                </ul>
            </div>
            <div>
                <h5>Quy định</h5>
                <ul>
                    <li><a href="${pageContext.request.contextPath}/page?name=rules">Nội quy cộng đồng</a></li>
                    <li><a href="${pageContext.request.contextPath}/page?name=rules#content">Quy định nội dung</a></li>
                </ul>
            </div>
        </div>

        <div class="footer-base">
            <span>&copy; 2026 ĐọcTruyện — đồ án cuối kỳ</span>
            <span>Không spam · Không link độc hại · Bình luận văn minh</span>
        </div>
    </div>
</footer>

