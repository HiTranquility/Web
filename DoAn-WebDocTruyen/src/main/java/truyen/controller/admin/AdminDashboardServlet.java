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
import truyen.util.AppListener;

/** TRANG 25 — Bảng điều khiển quản trị. */
@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    private StoryDAO storyDAO;
    private ReportDAO reportDAO;
    private TagDAO tagDAO;

    @Override
    public void init() throws ServletException {
        storyDAO = new StoryDAO();
        reportDAO = new ReportDAO();
        tagDAO = new TagDAO();
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

            /*
             * Ba bieu do 14 ngay gan nhat.
             *
             * max tinh o DAY chu khong o JSP: EL khong co ham max() cho mang,
             * tinh trong JSP phai chay them mot vong lap chi de tim so lon nhat.
             */
            int[] dStories = storyDAO.countByDay("stories", 14);
            int[] dUsers   = storyDAO.countByDay("users", 14);
            int[] dViews   = storyDAO.countByDay("views", 14);
            request.setAttribute("dStories", dStories);
            request.setAttribute("dUsers", dUsers);
            request.setAttribute("dViews", dViews);
            request.setAttribute("mStories", max(dStories));
            request.setAttribute("mUsers", max(dUsers));
            request.setAttribute("mViews", max(dViews));

            request.setAttribute("pending", reportDAO.countPending());
            request.setAttribute("topStories", storyDAO.findTop("views", 5));
            // The loai xep theo LUOT XEM, khong phai so truyen — dung nhu ban
            // dang ky de tai ghi "The loai duoc doc nhieu nhat".
            // Khong doc tu cache application scope: cache do giu so TRUYEN.
            request.setAttribute("tags", tagDAO.findTopByViews(8));

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

    /** Gia tri lon nhat trong mang, 0 neu mang rong. */
    private int max(int[] a) {
        int m = 0;
        for (int x : a) if (x > m) m = x;
        return m;
    }
}
