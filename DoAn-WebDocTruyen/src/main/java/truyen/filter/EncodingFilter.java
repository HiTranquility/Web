package truyen.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Đặt UTF-8 cho MỌI request, TRƯỚC khi bất kỳ ai đọc tham số.
 *
 * LỖI MÀ FILTER NÀY SỬA
 *   Tiếng Việt gõ vào form web bị hỏng khi lưu xuống CSDL:
 *       gõ  "Trả lời thử"
 *       lưu "Tráº£ lá»i thá»­"
 *
 *   Mỗi servlet đều đã gọi request.setCharacterEncoding("UTF-8") ở dòng đầu.
 *   Nhưng đặc tả Servlet nói rõ: lệnh đó chỉ có tác dụng khi gọi TRƯỚC lần
 *   đọc tham số ĐẦU TIÊN. Đọc rồi thì thân request đã được giải mã xong,
 *   đặt lại không đổi được gì nữa — và cũng KHÔNG báo lỗi.
 *
 *   Mà AuthFilter chạy trước servlet, và dòng đầu của nó là
 *       String action = request.getParameter("action");
 *   Chính dòng đó kích hoạt giải mã thân request bằng bảng mã mặc định
 *   ISO-8859-1. Servlet đặt UTF-8 sau đó là đã muộn.
 *
 * VÌ SAO CHỈ HỎNG POST, KHÔNG HỎNG GET
 *   Tham số trên URL do connector giải mã, và Tomcat 8 trở lên mặc định
 *   dùng UTF-8 cho URI. Nên tìm kiếm "cánh cổng" vẫn ra đúng.
 *   Chỉ THÂN của POST mới dùng bảng mã mặc định ISO-8859-1.
 *
 * VÌ SAO KHAI TRONG web.xml CHỨ KHÔNG DÙNG @WebFilter
 *   Thứ tự chạy giữa các filter khai bằng @WebFilter là KHÔNG XÁC ĐỊNH —
 *   đặc tả không hứa gì, mỗi container một kiểu. Mà filter này bắt buộc
 *   phải chạy trước AuthFilter, không có ngoại lệ.
 *
 *   Thứ tự các <filter-mapping> trong web.xml thì được bảo đảm, và Tomcat
 *   chạy nhóm khai trong web.xml trước nhóm khai bằng annotation. Vì vậy
 *   lớp này KHÔNG có @WebFilter — xem khai báo ở WEB-INF/web.xml.
 */
public class EncodingFilter implements Filter {

    private static final String UTF8 = "UTF-8";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        /*
         * Chỉ đặt khi CHƯA có. Nếu một filter khác đã đặt rồi thì gọi lại
         * cũng vô hại, nhưng kiểm trước cho rõ ý: filter này không giành
         * quyền, nó chỉ vá chỗ trống.
         */
        if (req.getCharacterEncoding() == null) {
            req.setCharacterEncoding(UTF8);
        }
        res.setCharacterEncoding(UTF8);

        chain.doFilter(req, res);
    }

    @Override
    public void init(javax.servlet.FilterConfig config) { }

    @Override
    public void destroy() { }
}
