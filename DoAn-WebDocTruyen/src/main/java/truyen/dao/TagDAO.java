package truyen.dao;

import truyen.util.DemoData;

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
        // CHE DO XEM GIAO DIEN: chua co db.properties thi lay du lieu gia.
        if (!DBConnection.isReady()) return DemoData.tags();
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

    /**
     * The loai duoc DOC nhieu nhat — cong luot xem cua moi truyen trong the loai.
     *
     * KHAC findAllWithCount(): ham kia dem SO TRUYEN, ham nay cong LUOT XEM.
     * Ban dang ky de tai ghi "The loai duoc doc nhieu nhat", ma mot the loai
     * co 10 truyen khong ai doc thi khong the goi la duoc doc nhieu.
     *
     * COALESCE cho SUM: the loai chua co truyen nao thi SUM tra ve NULL.
     * LEFT JOIN de the loai rong van xuat hien, chi la o cuoi bang.
     */
    public List<Tag> findTopByViews(int limit) throws SQLException {
        if (!DBConnection.isReady()) return DemoData.tagsByViews(limit);

        String sql =
            "SELECT t.id, t.name, t.slug, "
          + "       COUNT(DISTINCT s.id) AS story_count, "
          + "       COALESCE(SUM(s.view_count), 0) AS view_total "
          + "FROM tags t "
          + "LEFT JOIN story_tags st ON st.tag_id = t.id "
          + "LEFT JOIN stories s ON s.id = st.story_id AND s.status = 'PUBLISHED' "
          + "GROUP BY t.id, t.name, t.slug "
          + "ORDER BY view_total DESC "
          + "LIMIT ?";

        List<Tag> list = new ArrayList<>();
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Tag t = new Tag();
                    t.setId(rs.getInt("id"));
                    t.setName(rs.getString("name"));
                    t.setSlug(rs.getString("slug"));
                    t.setStoryCount(rs.getInt("story_count"));
                    t.setViewCount(rs.getInt("view_total"));
                    list.add(t);
                }
            }
        }
        return list;
    }

    /** Thể loại của MỘT truyện — hiện ở trang chi tiết. */
    public List<Tag> findByStory(int storyId) throws SQLException {
        // CHE DO XEM GIAO DIEN: chua co db.properties thi lay du lieu gia.
        if (!DBConnection.isReady()) return DemoData.story(storyId) == null
                    ? new java.util.ArrayList<Tag>()
                    : DemoData.story(storyId).getTags();
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
        // CHE DO XEM GIAO DIEN: chua co db.properties thi lay du lieu gia.
        if (!DBConnection.isReady()) return DemoData.tagBySlug(slug);
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
     * Them the loai moi — trang 28.
     *
     * slug do SlugUtil sinh tu ten, khong de nguoi dung tu go. Go tay thi som
     * muon co ai do dat slug "Ngon Tinh" co dau cach va chu hoa, roi duong dan
     * /story?action=list&tag=Ngon%20Tinh trong rat xau va de go sai.
     */
    public void insert(Tag tag) throws SQLException {
        if (!DBConnection.isReady()) return;

        String sql = "INSERT INTO tags (name, slug) VALUES (?, ?)";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tag.getName());
            ps.setString(2, tag.getSlug());
            ps.executeUpdate();
        }
    }

    /** Doi ten the loai. */
    public void update(Tag tag) throws SQLException {
        if (!DBConnection.isReady()) return;

        String sql = "UPDATE tags SET name = ?, slug = ? WHERE id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tag.getName());
            ps.setString(2, tag.getSlug());
            ps.setInt(3, tag.getId());
            ps.executeUpdate();
        }
    }

    /** Xoa the loai — XOA THAT, khong phai xoa mem. */
    public void delete(int id) throws SQLException {
        if (!DBConnection.isReady()) return;

        try (Connection con = DBConnection.get()) {
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COUNT(*) FROM story_tags WHERE tag_id = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new SQLException(
                                "The loai nay dang duoc " + rs.getInt(1)
                              + " truyen su dung. Go nhan khoi cac truyen do truoc.");
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM tags WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        }
    }

    /** Kiem tra trung slug truoc khi them/sua. exceptId = 0 khi them moi. */
    public boolean slugExists(String slug, int exceptId) throws SQLException {
        if (!DBConnection.isReady()) return false;

        String sql = "SELECT 1 FROM tags WHERE slug = ? AND id <> ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, slug);
            ps.setInt(2, exceptId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Gán lại toàn bộ thể loại cho một truyện: xoá hết rồi thêm mới. */
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
