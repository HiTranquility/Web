package truyen.util;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import truyen.dao.TagDAO;
import truyen.model.Report;
import truyen.model.Tag;

/**
 * Nạp danh sách thể loại vào APPLICATION SCOPE, một lần cho cả web.
 *
 * ĐÂY LÀ CÁI SCOPE THỨ TƯ — trước giờ dự án chưa dùng tới.
 *
 *   page         biến tạm trong một file JSP        (c:set)
 *   request      dữ liệu cho MỘT lần tải trang      (setAttribute)
 *   session      theo MỘT người, qua nhiều trang    (currentUser, flash)
 *   application  chung CHO CẢ WEB, sống tới khi tắt  <-- chỗ này
 *
 * VÌ SAO THỂ LOẠI HỢP VỚI APPLICATION SCOPE
 *   Danh sách thể loại bị hỏi ở SÁU chỗ: kho truyện, form đăng truyện, form
 *   sửa truyện, bảng điều khiển, trang quản trị thể loại. Mỗi lần mở kho
 *   truyện là một câu SQL cho một danh sách mười dòng gần như không bao giờ
 *   đổi — chỉ admin sửa, và cả tháng mới sửa một lần.
 *
 *   Đặt ở request thì mỗi người mở trang là một câu. Đặt ở session thì mỗi
 *   người một bản sao, mười nghìn người là mười nghìn bản. Application scope
 *   giữ ĐÚNG MỘT bản cho tất cả — đúng bản chất của dữ liệu này.
 *
 * CÁI GIÁ, VÀ CÁCH TRẢ
 *   Dữ liệu để trong bộ nhớ thì phải tự lo cập nhật. Admin sửa thể loại mà
 *   không nạp lại là cả web hiện tên cũ cho tới lúc restart.
 *   Vì vậy AdminTagServlet gọi refresh() sau MỌI lần thêm/sửa/xoá.
 *
 * VÌ SAO LÀ LISTENER CHỨ KHÔNG PHẢI FILTER HAY SERVLET
 *   Việc này chỉ cần chạy MỘT lần lúc web khởi động, không phải mỗi request.
 *   Listener là đúng công cụ cho "làm một lần lúc bật, dọn một lần lúc tắt".
 */
@WebListener
public class AppListener implements ServletContextListener {

    /** Tên attribute trong application scope. JSP đọc bằng ${allTags}. */
    public static final String TAGS = "allTags";

    @Override
    public void contextInitialized(ServletContextEvent e) {
        refresh(e.getServletContext());
        loadReportCategories(e.getServletContext());
        e.getServletContext().log("AppListener: đã nạp danh sách thể loại");

        // Dọn dẹp view_logs định kỳ chạy ngầm (ISSUE-013)
        new Thread(() -> {
            try {
                int cleaned = new truyen.dao.ViewLogDAO().cleanOldLogs(90);
                if (cleaned > 0) {
                    e.getServletContext().log("AppListener: đã dọn dẹp " + cleaned + " bản ghi view_logs cũ (>90 ngày)");
                }
            } catch (Exception ex) {
                e.getServletContext().log("AppListener: dọn dẹp view_logs lỗi", ex);
            }
        }, "viewlogs-cleaner").start();
    }

    /**
     * Nạp lại danh sách thể loại.
     *
     * Gọi lúc khởi động và sau mỗi lần admin sửa thể loại.
     *
     * Lỗi truy vấn thì đặt danh sách RỖNG chứ không để null: JSP gặp null sẽ
     * không lặp gì cả — đúng như rỗng — nhưng mọi đoạn Java đọc vào sẽ nổ
     * NullPointerException. Rỗng thì cả hai phía đều an toàn.
     */
    public static void refresh(ServletContext ctx) {
        try {
            ctx.setAttribute(TAGS, new TagDAO().findAllWithCount());
        } catch (SQLException ex) {
            ctx.log("AppListener: không nạp được thể loại", ex);
            ctx.setAttribute(TAGS, new ArrayList<Tag>());
        }
    }

    /**
     * Lấy danh sách thể loại từ application scope.
     *
     * Chưa có (web vừa bật mà listener lỗi) thì nạp ngay tại chỗ — trang vẫn
     * chạy đúng, chỉ chậm hơn một câu SQL cho lần đầu.
     */
    @SuppressWarnings("unchecked")
    public static List<Tag> tags(ServletContext ctx) {
        Object cached = ctx.getAttribute(TAGS);
        if (cached == null) {
            refresh(ctx);
            cached = ctx.getAttribute(TAGS);
        }
        return cached == null ? new ArrayList<Tag>() : (List<Tag>) cached;
    }

    // ========================================================================
    //  LOẠI VI PHẠM CHO FORM BÁO CÁO  (ISSUE-025)
    // ========================================================================

    /**
     * Nạp danh sách loại vi phạm vào application scope.
     *
     * <p>ĐÂY LÀ ỨNG VIÊN HOÀN HẢO CHO APPLICATION SCOPE — còn hợp hơn cả thể
     * loại truyện: nó là hằng số biên dịch trong {@code Report}, không đọc từ
     * CSDL, và <b>không bao giờ đổi</b> khi web đang chạy. Nạp một lần lúc
     * khởi động là xong, không cần {@code refresh()} như thể loại.
     *
     * <p>Vì sao không để JSP tự gọi {@code Report.categoryLabel()}: EL gọi
     * phương thức static là thứ dễ vỡ giữa các phiên bản container, và nó kéo
     * tên lớp Java vào file JSP — đi ngược luật "JSP không chứa mã Java" ở
     * {@code 02-VIEW}. Một Map đặt sẵn thì JSP chỉ việc tra khoá.
     */
    public static void loadReportCategories(ServletContext ctx) {
        ctx.setAttribute("reportCategoriesStory",   Report.categoriesFor("STORY"));
        ctx.setAttribute("reportCategoriesComment", Report.categoriesFor("COMMENT"));

        // LinkedHashMap giữ đúng thứ tự khai báo — thứ tự đó là thứ tự hiện
        // trong ô chọn, và nó được sắp có chủ ý (nặng trước, "Khác" cuối).
        Map<String, String> labels = new LinkedHashMap<>();
        for (String code : Report.CATEGORIES) {
            labels.put(code, Report.categoryLabel(code));
        }
        ctx.setAttribute("reportCategoryLabels", labels);

        // Ba loại nặng — trang quản trị dùng để tô đỏ nhãn.
        ctx.setAttribute("reportSevereCategories", Report.SEVERE);
    }

    @Override
    public void contextDestroyed(ServletContextEvent e) {
        ServletContext ctx = e.getServletContext();
        ctx.removeAttribute(TAGS);
        ctx.removeAttribute("reportCategoriesStory");
        ctx.removeAttribute("reportCategoriesComment");
        ctx.removeAttribute("reportCategoryLabels");
        ctx.removeAttribute("reportSevereCategories");
    }
}
