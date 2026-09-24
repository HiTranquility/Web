package truyen.controller.common;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import truyen.dao.StoryDAO;
import truyen.model.Story;

/**
 * Tự động sinh XML Sitemap chuẩn sitemaps.org cho công cụ tìm kiếm (ISSUE-012).
 */
@WebServlet(name = "SitemapServlet", urlPatterns = "/sitemap.xml")
public class SitemapServlet extends HttpServlet {

    private final StoryDAO storyDAO = new StoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/xml; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        String baseUrl = req.getScheme() + "://" + req.getServerName()
                + (req.getServerPort() == 80 || req.getServerPort() == 443 ? "" : ":" + req.getServerPort())
                + req.getContextPath();

        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.println("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");

        // Các trang tĩnh quan trọng
        writeUrl(out, baseUrl + "/", "1.0", "daily");
        writeUrl(out, baseUrl + "/story?action=list", "0.9", "daily");
        writeUrl(out, baseUrl + "/rank", "0.8", "daily");
        writeUrl(out, baseUrl + "/page?name=rules", "0.5", "monthly");
        writeUrl(out, baseUrl + "/page?name=guide", "0.5", "monthly");

        // Các truyện mới nhất
        try {
            List<Story> stories = storyDAO.findPage(null, null, "latest", null, 0, 100);
            for (Story s : stories) {
                writeUrl(out, baseUrl + "/story?action=detail&amp;id=" + s.getId(), "0.8", "weekly");
            }
        } catch (SQLException ignored) {
            // Giữ cho XML luôn hợp lệ ngay cả khi DB bận
        }

        out.println("</urlset>");
    }

    private void writeUrl(PrintWriter out, String loc, String priority, String changefreq) {
        out.println("  <url>");
        out.println("    <loc>" + loc + "</loc>");
        out.println("    <changefreq>" + changefreq + "</changefreq>");
        out.println("    <priority>" + priority + "</priority>");
        out.println("  </url>");
    }
}
