package truyen.controller.user;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import truyen.dao.ReportDAO;
import truyen.model.Report;
import truyen.model.ReportEvidence;
import truyen.model.User;
import truyen.util.RateLimiter;
import truyen.util.UploadUtil;
import static truyen.util.ServletHelper.parseIntOr;
import static truyen.util.ServletHelper.currentUser;
import static truyen.util.ServletHelper.trimOrEmpty;

/**
 * Người dùng gửi báo cáo vi phạm, kèm phân loại và ảnh bằng chứng (ISSUE-025).
 *
 * TẦNG: controller/ — không SQL, không HTML.
 */
@WebServlet("/report")
/*
 * GIỚI HẠN CỦA CONTAINER PHẢI CAO HƠN GIỚI HẠN CỦA ỨNG DỤNG — CÓ LÝ DO.
 *
 * Thoạt nhìn thì đặt maxFileSize = 2 MB cho khớp UploadUtil.MAX_BYTES là gọn
 * nhất. Đã thử, và nó HỎNG theo kiểu rất khó đoán:
 *
 *   Gửi ảnh PNG thật 2.6 MB  ->  người dùng nhận trang 403 trống trơn.
 *
 * Vì sao: CsrfFilter chạy TRƯỚC servlet và gọi request.getParameter("_csrf").
 * Với request multipart, chỉ riêng lệnh đó đã buộc Tomcat phân tích cả thân
 * request. File vượt maxFileSize thì Tomcat ném lỗi ngay tại đó, getParameter()
 * trả null, filter tưởng là thiếu token CSRF và trả 403. Servlet KHÔNG BAO GIỜ
 * được chạy, nên câu báo lỗi tử tế trong UploadUtil không bao giờ tới được
 * người dùng.
 *
 * Cách sửa: để container làm hàng rào CHỐNG ĐỔ (chặn file khổng lồ trước khi
 * nó ăn hết RAM và ổ đĩa), còn giới hạn NGƯỜI DÙNG THẤY thì để UploadUtil lo —
 * nơi còn nói được câu tử tế. 10 MB đủ rộng để mọi lỗi "chọn nhầm ảnh hơi to"
 * đi lọt tới servlet, và vẫn đủ chặt để không ai đẩy được file 500 MB lên.
 *
 * Còn sót: file vượt cả 10 MB vẫn ra 403 trống. Chấp nhận — đó là ca phá hoại
 * chứ không phải người dùng lỡ tay, và quan trọng là Tomcat ngừng đọc chứ
 * không nuốt hết vào RAM.
 */
@MultipartConfig(
        // Dưới ngưỡng này thì giữ trong RAM, trên thì Tomcat ghi ra file tạm.
        fileSizeThreshold = 512 * 1024,           // 512 KB
        // Hàng rào CHỐNG ĐỔ, không phải giới hạn người dùng thấy (xem trên).
        maxFileSize       = 10L * 1024 * 1024,    // 10 MB
        // Cả request: 3 ảnh cỡ đó + phần chữ, cộng dư.
        maxRequestSize    = 35L * 1024 * 1024
)
public class ReportServlet extends HttpServlet {

    private ReportDAO reportDAO;

    @Override
    public void init() throws ServletException {
        reportDAO = new ReportDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        User me = currentUser(request);
        if (me == null) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return;
        }

        String type     = "COMMENT".equals(request.getParameter("targetType"))
                              ? "COMMENT" : "STORY";
        int targetId    = parseIntOr(request.getParameter("targetId"), 0);
        int storyId     = parseIntOr(request.getParameter("storyId"), targetId);
        String category = trimOrEmpty(request.getParameter("category"));
        String reason   = trimOrEmpty(request.getParameter("reason"));
        String back     = request.getContextPath() + "/story?action=detail&id=" + storyId;

        /*
         * THỨ TỰ KIỂM — dừng ở chỗ đầu tiên sai. Xem ISSUE-025 phase-2 §3.1.
         *
         * Ba bước đầu đều KHÔNG đụng tới file: chặn được sớm thì không có byte
         * nào phải ghi xuống đĩa.
         */

        // 1. Gửi quá dày -> chặn. Mỗi báo cáo kéo theo tối đa 3 ảnh nằm lại
        //    trên ổ đĩa, nên đây là hàng rào chống làm đầy đĩa máy chủ.
        if (RateLimiter.isReportSpam(me.getId())) {
            long wait = RateLimiter.getReportCooldownRemainingSeconds(me.getId());
            flashError(request, "Bạn gửi báo cáo quá nhanh. Thử lại sau " + wait + " giây.");
            response.sendRedirect(back);
            return;
        }

        // 2. Loại vi phạm phải hợp lệ VỚI KIỂU ĐÍCH. Danh sách trắng ở model:
        //    báo cáo bình luận không nhận "Đạo văn", báo cáo truyện không nhận
        //    "Quấy rối". Không tin ô select của trình duyệt.
        if (!Report.isValidCategory(category, type)) {
            flashError(request, "Hãy chọn loại vi phạm.");
            response.sendRedirect(back);
            return;
        }

        // 3. Chọn "Khác" thì buộc phải nói rõ là khác cái gì.
        if ("OTHER".equals(category) && reason.isEmpty()) {
            flashError(request, "Chọn \"Khác\" thì cần mô tả cụ thể vi phạm.");
            response.sendRedirect(back);
            return;
        }

        if (reason.isEmpty()) {
            reason = Report.categoryLabel(category);
        }
        if (reason.length() > 500) {
            // Cắt cho vừa cột VARCHAR(500). Để CSDL tự từ chối thì người dùng
            // nhận được một trang lỗi 500 khó hiểu thay vì báo cáo được gửi.
            reason = reason.substring(0, 500);
        }

        /*
         * 4. ĐẾM ẢNH TRƯỚC KHI LƯU BẤT KỲ FILE NÀO.
         *
         * Lưu dần rồi mới phát hiện thừa là đã có file rác nằm trên ổ đĩa mà
         * không dòng CSDL nào trỏ tới — không ai dọn được nữa, vì không biết
         * file nào là rác.
         */
        List<Part> pics = new ArrayList<>();
        for (Part p : request.getParts()) {
            if ("evidence".equals(p.getName()) && p.getSize() > 0) {
                pics.add(p);
            }
        }
        if (pics.size() > ReportDAO.MAX_EVIDENCE) {
            flashError(request, "Tối đa " + ReportDAO.MAX_EVIDENCE + " ảnh bằng chứng.");
            response.sendRedirect(back);
            return;
        }

        // 5. Lưu ảnh. Giữ danh sách đường dẫn để còn dọn nếu bước 6 hỏng.
        List<ReportEvidence> saved = new ArrayList<>();
        try {
            for (Part p : pics) {
                String path = UploadUtil.saveEvidence(p, getServletContext());
                if (path == null) continue;
                ReportEvidence ev = new ReportEvidence();
                ev.setFilePath(path);
                ev.setFileSize((int) p.getSize());
                saved.add(ev);
            }
        } catch (UploadUtil.UploadException e) {
            // Ảnh quá lớn hoặc không phải ảnh. Dọn những cái đã lưu trước đó.
            cleanUp(saved);
            flashError(request, e.getMessage());
            response.sendRedirect(back);
            return;
        }

        Report r = new Report();
        r.setReporterId(me.getId());
        r.setTargetType(type);
        r.setTargetId(targetId);
        r.setCategory(category);
        r.setReason(reason);

        try {
            // 6. Ghi báo cáo + ảnh trong MỘT transaction.
            reportDAO.insert(r, saved);
            RateLimiter.recordReport(me.getId());
            flash(request, saved.isEmpty()
                    ? "Đã gửi báo cáo. Quản trị viên sẽ xem xét."
                    : "Đã gửi báo cáo kèm " + saved.size() + " ảnh. Quản trị viên sẽ xem xét.");
        } catch (SQLException e) {
            /*
             * CSDL rollback được, ổ ĐĨA thì không. Transaction hỏng mà không
             * xoá file vừa lưu là để lại ảnh mồ côi vĩnh viễn.
             */
            cleanUp(saved);
            log("ReportServlet: không gửi được báo cáo", e);
            flashError(request, "Chưa gửi được báo cáo, thử lại sau.");
        }

        response.sendRedirect(back);
    }

    /** Xoá những ảnh đã lưu khi lượt gửi không thành. */
    private void cleanUp(List<ReportEvidence> saved) {
        for (ReportEvidence ev : saved) {
            UploadUtil.deleteEvidence(ev.getFilePath(), getServletContext());
        }
        saved.clear();
    }

    /** Báo THÀNH CÔNG — khung xanh. */
    private void flash(HttpServletRequest request, String message) {
        request.getSession().setAttribute("flash", message);
    }

    /**
     * Báo LỖI — khung đỏ.
     *
     * Phải tách khỏi {@link #flash}: layout đọc hai attribute khác nhau
     * (`flash` -> `panel-ok` xanh, `flashError` -> `panel-err` đỏ). Nhét câu
     * "Ảnh quá 2 MB" vào `flash` thì người dùng thấy một khung XANH báo lỗi —
     * trông như đã gửi xong trong khi thật ra là hỏng.
     */
    private void flashError(HttpServletRequest request, String message) {
        request.getSession().setAttribute("flashError", message);
    }
}
