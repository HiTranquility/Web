package truyen.util;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import truyen.model.Bookmark;
import truyen.model.Chapter;
import truyen.model.Comment;
import truyen.model.Story;
import truyen.model.Tag;
import truyen.model.User;

/**
 * DỮ LIỆU GIẢ — dùng để DỰNG VÀ XEM GIAO DIỆN khi chưa có MySQL.
 *
 * TẦNG: util/ (không phải model, không phải dao)
 *
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │  VÌ SAO CÓ FILE NÀY                                                      │
 * │                                                                          │
 * │  Giai đoạn hiện tại của đồ án là LÀM GIAO DIỆN. Nhưng JSP chỉ là cái     │
 * │  khuôn — không có dữ liệu đổ vào thì mọi trang đều hiện "chưa có gì",    │
 * │  và không ai đánh giá được thiết kế.                                     │
 * │                                                                          │
 * │  Chờ cài MySQL + chạy sample_data.sql xong mới xem được giao diện là     │
 * │  ràng buộc sai thứ tự: giao diện không nên phụ thuộc cơ sở dữ liệu.      │
 * │                                                                          │
 * │  Nên file này đóng vai "cơ sở dữ liệu giả trong RAM". DAO sẽ tự lấy dữ   │
 * │  liệu ở đây KHI VÀ CHỈ KHI chưa cấu hình db.properties.                  │
 * └──────────────────────────────────────────────────────────────────────────┘
 *
 * KHI NÀO XOÁ FILE NÀY
 *   Khi chạy được scripts\setup-db.ps1 và web đọc dữ liệu thật. Lúc đó:
 *     1. Xoá file này
 *     2. Xoá mọi dòng "if (!DBConnection.isReady()) return DemoData..." trong dao/
 *   Không có chỗ nào khác trong dự án tham chiếu tới DemoData, nên xoá là sạch.
 *
 * LƯU Ý QUAN TRỌNG
 *   Đây KHÔNG phải cache, KHÔNG phải mock để test. Nó chỉ sống trong bộ nhớ,
 *   mọi thay đổi (đăng truyện, bình luận) sẽ mất khi tắt server. Đúng như vậy —
 *   giai đoạn này chỉ cần NHÌN, chưa cần LƯU.
 *
 *   Dữ liệu ở đây trùng khớp với database/sample_data.sql để lúc chuyển sang
 *   DB thật, giao diện trông y hệt, không bị "lệch pha".
 */
public final class DemoData {

    /** Lớp tiện ích thuần tĩnh — chặn khởi tạo. */
    private DemoData() { }

    /** Mốc thời gian cố định để mỗi lần khởi động không ra ngày khác nhau. */
    private static final LocalDateTime T0 = LocalDateTime.of(2026, 8, 1, 9, 0);

    // ========================================================================
    //  NGƯỜI DÙNG
    // ========================================================================

    private static User user(int id, String username, String name, String role,
                             String status, String bio) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setEmail(username + "@example.com");
        u.setDisplayName(name);
        u.setRole(role);
        u.setStatus(status);
        u.setBio(bio);
        u.setCreatedAt(T0.minusDays(120 - id * 7));

        // Mật khẩu: admin/admin123, còn lại 123456.
        //
        // VÌ SAO BĂM THẬT CHỨ KHÔNG MỞ CỬA HẬU
        //   Cách nhanh hơn là cho AuthServlet "ở chế độ giao diện thì cho qua
        //   hết". Nhưng như vậy là chèn một nhánh bỏ qua xác thực vào đúng chỗ
        //   nhạy cảm nhất của web — chỉ cần một lần quên xoá là lỗ hổng thật.
        //
        //   Dùng chuỗi băm thật thì luồng đăng nhập chạy y hệt lúc có database:
        //   vẫn qua PasswordUtil.verify(), vẫn so sánh chống timing attack.
        //   Không có dòng code nào trong tầng controller biết đây là dữ liệu giả.
        //
        //   Hai chuỗi dưới đây do chính PasswordUtil.hash() của dự án sinh ra
        //   và đã được verify() kiểm lại ngay sau khi sinh. Tuyệt đối không
        //   chép chuỗi băm từ nguồn ngoài — thuật toán khác nhau thì không
        //   tài khoản nào đăng nhập được.
        u.setPasswordHash("admin".equals(username)
            ? "pbkdf2$120000$Cofr6ZQoRKI5M8O6zKxCfg==$mybo64SgAvcGL7G+Br9nWYK1VEaOXZm9ZZjIQnVAJYs="
            : "pbkdf2$120000$EAHYyRfFrnOjfMeajaEUcQ==$srYc9I6BBvbESS3cB0DCqpfEDXS38iHGKPvHte2fMzk=");
        return u;
    }

    public static List<User> users() {
        return new ArrayList<>(Arrays.asList(
            user(1, "admin", "Quản trị viên", "ADMIN", "ACTIVE",
                 "Tài khoản quản trị của Web Đọc Truyện."),
            user(2, "mocmien", "Mộc Miên", "USER", "ACTIVE",
                 "Viết truyện ngôn tình và chút đời thường. Ba tác phẩm, đều viết chậm mà chắc."),
            user(3, "haiduong", "Hải Dương", "USER", "ACTIVE",
                 "Thích viết trinh thám. Mỗi chương là một mảnh ghép."),
            user(4, "kiemvu", "Kiếm Vũ", "USER", "ACTIVE",
                 "Kiếm hiệp, tiên hiệp. Ra chương đều đặn mỗi tuần."),
            user(5, "thuytien", "Thuỷ Tiên", "USER", "ACTIVE",
                 "Độc giả. Đọc nhiều hơn viết."),
            user(6, "spammer", "Tài khoản vi phạm", "USER", "BANNED",
                 "Tài khoản này đã bị khoá.")
        ));
    }

    public static User user(int id) {
        for (User u : users()) {
            if (u.getId() == id) return u;
        }
        return null;
    }

    // ========================================================================
    //  THỂ LOẠI
    // ========================================================================

    private static Tag tag(int id, String name, String slug, int count) {
        Tag t = new Tag();
        t.setId(id);
        t.setName(name);
        t.setSlug(slug);
        t.setStoryCount(count);
        return t;
    }

    public static List<Tag> tags() {
        return new ArrayList<>(Arrays.asList(
            tag(1,  "Ngôn tình",   "ngon-tinh",   3),
            tag(2,  "Trinh thám",  "trinh-tham",  2),
            tag(3,  "Kiếm hiệp",   "kiem-hiep",   2),
            tag(4,  "Tiên hiệp",   "tien-hiep",   1),
            tag(5,  "Đời thường",  "doi-thuong",  2),
            tag(6,  "Kinh dị",     "kinh-di",     1),
            tag(7,  "Hài hước",    "hai-huoc",    1),
            tag(8,  "Học đường",   "hoc-duong",   2),
            tag(9,  "Xuyên không", "xuyen-khong", 1),
            tag(10, "Hoàn thành",  "hoan-thanh",  3)
        ));
    }

    public static Tag tagBySlug(String slug) {
        for (Tag t : tags()) {
            if (t.getSlug().equals(slug)) return t;
        }
        return null;
    }

    private static List<Tag> tagsOf(int... ids) {
        List<Tag> all = tags();
        List<Tag> out = new ArrayList<>();
        for (int id : ids) {
            for (Tag t : all) {
                if (t.getId() == id) out.add(t);
            }
        }
        return out;
    }

    // ========================================================================
    //  TRUYỆN
    // ========================================================================

    private static Story story(int id, String title, String slug, int authorId,
                               String authorName, String progress, int views,
                               int chapters, int daysAgo, String desc, int... tagIds) {
        Story s = new Story();
        s.setId(id);
        s.setTitle(title);
        s.setSlug(slug);
        s.setAuthorId(authorId);
        s.setAuthorName(authorName);
        s.setStatus("PUBLISHED");
        s.setProgress(progress);
        s.setViewCount(views);
        s.setChapterCount(chapters);
        s.setDescription(desc);
        s.setCreatedAt(T0.minusDays(daysAgo));
        s.setUpdatedAt(T0.minusDays(daysAgo / 3));
        s.setTags(tagsOf(tagIds));
        // Không đặt coverUrl: thẻ truyện sẽ rơi vào nhánh chữ cái đầu. Đúng
        // trường hợp thật nhất — phần lớn truyện mới đăng đều chưa có ảnh bìa.
        return s;
    }

    /** 8 truyện đã xuất bản, thứ tự trong danh sách là thứ tự id. */
    public static List<Story> stories() {
        return new ArrayList<>(Arrays.asList(
            story(1, "Mùa hạ năm ấy", "mua-ha-nam-ay", 2, "Mộc Miên",
                  "COMPLETED", 15420, 24, 210,
                  "Một mùa hè cuối cấp, một lời hứa chưa kịp nói. Chuyện của hai người "
                  + "trẻ lớn lên ở thị trấn ven biển, nơi mọi thứ đều chậm trừ thời gian.",
                  1, 8, 10),

            story(2, "Người ở lại", "nguoi-o-lai", 2, "Mộc Miên",
                  "ONGOING", 8730, 12, 95,
                  "Sau đám tang của mẹ, cô trở về căn nhà cũ và phát hiện những lá thư "
                  + "chưa từng được gửi đi.",
                  1, 5),

            story(3, "Cà phê tầng bốn", "ca-phe-tang-bon", 2, "Mộc Miên",
                  "ONGOING", 4210, 9, 40,
                  "Quán cà phê nằm trên tầng bốn không thang máy. Ít khách, nhưng ai lên "
                  + "được tới nơi đều có một câu chuyện.",
                  1, 5, 7),

            story(4, "Đêm không trăng", "dem-khong-trang", 3, "Hải Dương",
                  "ONGOING", 22150, 31, 180,
                  "Vụ án mạng trong căn hộ khoá trái từ bên trong. Không dấu vân tay, "
                  + "không dấu chân, chỉ có một chiếc đồng hồ dừng lúc 3 giờ 14.",
                  2, 6),

            story(5, "Mật mã sông Hàn", "mat-ma-song-han", 3, "Hải Dương",
                  "COMPLETED", 18900, 28, 260,
                  "Chuỗi ký hiệu lạ xuất hiện trên các cây cầu. Một nhà ngôn ngữ học về "
                  + "hưu là người duy nhất đọc được chúng.",
                  2, 10),

            story(6, "Kiếm khách vô danh", "kiem-khach-vo-danh", 4, "Kiếm Vũ",
                  "ONGOING", 31200, 45, 300,
                  "Hắn không có tên, không có môn phái, chỉ có một thanh kiếm gỉ và "
                  + "trí nhớ về một trận mưa.",
                  3),

            story(7, "Trấn yêu lục", "tran-yeu-luc", 4, "Kiếm Vũ",
                  "ONGOING", 12480, 18, 70,
                  "Ghi chép của một người canh giữ ranh giới giữa hai cõi, viết trong "
                  + "ba mươi năm không ai đọc.",
                  3, 4, 6),

            story(8, "Thư gửi mười năm sau", "thu-gui-muoi-nam-sau", 4, "Kiếm Vũ",
                  "COMPLETED", 9640, 15, 150,
                  "Lớp 12A viết thư cho chính mình mười năm sau. Mười năm trôi qua, "
                  + "chỉ hai mươi ba trong ba mươi tám lá thư có người tới nhận.",
                  5, 8, 9, 10)
        ));
    }

    /** Xếp hạng giả lập, cùng ba tiêu chí với StoryDAO.findTop(). */
    public static List<Story> top(String by, int limit) {
        List<Story> list = stories();
        if ("chapters".equals(by)) {
            list.sort((a, b) -> b.getChapterCount() - a.getChapterCount());
        } else if ("newest".equals(by)) {
            list.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        } else {
            list.sort((a, b) -> b.getViewCount() - a.getViewCount());
        }
        return slice(list, 0, limit);
    }

    public static User userByUsername(String username) {
        for (User u : users()) {
            if (u.getUsername().equals(username)) return u;
        }
        return null;
    }

    /** Chương liền trước (direction < 0) hoặc liền sau (direction > 0). */
    public static Chapter neighbour(int storyId, int chapterNo, int direction) {
        int want = chapterNo + (direction > 0 ? 1 : -1);
        for (Chapter c : chapters(storyId)) {
            if (c.getChapterNo() == want) return c;
        }
        return null;
    }

    /** Tổng lượt xem của một tác giả. */
    public static int viewsOfAuthor(int authorId) {
        int sum = 0;
        for (Story s : storiesByAuthor(authorId)) sum += s.getViewCount();
        return sum;
    }

    public static Story story(int id) {
        for (Story s : stories()) {
            if (s.getId() == id) return s;
        }
        return null;
    }

    /** Truyện của một tác giả. */
    public static List<Story> storiesByAuthor(int authorId) {
        List<Story> out = new ArrayList<>();
        for (Story s : stories()) {
            if (s.getAuthorId() == authorId) out.add(s);
        }
        return out;
    }

    /** Lọc theo thể loại + từ khoá, giống điều kiện WHERE của StoryDAO.findPage(). */
    public static List<Story> filter(String tagSlug, String keyword) {
        List<Story> out = new ArrayList<>();
        for (Story s : stories()) {
            if (tagSlug != null && !tagSlug.isEmpty()) {
                boolean hit = false;
                for (Tag t : s.getTags()) {
                    if (t.getSlug().equals(tagSlug)) { hit = true; break; }
                }
                if (!hit) continue;
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                String k = keyword.toLowerCase();
                if (!s.getTitle().toLowerCase().contains(k)
                        && !s.getDescription().toLowerCase().contains(k)) {
                    continue;
                }
            }
            out.add(s);
        }
        return out;
    }

    /** Cắt một trang từ danh sách, an toàn với chỉ số vượt biên. */
    public static <T> List<T> slice(List<T> src, int offset, int limit) {
        if (offset >= src.size()) return new ArrayList<>();
        int to = Math.min(offset + limit, src.size());
        return new ArrayList<>(src.subList(Math.max(0, offset), to));
    }

    // ========================================================================
    //  CHƯƠNG
    // ========================================================================

    /** Vài đoạn văn để chương đọc thử có độ dài thật, kiểm tra được typography. */
    private static final String LOREM =
        "Buổi chiều hôm đó trời không mưa, nhưng mây kéo thấp tới mức tưởng như "
        + "có thể chạm tay vào. Cô đứng ở đầu ngõ, tay còn cầm chiếc cặp sách đã "
        + "sờn quai, nhìn con đường quen thuộc mà thấy lạ.\n\n"
        + "\"Về rồi à?\" — tiếng bà hàng nước vọng ra từ sau tấm bạt xanh. Cô gật "
        + "đầu, không biết trả lời gì thêm. Mười năm là quãng đủ dài để một con "
        + "ngõ đổi tên, nhưng chưa đủ để người ta quên một khuôn mặt.\n\n"
        + "Căn nhà cuối ngõ vẫn còn đó. Sơn tường bong từng mảng, để lộ lớp vôi "
        + "trắng bên dưới, thứ màu mà cô nhớ rõ hơn cả màu sơn hiện tại. Cánh cổng "
        + "sắt kêu lên một tiếng dài khi cô đẩy vào, đúng cái tiếng của mười năm "
        + "trước, không hơn không kém một nốt nào.\n\n"
        + "Trên bàn, chồng thư vẫn nằm nguyên chỗ mẹ để. Không ai động vào. Cô kéo "
        + "ghế ngồi xuống, và lần đầu tiên kể từ hôm đám tang, cô thấy mình có đủ "
        + "sức để mở phong bì đầu tiên.\n\n"
        + "Nét chữ trong đó là của mẹ. Nhưng câu đầu tiên lại bắt đầu bằng tên cô.";

    private static Chapter chapter(int id, int storyId, String storyTitle,
                                   int no, String title) {
        Chapter c = new Chapter();
        c.setId(id);
        c.setStoryId(storyId);
        c.setStoryTitle(storyTitle);
        c.setChapterNo(no);
        c.setTitle(title);
        c.setContent(LOREM);
        c.setCreatedAt(T0.minusDays(60 - no));
        c.setUpdatedAt(T0.minusDays(60 - no));
        return c;
    }

    private static final String[] CHAPTER_TITLES = {
        "Ngõ cũ", "Người quen mặt", "Bức thư thứ nhất", "Chiều không mưa",
        "Chuyện chưa kể", "Cánh cổng sắt", "Tháng bảy", "Điều bỏ lại",
        "Đêm dài nhất", "Lối rẽ", "Trở về", "Kết"
    };

    /**
     * Danh sách chương của một truyện.
     *
     * Id chương sinh theo công thức storyId*100 + số chương, để id không đụng
     * nhau giữa các truyện mà vẫn suy ngược ra được truyện cha khi debug.
     */
    public static List<Chapter> chapters(int storyId) {
        Story s = story(storyId);
        if (s == null) return new ArrayList<>();

        List<Chapter> out = new ArrayList<>();
        // Chỉ dựng tối đa 12 chương — đủ để thấy danh sách cuộn và phân trang,
        // không cần dựng đủ 45 chương như số hiển thị trên thẻ truyện.
        int n = Math.min(s.getChapterCount(), CHAPTER_TITLES.length);
        for (int i = 1; i <= n; i++) {
            out.add(chapter(storyId * 100 + i, storyId, s.getTitle(),
                            i, CHAPTER_TITLES[i - 1]));
        }
        return out;
    }

    public static Chapter chapter(int id) {
        for (Chapter c : chapters(id / 100)) {
            if (c.getId() == id) return c;
        }
        return null;
    }

    // ========================================================================
    //  BÌNH LUẬN
    // ========================================================================

    private static Comment comment(int id, int storyId, int userId, String user,
                                   String name, int hoursAgo, String content) {
        Comment c = new Comment();
        c.setId(id);
        c.setStoryId(storyId);
        c.setUserId(userId);
        c.setUsername(user);
        c.setDisplayName(name);
        c.setContent(content);
        c.setStatus("VISIBLE");
        c.setCreatedAt(T0.minusHours(hoursAgo));
        return c;
    }

    public static List<Comment> comments(int storyId) {
        List<Comment> all = new ArrayList<>(Arrays.asList(
            comment(1, 1, 5, "thuytien", "Thuỷ Tiên", 3,
                    "Chương cuối đọc xong ngồi im một lúc. Cảm ơn tác giả."),
            comment(2, 1, 3, "haiduong", "Hải Dương", 20,
                    "Cách tả cảnh biển rất có không khí. Học được nhiều."),
            comment(3, 1, 2, "mocmien", "Mộc Miên", 18,
                    "Cảm ơn mọi người đã đọc tới cuối. Truyện sau mình sẽ viết nhanh hơn."),
            comment(4, 4, 5, "thuytien", "Thuỷ Tiên", 8,
                    "Chi tiết chiếc đồng hồ dừng lúc 3h14 là manh mối phải không ạ?"),
            comment(5, 4, 3, "haiduong", "Hải Dương", 6,
                    "Bạn đọc kỹ đó. Nhưng chưa phải manh mối chính đâu."),
            comment(6, 6, 5, "thuytien", "Thuỷ Tiên", 30,
                    "Ra chương đều thật sự đáng quý. Mong tác giả giữ sức."),
            comment(7, 6, 4, "kiemvu", "Kiếm Vũ", 26,
                    "Mình cố gắng mỗi tuần một chương. Cảm ơn bạn.")
        ));

        List<Comment> out = new ArrayList<>();
        for (Comment c : all) {
            if (c.getStoryId() == storyId) out.add(c);
        }
        return out;
    }

    // ========================================================================
    //  TỦ TRUYỆN (BOOKMARK)
    // ========================================================================

    private static Bookmark bookmark(int userId, int storyId, int lastNo) {
        Story s = story(storyId);
        Bookmark b = new Bookmark();
        b.setUserId(userId);
        b.setStoryId(storyId);
        b.setStoryTitle(s.getTitle());
        b.setStorySlug(s.getSlug());
        b.setTotalChapters(s.getChapterCount());
        b.setLastChapterNo(lastNo);
        b.setLastChapterId(lastNo == 0 ? 0 : storyId * 100 + lastNo);
        b.setCreatedAt(T0.minusDays(storyId));
        return b;
    }

    public static List<Bookmark> bookmarks(int userId) {
        // Ai cũng thấy cùng một tủ truyện mẫu — giai đoạn dựng giao diện thì
        // điều cần thấy là bố cục của thẻ, không phải quyền sở hữu.
        return new ArrayList<>(Arrays.asList(
            bookmark(userId, 1, 24),  // đọc xong
            bookmark(userId, 4, 12),  // đang đọc dở
            bookmark(userId, 6, 3),   // mới bắt đầu
            bookmark(userId, 8, 0)    // lưu mà chưa đọc
        ));
    }

    public static boolean bookmarked(int storyId) {
        return storyId == 1 || storyId == 4 || storyId == 6 || storyId == 8;
    }
}
