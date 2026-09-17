package truyen.controller.user;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import truyen.dao.RatingDAO;
import truyen.dao.StoryDAO;
import truyen.model.Story;
import truyen.model.User;
import static truyen.util.ServletHelper.parseIntOr;
import static truyen.util.ServletHelper.currentUser;

/** Chấm sao truyện. */
@WebServlet("/rating")
public class RatingServlet extends HttpServlet {

    private RatingDAO ratingDAO;
    private StoryDAO storyDAO;

    @Override
    public void init() throws ServletException {
        ratingDAO = new RatingDAO();
        storyDAO = new StoryDAO();
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
        int score   = parseIntOr(request.getParameter("score"), 0);

        if (score >= 1 && score <= 5 && storyId > 0) {
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

        /*
         * Redirect sau POST (mẫu Post/Redirect/Get).
         */
        response.sendRedirect(request.getContextPath()
                + "/story?action=detail&id=" + storyId + "#rating");
    }
}
