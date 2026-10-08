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
         * ẢNH BẰNG CHỨNG CHỈ ADMIN ĐƯỢC XEM  (ISSUE-025).
         *
         * Mọi ảnh khác trong thư mục này là ảnh BÌA TRUYỆN — vốn để khoe, ai
         * xem cũng được. Ảnh tố cáo thì ngược hẳn: nó có thể là ảnh chụp tin
         * nhắn riêng, thông tin cá nhân của người bị tố, hoặc chính nội dung
         * phản cảm đang bị báo cáo. Để công khai là ai đoán được tên file cũng
         * xem được hết.
         *
         * [NEVER] dựa vào "tên file ngẫu nhiên nên không ai đoán được". Đó là
         * bảo mật bằng cách giấu, và nó hỏng ngay khi một admin dán link vào
         * chat nhóm. Hàng rào phải là một câu if ở máy chủ.
         *
         * ĐẶT SAU PHẦN NORMALIZE Ở TRÊN, KHÔNG PHẢI TRƯỚC. So tiền tố trên
         * chuỗi thô thì "/uploads/x/../evidence/a.jpg" lách qua được; so trên
         * đường dẫn đã rút gọn thì không.
         */
        String rel = root.relativize(file).toString().replace('\\', '/');
        if (rel.startsWith(truyen.util.UploadUtil.EVIDENCE_PREFIX)) {
            truyen.model.User me = truyen.util.ServletHelper.currentUser(request);
            if (me == null || !me.isAdmin()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }

        /*
         * Kiểu nội dung suy từ ĐUÔI FILE do chính mình đặt lúc lưu, không phải
         * từ thứ người dùng gửi lên. UploadUtil chỉ sinh 4 đuôi này.
         */
        /*
         * ẢNH TRANG TRUYỆN TRANH: chỉ phát cho người ĐỌC ĐƯỢC chương đó.
         *
         * Không chặn ở đây thì chương VIP truyện tranh chẳng còn gì là VIP:
         * một người mở khoá rồi gửi link ảnh cho cả lớp. Tên file ngẫu nhiên
         * chống ĐOÁN, không chống CHIA SẺ.
         *
         * Thường thì phiên đã được ChapterServlet cấp quyền lúc mở chương —
         * tra một tập trong bộ nhớ, không tốn câu SQL nào. Chỉ khi chưa có
         * (mở thẳng link ảnh, ảnh thu nhỏ ở tủ sách) mới hỏi CSDL một lần,
         * đúng luật ChapterAccess như trang đọc.
         */
        boolean gated = rel.startsWith(truyen.util.UploadUtil.PAGES_PREFIX);
        if (gated && !canReadPage(request, rel)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

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
        //
        // Ảnh trang truyện tranh thì "private": vẫn cache lâu trong trình duyệt
        // của người đọc (lật lại chương không tải lại), nhưng CẤM proxy / CDN
        // dùng chung giữ bản sao — không thì người chưa mở khoá đi qua cùng
        // proxy sẽ nhận ảnh từ bộ đệm, bỏ qua bước kiểm quyền ở trên.
        response.setHeader("Cache-Control", gated
                ? "private, max-age=86400"
                : "public, max-age=31536000, immutable");
        response.setContentLengthLong(Files.size(file));

        try (OutputStream out = response.getOutputStream()) {
            Files.copy(file, out);
        }
    }

    private final truyen.dao.ChapterDAO chapterDAO = new truyen.dao.ChapterDAO();
    private final truyen.dao.StoryDAO storyDAO = new truyen.dao.StoryDAO();
    private final truyen.dao.UnlockDAO unlockDAO = new truyen.dao.UnlockDAO();

    /** Người đang xem có đọc được chương chứa ảnh này không. */
    private boolean canReadPage(HttpServletRequest request, String rel) {
        int chapterId = truyen.util.UploadUtil.chapterIdOfPage(rel);
        if (chapterId <= 0) return false;

        javax.servlet.http.HttpSession session = request.getSession(false);
        if (truyen.util.ChapterAccess.isGranted(session, chapterId)) return true;

        truyen.model.User me = truyen.util.ServletHelper.currentUser(request);
        if (me == null) return false;          // nghiệp vụ: phải đăng nhập mới đọc
        try {
            truyen.model.Chapter ch = chapterDAO.findById(chapterId);
            truyen.model.Story st = ch == null ? null : storyDAO.findById(ch.getStoryId());
            boolean ok = truyen.util.ChapterAccess.canRead(me, st, ch, unlockDAO::hasUnlocked);
            if (ok) truyen.util.ChapterAccess.grant(session, chapterId);
            return ok;
        } catch (java.sql.SQLException e) {
            log("UploadedFileServlet: không kiểm được quyền đọc chương " + chapterId, e);
            return false;                      // không chắc -> không phát
        }
    }
}
