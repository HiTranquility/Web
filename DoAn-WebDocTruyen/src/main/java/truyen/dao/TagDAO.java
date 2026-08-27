package truyen.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import truyen.model.Tag;
import truyen.util.DBConnection;

/** CASE 03 — Thể loại và bảng nối story_tags. */
public class TagDAO {

    /**
     * Mọi thể loại, kèm số truyện đang mang thể loại đó — cho bộ lọc hiện
     * "Tiên hiệp (24)".
     *
     * LEFT JOIN chứ không phải JOIN: thể loại chưa có truyện nào vẫn phải hiện
     * ra (với số 0). JOIN thường sẽ loại nó khỏi kết quả.
     */
    public List<Tag> findAllWithCount() throws SQLException {
        String sql =
            "SELECT t.id, t.name, t.slug, COUNT(s.id) AS story_count "
          + "FROM tags t "
          + "LEFT JOIN story_tags st ON st.tag_id = t.id "
          + "LEFT JOIN stories s ON s.id = st.story_id AND s.status = 'PUBLISHED' "
          + "GROUP BY t.id, t.name, t.slug "
          + "ORDER BY t.name";

        List<Tag> list = new ArrayList<>();
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Tag t = new Tag(rs.getInt("id"), rs.getString("name"), rs.getString("slug"));
                t.setStoryCount(rs.getInt("story_count"));
                list.add(t);
            }
        }
        return list;
    }

    /** Thể loại của MỘT truyện — hiện ở trang chi tiết. */
    public List<Tag> findByStory(int storyId) throws SQLException {
        String sql =
            "SELECT t.id, t.name, t.slug "
          + "FROM tags t JOIN story_tags st ON st.tag_id = t.id "
          + "WHERE st.story_id = ? ORDER BY t.name";

        List<Tag> list = new ArrayList<>();
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, storyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Tag(rs.getInt("id"), rs.getString("name"), rs.getString("slug")));
                }
            }
        }
        return list;
    }

    public Tag findBySlug(String slug) throws SQLException {
        String sql = "SELECT id, name, slug FROM tags WHERE slug = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, slug);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? new Tag(rs.getInt("id"), rs.getString("name"), rs.getString("slug"))
                        : null;
            }
        }
    }

    /**
     * Gán lại toàn bộ thể loại cho một truyện: xoá hết rồi thêm mới.
     *
     * VÌ SAO XOÁ HẾT RỒI THÊM LẠI, không so sánh cái nào thêm cái nào bớt:
     * số tag mỗi truyện chỉ 2-3 cái, nên cách này rẻ hơn và code ngắn hơn hẳn
     * so với việc tính hiệu hai tập hợp.
     *
     * HAI CÂU LỆNH NÀY PHẢI CHUNG MỘT TRANSACTION.
     * Nếu xoá xong mà thêm lỗi giữa chừng, truyện sẽ mất sạch thể loại. Tắt
     * autocommit, thành công thì commit, lỗi thì rollback về như cũ.
     */
    public void setTagsForStory(int storyId, String[] tagIds) throws SQLException {
        try (Connection con = DBConnection.get()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement del = con.prepareStatement(
                        "DELETE FROM story_tags WHERE story_id = ?")) {
                    del.setInt(1, storyId);
                    del.executeUpdate();
                }

                if (tagIds != null && tagIds.length > 0) {
                    try (PreparedStatement ins = con.prepareStatement(
                            "INSERT INTO story_tags (story_id, tag_id) VALUES (?, ?)")) {
                        for (String tagId : tagIds) {
                            try {
                                ins.setInt(1, storyId);
                                ins.setInt(2, Integer.parseInt(tagId));
                                ins.addBatch();     // gom lại, gửi một lần
                            } catch (NumberFormatException ignore) {
                                // tagId không phải số -> ai đó sửa form. Bỏ qua.
                            }
                        }
                        ins.executeBatch();
                    }
                }
                con.commit();

            } catch (SQLException e) {
                con.rollback();   // trả database về đúng trạng thái trước khi bắt đầu
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }
}
