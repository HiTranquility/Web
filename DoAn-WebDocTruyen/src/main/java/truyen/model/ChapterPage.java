package truyen.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Một trang ảnh của chương truyện tranh (bảng chapter_pages). */
public class ChapterPage implements Serializable {

    private int id;
    private int chapterId;
    private int pageNo;
    private String imagePath;   // tương đối trong uploads: pages/1/12/abc.jpg
    private int width;          // 0 = không đọc được kích thước
    private int height;

    public ChapterPage() { }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getChapterId() { return chapterId; }
    public void setChapterId(int chapterId) { this.chapterId = chapterId; }

    public int getPageNo() { return pageNo; }
    public void setPageNo(int pageNo) { this.pageNo = pageNo; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    /** Đường dẫn để JSP ghép sau contextPath: ${ctx}${page.url}. */
    public String getUrl() { return "/uploads/" + imagePath; }

    /** Có kích thước thật không — JSP chỉ in width/height khi biết chắc. */
    public boolean isSized() { return width > 0 && height > 0; }

    /**
     * Xếp thứ tự trang CUỐI CÙNG của một chương sau khi tác giả sắp xếp lại,
     * bỏ bớt trang cũ và thêm trang mới.
     *
     * @param existing  các trang đang có, thứ tự cũ
     * @param added     các trang VỪA TẢI LÊN, theo thứ tự file trong form (id = 0)
     * @param tokens    thứ tự mới form gửi lên: "p{id}" = trang cũ, "n{chỉ số}" =
     *                  trang mới thứ mấy trong added. Có thể thiếu, thừa, lặp,
     *                  hoặc chứa id của chương KHÁC — đều phải chịu được.
     * @param removeIds id các trang cũ bị bỏ
     * @return thứ tự cuối cùng. Trang nào form "quên" nhắc tới thì GIỮ LẠI và
     *         xếp cuối theo thứ tự gốc (cũ trước, mới sau) — mất một trang vì
     *         form gửi thiếu là lỗi tệ hơn nhiều so với một trang lệch chỗ.
     *
     * Hàm thuần, không đụng CSDL — test được trực tiếp.
     */
    public static List<ChapterPage> arrange(List<ChapterPage> existing, List<ChapterPage> added,
                                            List<String> tokens, Collection<Integer> removeIds) {
        Map<Integer, ChapterPage> old = new LinkedHashMap<>();
        for (ChapterPage p : existing) old.put(p.getId(), p);
        Map<Integer, ChapterPage> fresh = new LinkedHashMap<>();
        for (int i = 0; i < added.size(); i++) fresh.put(i, added.get(i));

        List<ChapterPage> out = new ArrayList<>();
        if (tokens != null) {
            for (String raw : tokens) {
                String t = raw == null ? "" : raw.trim();
                if (t.length() < 2) continue;
                int n;
                try { n = Integer.parseInt(t.substring(1)); } catch (NumberFormatException e) { continue; }
                ChapterPage p = t.charAt(0) == 'p' ? old.remove(n)          // remove: chặn token lặp
                              : t.charAt(0) == 'n' ? fresh.remove(n) : null;
                if (p != null && !(p.getId() > 0 && removeIds != null && removeIds.contains(p.getId()))) {
                    out.add(p);
                }
            }
        }
        for (ChapterPage p : old.values()) {             // trang cũ form không nhắc tới
            if (removeIds == null || !removeIds.contains(p.getId())) out.add(p);
        }
        out.addAll(fresh.values());                      // trang mới form không nhắc tới
        return out;
    }
}
