package truyen.controller.common;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Phục vụ ảnh người dùng đã tải lên.
 *
 * VÌ SAO PHẢI CÓ SERVLET NÀY
 *   Ảnh cố ý lưu NGOÀI thư mục webapp (xem UploadUtil: để trong đó thì mỗi
 *   lần triển khai lại là mất sạch). Nhưng Tomcat chỉ tự phục vụ file nằm
 *   trong webapp — ngoài đó thì phải có người đọc ra, và đó là servlet này.
 *
 *   Đây là cái giá của quyết định lưu ngoài, và trả là đúng: mất một file
 *   servlet, đổi lại ảnh người dùng không bốc hơi mỗi lần deploy.
 */
@WebServlet("/uploads/*")
public class UploadedFileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        File folder;
        try {
            folder = truyen.util.UploadUtil.resolveUploadFolder(getServletContext());
        } catch (truyen.util.UploadUtil.UploadException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String rest = request.getPathInfo();        // "/abc123.png"

        if (rest == null || rest.length() < 2) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        /*
         * CHỐNG ĐI NGƯỢC THƯ MỤC (path traversal).
         *
         * Người ta gõ thẳng vào thanh địa chỉ:
         *     /uploads/../../../../Windows/win.ini
         *     /uploads/..%2f..%2fsrc%2fmain%2fresources%2fdb.properties
         * Nối chuỗi thật thà thì servlet này thành công cụ đọc trộm mọi file
         * trên máy chủ — kể cả db.properties có mật khẩu CSDL.
         *
         * KHÔNG lọc bằng cách tìm chữ "..": có hàng chục kiểu mã hoá để né,
         * và đó là cuộc đua không thắng được. Cách chắc ăn là quy về đường
         * dẫn tuyệt đối đã rút gọn (normalize) rồi HỎI: nó có còn nằm trong
         * thư mục ảnh không? Câu hỏi đó không né được.
         */
        Path root = folder.toPath().toAbsolutePath().normalize();
        Path file = root.resolve(rest.substring(1)).toAbsolutePath().normalize();

        if (!file.startsWith(root) || !Files.isRegularFile(file)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        /*
         * Kiểu nội dung suy từ ĐUÔI FILE do chính mình đặt lúc lưu, không phải
         * từ thứ người dùng gửi lên. UploadUtil chỉ sinh 4 đuôi này.
         */
        String name = file.getFileName().toString().toLowerCase();
        String type = name.endsWith(".png")  ? "image/png"
                    : name.endsWith(".gif")  ? "image/gif"
                    : name.endsWith(".webp") ? "image/webp"
                    : name.endsWith(".jpg")  ? "image/jpeg"
                    : null;
        if (type == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        /*
         * XOÁ BẢNG MÃ TRƯỚC KHI ĐẶT KIỂU NỘI DUNG.
         *
         * EncodingFilter gọi res.setCharacterEncoding("UTF-8") cho MỌI
         * response — đúng cho trang HTML, nhưng ảnh là dữ liệu nhị phân,
         * không có bảng mã nào cả. Để nguyên thì header thành
         *     Content-Type: image/png;charset=UTF-8
         * Trình duyệt bỏ qua phần thừa đó nên ảnh vẫn hiện, nhưng nó sai về
         * ý nghĩa và làm vài công cụ hiểu nhầm đây là văn bản.
         *
         * setHeader("Content-Type", ...) KHÔNG cứu được: Tomcat chuyển hướng
         * lệnh đó về đúng setContentType, rồi lại nối bảng mã vào như cũ.
         * Phải xoá bảng mã đi trước.
         */
        response.setCharacterEncoding(null);
        response.setContentType(type);

        /*
         * nosniff: cấm trình duyệt tự đoán lại kiểu nội dung.
         * Thiếu nó thì một file lọt lưới có thể được trình duyệt hiểu thành
         * HTML và chạy script trong đó — ngay trên tên miền của mình.
         */
        response.setHeader("X-Content-Type-Options", "nosniff");

        /*
         * Cache một năm. Tên file là chuỗi ngẫu nhiên và KHÔNG BAO GIỜ bị
         * dùng lại cho ảnh khác, nên nội dung sau một tên là bất biến — đổi
         * ảnh bìa là sinh tên mới. Đã bất biến thì cache càng lâu càng tốt.
         */
        response.setHeader("Cache-Control", "public, max-age=31536000, immutable");
        response.setContentLengthLong(Files.size(file));

        try (OutputStream out = response.getOutputStream()) {
            Files.copy(file, out);
        }
    }
}
