package truyen.controller.user;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import truyen.dao.NotificationDAO;
import truyen.dao.ReportDAO;
import truyen.dao.StoryDAO;
import truyen.model.Report;
import truyen.model.Story;
import truyen.model.User;
import truyen.util.MailSender;
import truyen.util.ServletHelper;
import static truyen.util.ServletHelper.currentUser;
import static truyen.util.ServletHelper.parseIntOr;
import static truyen.util.ServletHelper.trimOrEmpty;

/**
 * Tiếp nhận thư khiếu nại kiểm duyệt từ tác giả tới Ban Quản Trị.
 *
 * <p>Quy trình xử lý:
 * <ol>
 *   <li>Xác thực tác giả sở hữu truyện bị can thiệp / kiểm duyệt.</li>
 *   <li>Lưu phiếu khiếu nại vào bảng {@code reports} với nhãn [KHIẾU NẠI KIỂM DUYỆT].</li>
 *   <li>Gửi email thông báo bất đồng bộ qua JavaMail SMTP (MailSender) tới hòm thư Admin.</li>
 *   <li>Gửi thông báo xác nhận trong ứng dụng cho tác giả.</li>
 * </ol>
 *
 * TẦNG: controller/ — không SQL trực tiếp, không nhúng HTML.
 */
@WebServlet("/appeal")
public class AppealServlet extends HttpServlet {

    private StoryDAO storyDAO;
    private ReportDAO reportDAO;
    private NotificationDAO notificationDAO;

    @Override
    public void init() throws ServletException {
        storyDAO = new StoryDAO();
        reportDAO = new ReportDAO();
        notificationDAO = new NotificationDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User me = currentUser(request);
        if (me == null) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return;
        }

        int storyId = parseIntOr(request.getParameter("storyId"), 0);

        try {
            List<Story> myStories = storyDAO.findByAuthor(me.getId());
            Story selectedStory = null;
            if (storyId > 0) {
                selectedStory = storyDAO.findById(storyId);
                // Nếu truyện không thuộc về tác giả thì bỏ chọn
                if (selectedStory != null && selectedStory.getAuthorId() != me.getId()) {
                    selectedStory = null;
                    storyId = 0;
                }
            }

            request.setAttribute("myStories", myStories);
            request.setAttribute("selectedStory", selectedStory);
            request.setAttribute("storyId", storyId);
        } catch (SQLException e) {
            log("AppealServlet: Không nạp được danh sách truyện của tác giả", e);
            request.setAttribute("message", "Không thể tải thông tin truyện.");
        }

        request.setAttribute("pageTitle", "Gửi thư khiếu nại tới Admin");
        request.setAttribute("activeNav", "appeal");
        request.setAttribute("contentPage", "/WEB-INF/views/user/appeal.jsp");
        getServletContext()
                .getRequestDispatcher("/WEB-INF/views/layout/main.jsp")
                .forward(request, response);
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

        int storyId = parseIntOr(request.getParameter("storyId"), 0);
        String appealTitle = trimOrEmpty(request.getParameter("appealTitle"));
        String appealReason = trimOrEmpty(request.getParameter("appealReason"));

        try {
            List<Story> myStories = storyDAO.findByAuthor(me.getId());
            request.setAttribute("myStories", myStories);

            if (storyId <= 0) {
                returnBackWithError(request, response, "Vui lòng chọn tác phẩm cần gửi khiếu nại.", appealTitle, appealReason, storyId);
                return;
            }

            Story story = storyDAO.findById(storyId);
            if (story == null || story.getAuthorId() != me.getId()) {
                returnBackWithError(request, response, "Bạn chỉ có thể gửi thư khiếu nại cho tác phẩm do chính mình sáng tác.", appealTitle, appealReason, storyId);
                return;
            }
            request.setAttribute("selectedStory", story);

            if (appealReason.isEmpty()) {
                returnBackWithError(request, response, "Vui lòng nhập nội dung giải trình chi tiết.", appealTitle, appealReason, storyId);
                return;
            }

            if (appealReason.length() < 15) {
                returnBackWithError(request, response, "Nội dung giải trình quá ngắn (tối thiểu 15 ký tự) để Admin có đủ cơ sở xem xét.", appealTitle, appealReason, storyId);
                return;
            }

            if (appealReason.length() > 3000) {
                returnBackWithError(request, response, "Nội dung giải trình tối đa 3000 ký tự.", appealTitle, appealReason, storyId);
                return;
            }

            // 1. Tạo bản ghi báo cáo/khiếu nại vào CSDL
            Report r = new Report();
            r.setReporterId(me.getId());
            r.setTargetType("STORY");
            r.setTargetId(storyId);
            r.setCategory("OTHER");

            String fullReason = "[KHIẾU NẠI KIỂM DUYỆT] "
                    + (appealTitle.isEmpty() ? "" : ("[" + appealTitle + "] "))
                    + appealReason;
            if (fullReason.length() > 500) {
                r.setReason(fullReason.substring(0, 497) + "...");
            } else {
                r.setReason(fullReason);
            }
            reportDAO.insert(r);

            // 2. Gửi email thông báo bất đồng bộ tới hòm thư Admin
            String authorEmail = (me.getEmail() != null && !me.getEmail().trim().isEmpty())
                    ? me.getEmail().trim()
                    : "no-email@user.local";
            MailSender.sendAppealEmailAsync(
                    me.getDisplayName(),
                    authorEmail,
                    story.getTitle(),
                    storyId,
                    appealTitle.isEmpty() ? "Khiếu nại kiểm duyệt truyện" : appealTitle,
                    appealReason
            );

            // 3. Gửi thông báo in-app xác nhận tiếp nhận cho tác giả
            notificationDAO.sendNotification(
                    me.getId(),
                    storyId,
                    null,
                    "SYSTEM",
                    "Hệ thống đã tiếp nhận thư khiếu nại cho truyện \"" + story.getTitle() + "\". Quản trị viên sẽ xem xét và phản hồi trong thời gian sớm nhất."
            );

            // 4. Báo thành công và điều hướng
            request.getSession().setAttribute("flash", "✉️ Đã gửi thư khiếu nại tới Ban Quản Trị thành công! Quản trị viên sẽ kiểm tra hòm thư và hệ thống để phản hồi bạn.");
            response.sendRedirect(request.getContextPath() + "/notification");

        } catch (SQLException e) {
            log("AppealServlet: lỗi xử lý khiếu nại", e);
            returnBackWithError(request, response, "Không thể gửi thư khiếu nại do lỗi hệ thống. Vui lòng thử lại sau.", appealTitle, appealReason, storyId);
        }
    }

    private void returnBackWithError(HttpServletRequest request, HttpServletResponse response,
                                     String error, String appealTitle, String appealReason, int storyId)
            throws ServletException, IOException {
        request.setAttribute("message", error);
        request.setAttribute("appealTitle", appealTitle);
        request.setAttribute("appealReason", appealReason);
        request.setAttribute("storyId", storyId);
        request.setAttribute("pageTitle", "Gửi thư khiếu nại tới Admin");
        request.setAttribute("activeNav", "appeal");
        request.setAttribute("contentPage", "/WEB-INF/views/user/appeal.jsp");
        getServletContext()
                .getRequestDispatcher("/WEB-INF/views/layout/main.jsp")
                .forward(request, response);
    }
}

