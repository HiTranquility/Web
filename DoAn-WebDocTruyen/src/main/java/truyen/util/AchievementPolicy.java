package truyen.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import truyen.model.Achievement;

/** Quy tắc thuần: cùng danh mục cho giao diện, thưởng và xếp hạng đề xuất. */
public final class AchievementPolicy {
    public static final int MAX_DAILY_BONUS = 20;
    public static final int MAX_RECOMMENDATION_BOOST = 10;
    private AchievementPolicy() { }

    public static List<Achievement> catalog(Map<String, Integer> stats, boolean vip) {
        List<Achievement> list = new ArrayList<>();
        add(list, stats, "night_owl", "Cú Đêm", "🦉", "Đọc truyện", "Đọc một chương trong khung 23:00–05:00.", "night_reads", 1, 2, 1, 5);
        add(list, stats, "bookworm", "Mọt Sách", "📚", "Đọc truyện", "Đọc 30 chương khác nhau.", "read_count", 30, 3, 1, 15);
        add(list, stats, "scholar", "Đại Học Giả", "🎓", "Đọc truyện", "Đọc 150 chương khác nhau.", "read_count", 150, 5, 2, 30);
        add(list, stats, "generous", "Mạnh Thường Quân", "🪙", "Cộng đồng", "Gửi tặng xu cho một tác giả khác; mở khóa chương không tính là tặng xu.", "tip_count", 1, 2, 1, 5);
        add(list, stats, "critic", "Nhà Phê Bình", "✍️", "Cộng đồng", "Viết một đánh giá từ 80 ký tự cho truyện công khai của người khác.", "review_count", 1, 2, 1, 5);
        add(list, stats, "streak_master", "Kỷ Lục Điểm Danh", "🔥", "Gắn bó", "Hoàn thành một chuỗi điểm danh 7 ngày.", "max_streak", 7, 4, 1, 20);
        add(list, stats, "collector", "Nhà Sưu Tầm", "🔖", "Tủ sách", "Lưu 5 tác phẩm khác nhau vào tủ sách.", "bookmark_count", 5, 2, 1, 5);
        add(list, stats, "first_story", "Khởi Đầu Sáng Tác", "🖋️", "Sáng tác", "Có truyện công khai với ít nhất một chương đủ nội dung.", "published_count", 1, 3, 4, 20);
        add(list, stats, "steady_author", "Ngòi Bút Bền Bỉ", "📜", "Sáng tác", "Có 10 chương đủ nội dung trong các truyện công khai.", "published_chapters", 10, 4, 6, 40);
        add(list, stats, "finished_author", "Về Đích", "🏁", "Sáng tác", "Hoàn thành một truyện công khai có ít nhất 10 chương đủ nội dung.", "completed_count", 1, 5, 8, 100);
        Achievement membership = new Achievement("vip_patron", "Hội Viên Hoàng Gia", "⭐", "Hội viên",
                "Đặc quyền VIP theo thời hạn hiện có; không tăng uy tín đề xuất và không thưởng xu thành tựu.",
                vip, vip ? 1 : 0, 1, vip ? "VIP đang hiệu lực" : "VIP chưa hiệu lực");
        list.add(membership);
        return list;
    }

    private static void add(List<Achievement> list, Map<String, Integer> stats, String id, String title,
                            String icon, String category, String description, String stat, int target,
                            int bonus, int weight, int coins) {
        int progress = Math.max(0, stats.getOrDefault(stat, 0));
        Achievement value = new Achievement(id, title, icon, category, description, progress >= target,
                Math.min(target, progress), target, Math.min(target, progress) + " / " + target);
        value.setBonusPercent(bonus); value.setRecommendationWeight(weight); value.setRewardCoins(coins);
        list.add(value);
    }

    public static int dailyBonus(List<Achievement> achievements) {
        return Math.min(MAX_DAILY_BONUS, achievements.stream().filter(Achievement::isUnlocked)
                .mapToInt(Achievement::getBonusPercent).sum());
    }

    public static int recommendationBoost(List<Achievement> achievements) {
        return Math.min(MAX_RECOMMENDATION_BOOST, achievements.stream().filter(Achievement::isUnlocked)
                .mapToInt(Achievement::getRecommendationWeight).sum());
    }

    /** Cộng % huy hiệu trên thưởng gốc, không nhân chồng lên VIP/cấp bậc. */
    public static int dailyReward(int base, double legacyMultiplier, int badgeBonus) {
        if (base <= 0) return 0;
        double multiplier = Double.isFinite(legacyMultiplier) ? Math.max(1, Math.min(2, legacyMultiplier)) : 1;
        return (int) Math.round(base * (multiplier + Math.max(0, Math.min(MAX_DAILY_BONUS, badgeBonus)) / 100.0));
    }
}
