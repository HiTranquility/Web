package truyen.util;

import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpSession;

import truyen.model.Chapter;
import truyen.model.Story;
import truyen.model.User;

/**
 * MỘT luật duy nhất cho câu hỏi "người này đọc được chương này không".
 *
 * Trước đây luật nằm rải ở ChapterServlet.read(), .raw() và BookmarkServlet —
 * ba bản chép tay của cùng một chuỗi if. Truyện tranh thêm chỗ thứ tư (phát
 * ảnh trang ở UploadedFileServlet); chép thêm lần nữa là sớm muộn có một chỗ
 * lệch, và chỗ lệch nào cũng là một lỗ đọc chùa chương VIP.
 *
 * LUẬT
 *   - Truyện đã gỡ (DELETED)       -> không ai đọc
 *   - Truyện nháp (DRAFT)          -> chỉ tác giả và admin
 *   - Chương VIP                   -> tác giả, admin, hoặc người đã mở khoá
 *   - Còn lại                      -> mọi người ĐÃ ĐĂNG NHẬP (nghiệp vụ của web)
 */
public final class ChapterAccess {

    /** Hỏi "đã mở khoá chưa" — tách ra để test không cần CSDL. */
    public interface UnlockCheck {
        boolean hasUnlocked(int userId, int chapterId) throws SQLException;
    }

    /** Tên thuộc tính phiên giữ các chương đã được cấp quyền đọc trong phiên này. */
    private static final String SESSION_KEY = "readableChapters";

    private ChapterAccess() { }

    public static boolean canEdit(User me, Story story) {
        return me != null && story != null
                && (me.getId() == story.getAuthorId() || me.isAdmin() || me.hasPermission("MANAGE_STORIES"));
    }

    /** Truyện còn hiện với người này không (bỏ qua luật VIP của từng chương). */
    public static boolean canSeeStory(User me, Story story) {
        if (me == null || story == null || "DELETED".equals(story.getStatus())) return false;
        return !"DRAFT".equals(story.getStatus()) || canEdit(me, story);
    }

    /** Chương VIP này có đang KHOÁ với người này không. */
    public static boolean isLocked(User me, Story story, Chapter chapter, UnlockCheck unlocks)
            throws SQLException {
        if (chapter == null || !chapter.isVip() || chapter.getCoinPrice() <= 0) return false;
        if (me == null) return true;
        if (canEdit(me, story) || me.hasPermission("READ_VIP_FREE")) return false;
        return !unlocks.hasUnlocked(me.getId(), chapter.getId());
    }

    public static boolean canRead(User me, Story story, Chapter chapter, UnlockCheck unlocks)
            throws SQLException {
        return chapter != null && canSeeStory(me, story) && !isLocked(me, story, chapter, unlocks);
    }

    // ---- Cấp quyền theo phiên cho ảnh trang truyện tranh ------------------

    /**
     * Ghi nhớ "phiên này đã được đọc chương X" lúc ChapterServlet trả trang.
     *
     * Một chương truyện tranh 40 trang là 40 request ảnh. Kiểm quyền bằng CSDL
     * cho từng ảnh là 120 câu SQL cho một lần lật chương. Ghi nhớ trong phiên
     * thì ảnh chỉ việc tra một tập hợp trong bộ nhớ; lần đầu (vd. mở thẳng
     * link ảnh) mới phải hỏi CSDL.
     *
     * ConcurrentHashMap.newKeySet(): ảnh tải SONG SONG nên nhiều request cùng
     * phiên đọc/ghi tập này cùng lúc — HashSet thường sẽ hỏng ngầm.
     */
    public static void grant(HttpSession session, int chapterId) {
        if (session == null) return;
        readable(session).add(chapterId);
    }

    public static boolean isGranted(HttpSession session, int chapterId) {
        if (session == null) return false;
        Object o = session.getAttribute(SESSION_KEY);
        return o instanceof Set && ((Set<?>) o).contains(chapterId);
    }

    @SuppressWarnings("unchecked")
    private static Set<Integer> readable(HttpSession session) {
        synchronized (session) {
            Object o = session.getAttribute(SESSION_KEY);
            if (o instanceof Set) return (Set<Integer>) o;
            Set<Integer> s = ConcurrentHashMap.newKeySet();
            session.setAttribute(SESSION_KEY, s);
            return s;
        }
    }
}
