package truyen.model;

import java.io.Serializable;

/**
 * Một thể loại / nhãn: "Tiên hiệp", "Ngôn tình", "Trinh thám"...
 *
 * slug là bản thân thiện URL của name ("Tiên hiệp" -> "tien-hiep"), dùng cho
 * đường dẫn lọc truyện: /story?action=list&tag=tien-hiep
 * Không nhét tên có dấu và dấu cách vào URL — vừa xấu vừa phải mã hoá.
 */
public class Tag implements Serializable {

    private int id;
    private String name;
    private String slug;

    // Số truyện đang mang tag này. Chỉ có giá trị khi truy vấn cố ý đếm,
    // dùng để hiện "Tiên hiệp (24)" ở bộ lọc.
    private int storyCount;

    /**
     * Tổng lượt xem của mọi truyện thuộc thể loại này.
     *
     * KHÔNG phải cột trong bảng tags — chỉ câu truy vấn của bảng điều khiển
     * cộng sang. Các truy vấn khác để nguyên 0.
     *
     * Có trường riêng chứ không mượn storyCount: hai con số khác hẳn nhau
     * về ý nghĩa, mượn lẫn nhau là kiểu bug im lặng khó tìm nhất.
     */
    private int viewCount;

    public Tag() { }

    public Tag(int id, String name, String slug) {
        this.id = id;
        this.name = name;
        this.slug = slug;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public int getStoryCount() { return storyCount; }
    public void setStoryCount(int storyCount) { this.storyCount = storyCount; }

    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }
}
