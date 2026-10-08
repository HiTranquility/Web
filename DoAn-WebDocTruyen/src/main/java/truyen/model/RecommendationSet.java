package truyen.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
}
