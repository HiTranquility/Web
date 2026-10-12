package truyen.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import truyen.model.Achievement;
import truyen.model.User;
import truyen.util.DBConnection;

/**
 * Xử lý tính toán và truy vấn Huy hiệu Thành tích Độc giả (Reader Achievements).
 */
public class AchievementDAO {

    private final UserDAO userDAO = new UserDAO();

    public List<Achievement> getAchievements(int userId, User user) {
        List<Achievement> list = new ArrayList<>();
        if (userId <= 0) {
            return getDefaultMockAchievements();
        }

        int readCount = 0;
        boolean isVip = user != null && user.isVip();

        if (DBConnection.isReady()) {
            try {
                readCount = userDAO.countReadChapters(userId);
            } catch (SQLException ignored) {}
        }

        // 1. 🦉 Cú Đêm (Đọc truyện từ 23h đến 5h sáng)
        int nightReads = 0;
        if (DBConnection.isReady()) {
            String sql = "SELECT COUNT(*) FROM view_logs WHERE user_id = ? AND (HOUR(viewed_at) >= 23 OR HOUR(viewed_at) < 5)";
            try (Connection con = DBConnection.get();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) nightReads = rs.getInt(1);
                }
            } catch (SQLException ignored) {}
        }
        boolean nightOwlUnlocked = nightReads >= 1;
        list.add(new Achievement(
            "night_owl", "Cú Đêm", "🦉", "Đọc truyện",
            "Đọc truyện vào khung giờ đêm khuya tĩnh lặng (23:00 - 05:00 sáng)",
            nightOwlUnlocked, Math.min(1, nightReads), 1,
            nightOwlUnlocked ? "Đã mở khoá" : "Chưa đọc ban đêm"
        ));

        // 2. 📚 Mọt Sách (Đọc >= 30 chương truyện)
        boolean bookwormUnlocked = readCount >= 30;
        list.add(new Achievement(
            "bookworm", "Mọt Sách", "📚", "Đọc truyện",
            "Đọc tích luỹ ít nhất 30 chương truyện trên hệ thống",
            bookwormUnlocked, Math.min(30, readCount), 30,
            Math.min(30, readCount) + " / 30 chương"
        ));

        // 3. 🎓 Đại Học Giả (Đọc >= 150 chương truyện)
        boolean scholarUnlocked = readCount >= 150;
        list.add(new Achievement(
            "scholar", "Đại Học Giả", "🎓", "Đọc truyện",
            "Đọc tích luỹ trên 150 chương truyện, uyên bác thông thái",
            scholarUnlocked, Math.min(150, readCount), 150,
            Math.min(150, readCount) + " / 150 chương"
        ));

        // 4. 🪙 Mạnh Thường Quân (Tặng xu ủng hộ tác giả)
        int tipCount = 0;
        if (DBConnection.isReady()) {
            String sql = "SELECT COUNT(*) FROM transactions WHERE from_user_id = ? AND (message LIKE '%tặng%' OR story_id IS NOT NULL)";
            try (Connection con = DBConnection.get();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) tipCount = rs.getInt(1);
                }
            } catch (SQLException ignored) {}
        }
        boolean generousUnlocked = tipCount >= 1;
        list.add(new Achievement(
            "generous", "Mạnh Thường Quân", "🪙", "Cộng đồng",
            "Gửi tặng xu khích lệ tác giả sáng tác tác phẩm hay",
            generousUnlocked, Math.min(1, tipCount), 1,
            generousUnlocked ? "Đã gửi tặng tác giả" : "Chưa tặng xu"
        ));

        // 5. ✍️ Nhà Phê Bình (Gửi bài đánh giá truyện)
        int reviewCount = 0;
        if (DBConnection.isReady()) {
            String sql = "SELECT COUNT(*) FROM reviews WHERE user_id = ?";
            try (Connection con = DBConnection.get();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) reviewCount = rs.getInt(1);
                }
            } catch (SQLException ignored) {}
        }
        boolean criticUnlocked = reviewCount >= 1;
        list.add(new Achievement(
            "critic", "Nhà Phê Bình", "✍️", "Cộng đồng",
            "Viết đánh giá chuyên sâu chia sẻ góc nhìn và cảm nhận về truyện",
            criticUnlocked, Math.min(1, reviewCount), 1,
            criticUnlocked ? "Đã viết đánh giá" : "Chưa có bài đánh giá"
        ));

        // 6. 🔥 Bất Bại (Chuỗi điểm danh đạt 7 ngày)
        int maxStreak = 0;
        if (DBConnection.isReady()) {
            String sql = "SELECT COALESCE(MAX(streak_days), 0) FROM daily_checkins WHERE user_id = ?";
            try (Connection con = DBConnection.get();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) maxStreak = rs.getInt(1);
                }
            } catch (SQLException ignored) {}
        }
        boolean streakUnlocked = maxStreak >= 7;
        list.add(new Achievement(
            "streak_master", "Kỷ Lục Điểm Danh", "🔥", "Gắn bó",
            "Duy trì chuỗi điểm danh đều đặn 7 ngày trọn vẹn",
            streakUnlocked, Math.min(7, maxStreak), 7,
            Math.min(7, maxStreak) + " / 7 ngày"
        ));

        // 7. ⭐ Hội Viên Hoàng Gia (Kích hoạt Thẻ VIP)
        list.add(new Achievement(
            "vip_patron", "Hội Viên Hoàng Gia", "⭐", "Đặc quyền",
            "Kích hoạt Thẻ Hội Viên VIP để nhận chiết khấu tối đa và nhân đôi xu",
            isVip, isVip ? 1 : 0, 1,
            isVip ? "Đang kích hoạt VIP" : "Chưa đăng ký VIP"
        ));

        // 8. 📚 Nhà Sưu Tầm (Lưu >= 5 truyện vào Tủ sách)
        int bookmarkCount = 0;
        if (DBConnection.isReady()) {
            String sql = "SELECT COUNT(*) FROM bookmarks WHERE user_id = ?";
            try (Connection con = DBConnection.get();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) bookmarkCount = rs.getInt(1);
                }
            } catch (SQLException ignored) {}
        }
        boolean collectorUnlocked = bookmarkCount >= 5;
        list.add(new Achievement(
            "collector", "Nhà Sưu Tầm", "🔖", "Tủ sách",
            "Lưu trữ từ 5 tác phẩm yêu thích trở lên vào tủ sách cá nhân",
            collectorUnlocked, Math.min(5, bookmarkCount), 5,
            Math.min(5, bookmarkCount) + " / 5 bộ truyện"
        ));

        return list;
    }

    private List<Achievement> getDefaultMockAchievements() {
        List<Achievement> list = new ArrayList<>();
        list.add(new Achievement("night_owl", "Cú Đêm", "🦉", "Đọc truyện", "Đọc truyện lúc nửa đêm", true, 1, 1, "Đã mở khoá"));
        list.add(new Achievement("bookworm", "Mọt Sách", "📚", "Đọc truyện", "Đọc tích luỹ 30 chương", true, 30, 30, "30/30 chương"));
        list.add(new Achievement("scholar", "Đại Học Giả", "🎓", "Đọc truyện", "Đọc trên 150 chương", false, 45, 150, "45/150 chương"));
        list.add(new Achievement("generous", "Mạnh Thường Quân", "🪙", "Cộng đồng", "Tặng xu ủng hộ tác giả", false, 0, 1, "Chưa tặng xu"));
        list.add(new Achievement("critic", "Nhà Phê Bình", "✍️", "Cộng đồng", "Viết đánh giá truyện", true, 1, 1, "Đã viết đánh giá"));
        list.add(new Achievement("streak_master", "Kỷ Lục Điểm Danh", "🔥", "Gắn bó", "Điểm danh chuỗi 7 ngày", false, 3, 7, "3/7 ngày"));
        list.add(new Achievement("vip_patron", "Hội Viên Hoàng Gia", "⭐", "Đặc quyền", "Kích hoạt Hội Viên VIP", false, 0, 1, "Chưa đăng ký VIP"));
        list.add(new Achievement("collector", "Nhà Sưu Tầm", "🔖", "Tủ sách", "Lưu 5 truyện vào tủ sách", true, 5, 5, "5/5 bộ truyện"));
        return list;
    }
}
