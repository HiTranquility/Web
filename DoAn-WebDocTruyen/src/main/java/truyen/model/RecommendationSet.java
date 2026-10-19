package truyen.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import truyen.util.AchievementPolicy;

/**
 * Kết quả gợi ý truyện — danh sách truyện + LÝ DO gợi ý từng truyện.
 *
 * VÌ SAO LÝ DO NẰM Ở MAP RIÊNG CHỨ KHÔNG GẮN VÀO Story
 *   Ở chế độ xem giao diện (chưa có CSDL), DemoData trả về CÙNG một object
 *   Story cho mọi trang. Ghi lý do vào object đó là trang chủ, kho truyện...
 *   cũng hiện theo nhãn "Cùng thể loại". Map theo id thì không đụng gì tới
 *   object dùng chung.
 *
 *   JSP đọc bằng ${recReasons[story.id]} — story.id qua getter là Integer nên
 *   khớp khoá của Map<Integer, String>.
 */
public class RecommendationSet {

    public static final String REASON_ALSO_READ = "Độc giả cũng đọc";
    public static final String REASON_SAME_TAG  = "Cùng thể loại";
    public static final String REASON_TRENDING  = "Đang được đọc nhiều";
    public static final String REASON_NEW_AUTHOR = "Khám phá tác giả mới";

    private final List<Story> stories = new ArrayList<>();
    private final Map<Integer, String> reasons = new LinkedHashMap<>();

    public List<Story> getStories() { return stories; }
    public Map<Integer, String> getReasons() { return reasons; }

    public boolean isEmpty() { return stories.isEmpty(); }
    public int size() { return stories.size(); }

    /**
     * Gộp nhiều nguồn gợi ý theo THỨ TỰ ƯU TIÊN, bỏ trùng, bỏ chính truyện
     * đang xem, dừng khi đủ limit.
     *
     * Nguồn đứng trước thắng: một truyện vừa "độc giả cũng đọc" vừa "cùng thể
     * loại" chỉ hiện một lần, với lý do mạnh hơn (hành vi thật của người đọc).
     *
     * Hàm thuần — không đụng CSDL — nên test được trực tiếp.
     */
    public static RecommendationSet merge(int excludeId, int limit,
                                          List<List<Story>> sources, List<String> sourceReasons) {
        RecommendationSet out = new RecommendationSet();
        if (limit <= 0 || sources == null) return out;

        for (int i = 0; i < sources.size() && out.size() < limit; i++) {
            List<Story> src = sources.get(i);
            if (src == null) continue;
            String reason = (sourceReasons != null && i < sourceReasons.size())
                    ? sourceReasons.get(i) : null;

            for (Story s : src) {
                if (out.size() >= limit) break;
                if (s == null || s.getId() == excludeId) continue;
                if (out.reasons.containsKey(s.getId())) continue;
                out.stories.add(s);
                out.reasons.put(s.getId(), reason);
            }
        }
        return out;
    }

    /** Uy tín tăng tối đa 10%; giữ khác biệt giữa các nguồn phù hợp và dành chỗ khám phá. */
    public static RecommendationSet ranked(int excludeId, int limit, List<List<Story>> sources,
            List<String> sourceReasons, Map<Integer, Integer> boosts, List<Story> newcomers) {
        RecommendationSet out = new RecommendationSet();
        if (limit <= 0) return out;
        Map<Integer, Story> candidates = new LinkedHashMap<>();
        Map<Integer, Double> scores = new HashMap<>();
        Map<Integer, String> labels = new HashMap<>();
        double[] baseScores = { 100, 90, 65 };
        for (int i = 0; i < sources.size(); i++) {
            List<Story> source = sources.get(i);
            if (source == null) continue;
            for (int j = 0; j < source.size(); j++) {
                Story story = source.get(j);
                if (!eligible(story, excludeId) || candidates.containsKey(story.getId())) continue;
                int boost = Math.max(0, Math.min(AchievementPolicy.MAX_RECOMMENDATION_BOOST,
                        boosts.getOrDefault(story.getAuthorId(), 0)));
                double base = (i < baseScores.length ? baseScores[i] : 60) - Math.min(8, j * 0.5);
                candidates.put(story.getId(), story);
                scores.put(story.getId(), base * (1 + boost / 100.0));
                labels.put(story.getId(), sourceReasons.get(i));
            }
        }
        List<Story> sorted = new ArrayList<>(candidates.values());
        sorted.sort(Comparator.<Story>comparingDouble(s -> scores.get(s.getId())).reversed()
                .thenComparingInt(Story::getId));
        Map<Integer, Integer> perAuthor = new HashMap<>();
        Story discovery = null;
        if (limit >= 4 && newcomers != null) for (Story story : newcomers) {
            if (eligible(story, excludeId)) { discovery = story; break; }
        }
        // Đặt trước một chỗ cho tác giả mới, nhưng hiển thị ở cuối khối.
        int regularLimit = discovery == null ? limit : limit - 1;
        if (discovery != null) perAuthor.put(discovery.getAuthorId(), 1);
        for (Story story : sorted) {
            if (out.size() >= regularLimit) break;
            if (discovery != null && discovery.getId() == story.getId()) continue;
            if (perAuthor.getOrDefault(story.getAuthorId(), 0) >= 2) continue;
            out.stories.add(story); out.reasons.put(story.getId(), labels.get(story.getId()));
            perAuthor.merge(story.getAuthorId(), 1, Integer::sum);
        }
        if (discovery != null) {
            out.stories.add(discovery); out.reasons.put(discovery.getId(), REASON_NEW_AUTHOR);
        }
        return out;
    }

    private static boolean eligible(Story story, int excludeId) {
        return story != null && story.getId() != excludeId && story.getChapterCount() > 0 && "PUBLISHED".equals(story.getStatus());
    }
}
