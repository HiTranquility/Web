package truyen.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import truyen.model.Notification;
import truyen.util.DBConnection;
import truyen.util.DemoData;

/**
 * Thông báo chương mới.
 *
 * TẦNG: dao/
 */
public class NotificationDAO {

    /** Thông báo của một người, mới nhất trước. */
    public List<Notification> findByUser(int userId, int limit) throws SQLException {
        if (!DBConnection.isReady()) return DemoData.notifications(userId);

        String sql = "SELECT * FROM notifications WHERE user_id = ? "
                   + "ORDER BY created_at DESC LIMIT ?";
        List<Notification> list = new ArrayList<>();
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Số thông báo chưa đọc — hiện chấm đỏ trên thanh menu. */
    public int countUnread(int userId) throws SQLException {
        if (!DBConnection.isReady()) return DemoData.unreadCount(userId);

        String sql = "SELECT COUNT(*) FROM notifications "
                   + "WHERE user_id = ? AND is_read = FALSE";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Đánh dấu tất cả là đã đọc.
     *
     * Làm gộp một câu chứ không đánh dấu từng cái lúc người dùng bấm vào.
     * Người mở trang thông báo tức là đã nhìn thấy hết — không cần biết họ
     * bấm vào cái nào.
     */
    public void markAllRead(int userId) throws SQLException {
        if (!DBConnection.isReady()) {
            // Che do xem giao dien: ghi vao bo nho de cham do tat that,
            // giong het luc co CSDL. Xem ghi chu trong DemoData.markRead().
            DemoData.markRead(userId);
            return;
        }

        String sql = "UPDATE notifications SET is_read = TRUE "
                   + "WHERE user_id = ? AND is_read = FALSE";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    /** Gửi CÙNG MỘT thông báo cho nhiều người — tác giả đăng chương mới. */
    public void notifyFollowers(List<Integer> userIds, int storyId, int chapterId,
                                String message) throws SQLException {
        if (userIds == null || userIds.isEmpty()) return;
        if (!DBConnection.isReady()) return;

        String sql = "INSERT INTO notifications "
                   + "(user_id, story_id, chapter_id, type, message) "
                   + "VALUES (?, ?, ?, 'NEW_CHAPTER', ?)";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (Integer uid : userIds) {
                ps.setInt(1, uid);
                ps.setInt(2, storyId);
                ps.setInt(3, chapterId);
                ps.setString(4, message);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private Notification mapRow(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getInt("id"));
        n.setUserId(rs.getInt("user_id"));

        // getInt trả 0 cho NULL, không phân biệt được với id thật.
        // Phải hỏi lại wasNull() ngay sau đó.
        int sid = rs.getInt("story_id");
        n.setStoryId(rs.wasNull() ? null : sid);
        int cid = rs.getInt("chapter_id");
        n.setChapterId(rs.wasNull() ? null : cid);

        n.setType(rs.getString("type"));
        n.setMessage(rs.getString("message"));
        n.setRead(rs.getBoolean("is_read"));
        if (rs.getTimestamp("created_at") != null) {
            n.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        return n;
    }
}
