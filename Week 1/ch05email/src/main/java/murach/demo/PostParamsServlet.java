package murach.demo;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/* ============================================================================
 * CASE 05 — GET so với POST                               (slide 14-15)
 * ============================================================================
 *
 * VẤN ĐỀ
 *   Hai method gửi CÙNG tham số tới CÙNG servlet, đọc bằng CÙNG một lệnh
 *   getParameter(). Khác nhau ở chỗ tham số ĐI ĐƯỜNG NÀO — và chính điều đó
 *   quyết định nó có bị lộ, có bookmark được, có lặp lại được, có giới hạn
 *   dung lượng hay không.
 *
 *   GET   -> tham số nằm trong URL:  /postDemo?secret=hunter2
 *   POST  -> tham số nằm trong body: URL sạch, dữ liệu đi trong thân request
 *
 * CÁCH DÙNG (slide 15)
 *   <form action="emailList" method="post">
 *
 * KHI NÀO DÙNG GET (slide 15)
 *   - request chỉ ĐỌC dữ liệu từ server
 *   - chạy lại nhiều lần cũng không gây vấn đề gì
 *   => bookmark được, F5 thoải mái, trình duyệt được phép tải trước
 *
 * KHI NÀO DÙNG POST (slide 15 — sách liệt kê đúng 5 lý do)
 *   - request GHI dữ liệu lên server
 *   - chạy lại nhiều lần thì sinh chuyện (đặt hàng hai lần)
 *   - không muốn tham số lộ trên URL vì lý do bảo mật
 *   - không muốn người dùng bookmark trang kèm theo tham số
 *   - cần truyền hơn 4 KB dữ liệu
 *
 * "BẢO MẬT" Ở ĐÂY NGHĨA LÀ GÌ — ĐỪNG HIỂU NHẦM
 *   POST không mã hoá gì cả. Bắt gói tin lên là đọc được y như GET. Cái POST
 *   tránh được là dữ liệu bị GHI LẠI ở những nơi bạn quên mất: lịch sử trình
 *   duyệt, access log của server, header Referer khi người dùng bấm sang trang
 *   khác, và cái link mà họ vô tư copy gửi cho bạn bè. Muốn thật sự bảo mật thì
 *   phải là HTTPS — và HTTPS bảo vệ cả hai method như nhau.
 * ========================================================================= */
@WebServlet("/postDemo")
public class PostParamsServlet extends HttpServlet {

    // Servlet này nhận cả hai method để MỘT trang demo được cả hai.
    // Cả doGet và doPost đều đẩy về handle() — không nhân đôi logic.
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        handle(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        handle(request, response);
    }

    private void handle(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        /*
         * setCharacterEncoding PHẢI gọi TRƯỚC lệnh getParameter() đầu tiên.
         *
         * Lý do: lần gọi getParameter() đầu tiên khiến Tomcat parse toàn bộ body
         * và chốt luôn charset. Gọi sau đó thì không còn tác dụng, im lặng, và
         * tiếng Việt ra "?????" mà không có lỗi nào.
         *
         * Chỉ ảnh hưởng tham số trong BODY (tức POST). Tham số trên URL (GET)
         * do connector giải mã, cấu hình ở chỗ khác — Tomcat 8 trở lên mặc định
         * đã là UTF-8 nên phần GET thường không cần đụng tới.
         */
        request.setCharacterEncoding("UTF-8");

        request.setAttribute("method", request.getMethod());

        // Với POST thì cái này null — đó chính là điều trang demo muốn chỉ ra.
        request.setAttribute("queryString", request.getQueryString());

        // Cùng một lệnh, chạy đúng cho cả hai method. Servlet không cần biết
        // tham số đến từ URL hay từ body.
        request.setAttribute("secret", request.getParameter("secret"));

        /*
         * getContentType() cho thấy sự khác biệt rõ nhất:
         *   GET  -> null (không có body thì không có kiểu nội dung)
         *   POST -> application/x-www-form-urlencoded
         * (Form có upload file thì là multipart/form-data, đọc bằng
         *  request.getPart() chứ getParameter() không thấy gì.)
         */
        request.setAttribute("contentType", request.getContentType());

        getServletContext()
                .getRequestDispatcher("/demo/case05.jsp")
                .forward(request, response);
    }
}
