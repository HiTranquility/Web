package truyen.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import truyen.model.Follow;
import truyen.util.DBConnection;
import truyen.util.DemoData;

/** Theo dõi tác giả. */
public class FollowDAO {

    /** Người này có đang theo dõi tác giả kia không? */
    public boolean isFollowing(int followerId, int authorId) throws SQLException {
        if (!DBConnection.isReady()) return DemoData.following(authorId);

        String sql = "SELECT 1 FROM follows WHERE follower_id = ? AND author_id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, followerId);
            ps.setInt(2, authorId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Bấm theo dõi. */
    public void follow(int followerId, int authorId) throws SQLException {
        if (followerId == authorId) {
            throw new SQLException("Không thể tự theo dõi chính mình.");
        }
        if (!DBConnection.isReady()) return;

        String sql = "INSERT IGNORE INTO follows (follower_id, author_id) VALUES (?, ?)";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, followerId);
            ps.setInt(2, authorId);
            ps.executeUpdate();
        }
    }

    /** Bỏ theo dõi. Xoá dòng không tồn tại thì cũng không sao. */
    public void unfollow(int followerId, int authorId) throws SQLException {
        if (!DBConnection.isReady()) return;

        String sql = "DELETE FROM follows WHERE follower_id = ? AND author_id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, followerId);
            ps.setInt(2, authorId);
            ps.executeUpdate();
        }
    }

    /**
     * Danh sách tác giả một người đang theo dõi (TRANG 17).
     *
     * Lấy kèm tên và số truyện của tác giả ngay trong một câu. Không làm vậy
     * thì trang có 20 tác giả sẽ chạy 1 + 20 + 20 = 41 câu SQL — đúng cái bẫy
     * N+1 query mà mọi tài liệu đều cảnh báo.
     */
    public List<Follow> findFollowing(int followerId) throws SQLException {
        if (!DBConnection.isReady()) return DemoData.followingList(followerId);

        String sql =
            "SELECT f.follower_id, f.author_id, f.created_at, "
          + "       u.username, u.display_name, "
          + "       (SELECT COUNT(*) FROM stories s "
          + "        WHERE s.author_id = u.id AND s.status = 'PUBLISHED') AS story_count "
          + "FROM follows f "
          + "JOIN users u ON u.id = f.author_id "
          + "WHERE f.follower_id = ? "
          + "ORDER BY f.created_at DESC";

        List<Follow> list = new ArrayList<>();
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, followerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Follow f = new Follow();
                    f.setFollowerId(rs.getInt("follower_id"));
                    f.setAuthorId(rs.getInt("author_id"));
                    f.setAuthorUsername(rs.getString("username"));
                    String name = rs.getString("display_name");
                    f.setAuthorName(name != null ? name : rs.getString("username"));
                    f.setAuthorStoryCount(rs.getInt("story_count"));
                    if (rs.getTimestamp("created_at") != null) {
                        f.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    }
                    list.add(f);
                }
            }
        }
        return list;
    }

    /** Số người đang theo dõi một tác giả — hiện trên trang hồ sơ. */
    public int countFollowers(int authorId) throws SQLException {
        if (!DBConnection.isReady()) return DemoData.followerCount(authorId);

        String sql = "SELECT COUNT(*) FROM follows WHERE author_id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, authorId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Id của tất cả người đang theo dõi một tác giả.
     *
     * Dùng khi tác giả đăng chương mới: mỗi id trong danh sách này nhận một
     * thông báo. Trả về id chứ không trả về User vì bước sau chỉ cần id để
     * chèn vào bảng notifications — lấy cả object là lãng phí.
     */
    public List<Integer> findFollowerIds(int authorId) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        if (!DBConnection.isReady()) return ids;

        String sql = "SELECT follower_id FROM follows WHERE author_id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, authorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt(1));
            }
        }
        return ids;
    }
}
