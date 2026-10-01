package truyen.controller.user;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import truyen.dao.RatingDAO;
import truyen.dao.ReviewDAO;
import truyen.dao.StoryDAO;
import truyen.model.Review;
import truyen.model.Story;
import truyen.model.User;
import static truyen.util.ServletHelper.parseIntOr;
import static truyen.util.ServletHelper.currentUser;
import static truyen.util.ServletHelper.trimOrEmpty;

/**
 * Chấm sao và bài đánh giá chi tiết (ISSUE-022).
 */
@WebServlet("/rating")
public class RatingServlet extends HttpServlet {

    private RatingDAO ratingDAO;
    private StoryDAO storyDAO;
    private ReviewDAO reviewDAO;

    @Override
    public void init() throws ServletException {
        ratingDAO = new RatingDAO();
        storyDAO = new StoryDAO();
        reviewDAO = new ReviewDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        User me = currentUser(request);
        int storyId = parseIntOr(request.getParameter("storyId"), 0);

        if (me == null) {
            if (storyId > 0) {
                request.getSession(true).setAttribute("redirectAfterLogin",
                        request.getContextPath() + "/story?action=detail&id=" + storyId + "#reviews");
            }
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return;
        }

        String action = request.getParameter("action");
        if ("vote".equals(action)) {
            handleVote(request, response, me);
            return;
        } else if ("review".equals(action)) {
            handleReview(request, response, me);
            return;
        }

        // Mặc định: chấm điểm sao
        if (storyId <= 0) {
            response.sendRedirect(request.getContextPath() + "/story");
            return;
        }
        int score = parseIntOr(request.getParameter("score"), 0);

        if (score >= 1 && score <= 5) {
            try {
                Story story = storyDAO.findById(storyId);
                // Chặn tác giả tự chấm điểm truyện của chính mình
                if (story != null && story.getAuthorId() != me.getId()) {
                    ratingDAO.rate(me.getId(), storyId, score);
                }
            } catch (SQLException e) {
                log("RatingServlet: không chấm được, storyId=" + storyId, e);
            }
        }

        response.sendRedirect(request.getContextPath()
                + "/story?action=detail&id=" + storyId + "#rating");
    }

    private void handleReview(HttpServletRequest request, HttpServletResponse response, User me)
            throws IOException {
        int storyId = parseIntOr(request.getParameter("storyId"), 0);
        if (storyId <= 0) {
            response.sendRedirect(request.getContextPath() + "/story");
            return;
        }
        String title = trimOrEmpty(request.getParameter("title"));
        String content = trimOrEmpty(request.getParameter("content"));
        boolean hasSpoiler = "1".equals(request.getParameter("hasSpoiler"))
                          || "true".equalsIgnoreCase(request.getParameter("hasSpoiler"))
                          || "on".equalsIgnoreCase(request.getParameter("hasSpoiler"));
        int score = parseIntOr(request.getParameter("score"), 0);

        if (title.isEmpty() || content.isEmpty()) {
            request.getSession().setAttribute("flashError", "Tiêu đề và nội dung cảm nhận không được để trống.");
            response.sendRedirect(request.getContextPath() + "/story?action=detail&id=" + storyId + "#reviews");
            return;
        }

        if (title.length() > 150) {
            title = title.substring(0, 150);
        }
        if (content.length() > 4000) {
            content = content.substring(0, 4000);
        }

        try {
            Story story = storyDAO.findById(storyId);
            if (story == null || story.getAuthorId() == me.getId()) {
                request.getSession().setAttribute("flashError", "Tác giả không thể tự đánh giá truyện của mình.");
                response.sendRedirect(request.getContextPath() + "/story?action=detail&id=" + storyId + "#reviews");
                return;
            }

            // Nếu người dùng cũng gửi điểm sao kèm bài đánh giá
            if (score >= 1 && score <= 5) {
                ratingDAO.rate(me.getId(), storyId, score);
            } else {
                // Đảm bảo đã có ít nhất một lượt rate (mặc định 5 sao nếu chưa chấm)
                int existingScore = ratingDAO.findScore(me.getId(), storyId);
                if (existingScore <= 0) {
                    ratingDAO.rate(me.getId(), storyId, 5);
                }
            }

            Review review = new Review();
            review.setUserId(me.getId());
            review.setStoryId(storyId);
            review.setTitle(title);
            review.setContent(content);
            review.setHasSpoiler(hasSpoiler);

            boolean ok = reviewDAO.upsert(review);
            if (ok) {
                request.getSession().setAttribute("flash", "🌟 Cảm ơn bạn đã gửi bài đánh giá!");
            }
        } catch (SQLException e) {
            log("RatingServlet: không lưu được bài đánh giá", e);
            request.getSession().setAttribute("flashError", "Có lỗi xảy ra khi lưu bài đánh giá.");
        }

        response.sendRedirect(request.getContextPath() + "/story?action=detail&id=" + storyId + "#reviews");
    }

    private void handleVote(HttpServletRequest request, HttpServletResponse response, User me)
            throws IOException {
        int reviewId = parseIntOr(request.getParameter("reviewId"), 0);
        int storyId = parseIntOr(request.getParameter("storyId"), 0);

        if (storyId <= 0) {
            response.sendRedirect(request.getContextPath() + "/story");
            return;
        }

        if (reviewId > 0) {
            try {
                boolean voted = reviewDAO.toggleVote(reviewId, me.getId());
                // Nếu là AJAX
                if ("XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"))) {
                    response.setContentType("application/json; charset=UTF-8");
                    response.getWriter().write("{\"success\":true,\"voted\":" + voted + "}");
                    return;
                }
            } catch (SQLException e) {
                log("RatingServlet: không vote được reviewId=" + reviewId, e);
            }
        }

        response.sendRedirect(request.getContextPath() + "/story?action=detail&id=" + storyId + "#reviews");
    }
}
