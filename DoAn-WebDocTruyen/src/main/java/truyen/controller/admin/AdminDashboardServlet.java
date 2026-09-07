package truyen.controller.admin;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import truyen.dao.ReportDAO;
import truyen.dao.StoryDAO;
import truyen.dao.TagDAO;

/**
 * TRANG 25 — Bảng điều khiển quản trị.
 *
 * TẦNG: controller/admin/
 *
 * URL: /admin/dashboard
 *
 * VÌ SAO GỌI MỘT HÀM adminOverview() TRẢ VỀ MẢNG THAY VÌ SÁU HÀM ĐẾM
 *   Sáu câu COUNT riêng là sáu lần đi lại với MySQL cho một trang chỉ hiện
 *   sáu con số. StoryDAO.adminOverview() gộp tất cả vào MỘT câu bằng các
 *   subquery — một lần đi, sáu con số về.
 *
 *   Cái giá là trả về int[] chứ không phải một object có tên trường rõ ràng,
 *   nên phải nhớ thứ tự. Vì vậy thứ tự được ghi ngay dưới đây và trong
 *   Javadoc của adminOverview(). Có nhiều hơn sáu con số thì nên tạo hẳn một
 *   lớp DashboardStats.
 */
@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    private StoryDAO storyDAO;
    private TagDAO tagDAO;
    private ReportDAO reportDAO;

    @Override
    public void init() throws ServletException {
        storyDAO = new StoryDAO();
        tagDAO = new TagDAO();
        reportDAO = new ReportDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        try {
            // Thứ tự: [0] truyện · [1] chương · [2] tài khoản
            //         [3] tổng lượt xem · [4] bình luận · [5] truyện bị gỡ
            int[] o = storyDAO.adminOverview();
            request.setAttribute("cStories",  o[0]);
            request.setAttribute("cChapters", o[1]);
            request.setAttribute("cUsers",    o[2]);
            request.setAttribute("cViews",    o[3]);
            request.setAttribute("cComments", o[4]);
            request.setAttribute("cDeleted",  o[5]);

            request.setAttribute("pending", reportDAO.countPending());
            request.setAttribute("topStories", storyDAO.findTop("views", 5));
            request.setAttribute("tags", tagDAO.findAllWithCount());

        } catch (SQLException e) {
            log("AdminDashboardServlet: lỗi truy vấn", e);
            request.setAttribute("message", "Không tải được số liệu tổng quan.");
        }

        request.setAttribute("pageTitle", "Quản trị — Bảng điều khiển");
        request.setAttribute("activeNav", "admin");
        request.setAttribute("adminSection", "dashboard");
        request.setAttribute("contentPage", "/WEB-INF/views/admin/dashboard.jsp");
        getServletContext()
                .getRequestDispatcher("/WEB-INF/views/layout/admin.jsp")
                .forward(request, response);
    }
}
