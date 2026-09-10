package truyen.util;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import truyen.model.Bookmark;
import truyen.model.Follow;
import truyen.model.Notification;
import truyen.model.Report;
import truyen.model.Chapter;
import truyen.model.Comment;
import truyen.model.ReadHistory;
import truyen.model.Story;
import truyen.model.Tag;
import truyen.model.User;

/** DỮ LIỆU GIẢ — dùng để DỰNG VÀ XEM GIAO DIỆN khi chưa có MySQL. */
public final class DemoData {

    /** Lớp tiện ích thuần tĩnh — chặn khởi tạo. */
    private DemoData() { }

    /** Mốc thời gian cố định để mỗi lần khởi động không ra ngày khác nhau. */
    /** Thể loại xếp theo lượt xem — bản giả lập. */
    public static List<Tag> tagsByViews(int limit) {
        List<Tag> out = tags();
        for (Tag t : out) {
            int sum = 0;
            for (Story s : stories()) {
                for (Tag x : s.getTags()) {
                    if (x.getId() == t.getId()) sum += s.getViewCount();
                }
            }
            t.setViewCount(sum);
        }
        out.sort((a, b) -> b.getViewCount() - a.getViewCount());
        return slice(out, 0, limit);
    }

    /** Mốc thời gian. */
    private static final LocalDateTime T0 = LocalDateTime.now();

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
        List<User> list = new ArrayList<>(Arrays.asList(
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

        // storyCount không phải cột trong bảng users — trang quản trị đếm
        // sang. Ở đây đếm từ danh sách truyện giả cho khớp.
        for (User u : list) {
            u.setStoryCount(storiesByAuthor(u.getId()).size());
        }
        return list;
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

        /* Điểm đánh giá giả. */
        if (id != 3) {
            int count = 8 + id * 5;
            int avgTimes10 = 35 + (id * 3) % 15;      // 3.5 .. 4.9
            s.setRatingCount(count);
            s.setRatingSum(Math.round(count * avgTimes10 / 10f));
        }

        // Diem danh gia gia lap: suy tu luot xem cho co ve that — truyen doc
        // nhieu thi thuong cung nhieu nguoi cham. Cong thuc khong co y nghia
        // gi ngoai viec sinh ra so khac nhau moi truyen.
        int count = Math.max(3, views / 900);
        s.setRatingCount(count);
        s.setRatingSum(count * 4 + (id % 3));
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
                  "ONGOING", 4210, 9, 5,
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
                  "ONGOING", 12480, 18, 11,
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
        } else if ("rating".equals(by)) {
            // Cung nguong 3 luot nhu StoryDAO.findTop, de hai che do xem
            // ra cung thu tu.
            list.sort((a, b) -> {
                boolean qa = a.getRatingCount() >= 3, qb = b.getRatingCount() >= 3;
                if (qa != qb) return qb ? 1 : -1;
                return Double.compare(b.getRatingAvg(), a.getRatingAvg());
            });
        } else {
            list.sort((a, b) -> b.getViewCount() - a.getViewCount());
        }
        return slice(list, 0, limit);
    }

    /**
     * Xếp hạng theo giai đoạn — bản giả lập của StoryDAO.findTopByPeriod().
     */
    public static List<Story> topByPeriod(int days, int limit) {
        List<Story> list = stories();
        list.sort((a, b) -> recentHits(b.getId(), days) - recentHits(a.getId(), days));

        // Truyện không có lượt nào trong giai đoạn thì không lên bảng —
        // giống hệt câu SQL thật, vì nó JOIN với view_logs.
        List<Story> out = new ArrayList<>();
        for (Story s : list) {
            if (recentHits(s.getId(), days) > 0) out.add(s);
        }
        return slice(out, 0, limit);
    }

    /** Số lượt xem giả lập trong `days` ngày gần nhất. */
    private static int recentHits(int storyId, int days) {
        // Trộn id với một số nguyên tố để thứ tự khác hẳn thứ tự view_count.
        int base = (storyId * 37 + 11) % 23;
        return base * days / 7;
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

    /**
     * Lọc theo thể loại + từ khoá + tình trạng.
     *
     * Giữ ĐÚNG cùng điều kiện với mệnh đề WHERE của StoryDAO.findPage(). Lệch
     * nhau thì giao diện xem lúc chưa có MySQL sẽ khác lúc có — và bug đó rất
     * khó tìm vì ai cũng tưởng mình đang nhìn cùng một thứ.
     */
    public static List<Story> filter(String tagSlug, String keyword, String progress) {
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
                // Tìm cả tên tác giả, giống câu SQL thật.
                if (!s.getTitle().toLowerCase().contains(k)
                        && !s.getAuthorName().toLowerCase().contains(k)
                        && !s.getDescription().toLowerCase().contains(k)) {
                    continue;
                }
            }
            if ("ongoing".equals(progress) && !"ONGOING".equals(s.getProgress())) continue;
            if ("completed".equals(progress) && !"COMPLETED".equals(s.getProgress())) continue;
            out.add(s);
        }
        return out;
    }

    /** Sắp xếp, khớp với các nhánh ORDER BY của StoryDAO.findPage(). */
    public static List<Story> sort(List<Story> list, String by) {
        if ("popular".equals(by)) {
            list.sort((a, b) -> b.getViewCount() - a.getViewCount());
        } else if ("rating".equals(by)) {
            list.sort((a, b) -> Double.compare(b.getRatingAvg(), a.getRatingAvg()));
        } else {
            list.sort((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()));
        }
        return list;
    }

    /** Truyện tương tự — đếm số thể loại trùng, giống StoryDAO.findSimilar. */
    public static List<Story> similar(int storyId, int limit) {
        Story me = story(storyId);
        if (me == null) return new ArrayList<>();

        List<Story> out = new ArrayList<>();
        for (Story s : stories()) {
            if (s.getId() == storyId) continue;
            int shared = 0;
            for (Tag a : me.getTags()) {
                for (Tag b : s.getTags()) {
                    if (a.getId() == b.getId()) shared++;
                }
            }
            if (shared > 0) out.add(s);
        }
        // Nhiều thể loại trùng trước; hoà thì truyện đọc nhiều hơn trước.
        out.sort((a, b) -> {
            int sa = sharedTags(me, a), sb = sharedTags(me, b);
            if (sa != sb) return sb - sa;
            return b.getViewCount() - a.getViewCount();
        });
        return slice(out, 0, limit);
    }

    private static int sharedTags(Story x, Story y) {
        int n = 0;
        for (Tag a : x.getTags()) {
            for (Tag b : y.getTags()) {
                if (a.getId() == b.getId()) n++;
            }
        }
        return n;
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

    /**
     * Tìm trong nội dung chương — bản giả lập.
     *
     * Mọi chương giả đều dùng chung một đoạn văn LOREM, nên tìm "ngõ" sẽ ra
     * rất nhiều kết quả. Đó là hạn chế của dữ liệu giả, không phải của tính
     * năng: với CSDL thật mỗi chương một nội dung riêng.
     */
    public static List<Chapter> searchChapters(String keyword, int limit) {
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        List<Chapter> out = new ArrayList<>();
        if (kw.isEmpty()) return out;

        for (Story s : stories()) {
            for (Chapter c : chapters(s.getId())) {
                String hay = (c.getTitle() + " " + c.getContent()).toLowerCase();
                if (!hay.contains(kw)) continue;

                // Cắt đoạn trích y như ChapterDAO.snippet() để hai chế độ
                // hiển thị giống hệt nhau.
                String flat = c.getContent().replace('\n', ' ');
                int at = flat.toLowerCase().indexOf(kw);
                if (at >= 0) {
                    int from = Math.max(0, at - 60);
                    int to = Math.min(flat.length(), at + kw.length() + 60);
                    String cut = flat.substring(from, to);
                    if (from > 0) cut = "…" + cut;
                    if (to < flat.length()) cut = cut + "…";
                    c.setContent(cut);
                } else {
                    c.setContent(flat.substring(0, Math.min(120, flat.length())) + "…");
                }
                out.add(c);
                if (out.size() >= limit) return out;
            }
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
        return comment(id, storyId, userId, user, name, hoursAgo, content, null);
    }

    /** Bản có parentId — dùng cho các trả lời. */
    private static Comment comment(int id, int storyId, int userId, String user,
                                   String name, int hoursAgo, String content,
                                   Integer parentId) {
        Comment c = new Comment();
        c.setId(id);
        c.setStoryId(storyId);
        c.setUserId(userId);
        c.setUsername(user);
        c.setDisplayName(name);
        c.setContent(content);
        c.setStatus("VISIBLE");
        c.setCreatedAt(T0.minusHours(hoursAgo));
        c.setParentId(parentId);
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
                    "Mình cố gắng mỗi tuần một chương. Cảm ơn bạn."),

            // ---- Trả lời (parentId trỏ về bình luận gốc) ----
            comment(101, 1, 2, "mocmien", "Mộc Miên", 2,
                    "Cảm ơn bạn đã đọc tới cuối. Truyện sau mình viết nhanh hơn.", 1),
            comment(102, 1, 4, "kiemvu", "Kiếm Vũ", 1,
                    "Mình cũng vừa đọc xong. Chương 20 là chương hay nhất.", 1),
            comment(103, 4, 3, "haiduong", "Hải Dương", 5,
                    "Bạn đọc kỹ đó. Nhưng chưa phải manh mối chính đâu.", 4),
            comment(104, 4, 5, "thuytien", "Thuỷ Tiên", 4,
                    "Vậy là còn thứ khác nữa à? Hồi hộp quá.", 4),
            comment(105, 6, 4, "kiemvu", "Kiếm Vũ", 25,
                    "Cảm ơn bạn. Tuần này có thể ra hai chương.", 6)
        ));

        // Lọc theo truyện rồi XẾP CÂY — làm y hệt CommentDAO.buildTree() để
        // giao diện lúc chạy dữ liệu giả và lúc chạy dữ liệu thật giống nhau.
        List<Comment> mine = new ArrayList<>();
        for (Comment c : all) {
            if (c.getStoryId() == storyId) mine.add(c);
        }

        java.util.Map<Integer, Comment> roots = new java.util.LinkedHashMap<>();
        for (Comment c : mine) {
            if (!c.isReply()) roots.put(c.getId(), c);
        }
        for (Comment c : mine) {
            if (c.isReply()) {
                Comment parent = roots.get(c.getParentId());
                if (parent != null) parent.getReplies().add(c);
            }
        }
        return new ArrayList<>(roots.values());
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

    // ========================================================================
    //  LỊCH SỬ ĐỌC — TRANG 31
    // ========================================================================

    /**
     * Lịch sử mẫu. Cố ý xếp CẢ HAI loại để thấy đủ hai kiểu hiển thị:
     *   - truyện 4, 1  đã lưu  -> có vị trí đọc, nút "Đọc tiếp"
     *   - truyện 2, 5  chưa lưu -> không có vị trí, nút "Mở lại"
     * Loại thứ hai chính là lý do trang này tồn tại: bookmark không giữ nó.
     */
    public static List<ReadHistory> history(int userId, int limit) {
        List<ReadHistory> list = new ArrayList<>(Arrays.asList(
            hist(4, 0,  12, 31),   // hôm nay, đang đọc dở
            hist(2, 1,   0,  3),   // hôm qua, ngó qua rồi thôi
            hist(1, 2,  24,  9),   // 2 ngày trước, đã đọc xong
            hist(5, 6,   0,  1),   // 6 ngày trước
            hist(6, 20,  3,  4)    // hơn tuần -> hiện ngày/tháng
        ));
        return slice(list, 0, limit);
    }

    private static ReadHistory hist(int storyId, int daysAgo, int lastNo, int times) {
        Story s = story(storyId);
        ReadHistory h = new ReadHistory();
        h.setStoryId(storyId);
        h.setStoryTitle(s.getTitle());
        h.setCoverUrl(s.getCoverUrl());
        h.setAuthorName(s.getAuthorName());
        h.setTotalChapters(s.getChapterCount());

        /* Mốc thời gian tính từ HÔM NAY, không phải từ T0 cố định — nhãn
           "Hôm nay"/"Hôm qua" phải đúng vào mọi ngày mở dự án ra xem. */
        h.setLastViewed(LocalDateTime.now().minusDays(daysAgo).minusHours(2));
        h.setViewTimes(times);
        h.setLastChapterNo(lastNo);
        h.setLastChapterId(lastNo == 0 ? 0 : storyId * 100 + lastNo);
        return h;
    }

    // ========================================================================
    //  ĐÁNH GIÁ SAO
    // ========================================================================

    /** Điểm "tôi" đã chấm. Cho vài truyện có sẵn để thấy trạng thái đã chấm. */
    public static int myRating(int userId, int storyId) {
        if (storyId == 1) return 5;
        if (storyId == 4) return 4;
        return 0;
    }

    // ========================================================================
    //  THEO DÕI TÁC GIẢ
    // ========================================================================

    /**
     * Những người đã bấm vào trang thông báo trong LẦN CHẠY NÀY.
     *
     * VÌ SAO CẦN
     *   markAllRead() là lệnh GHI, mà mọi lệnh ghi ở chế độ xem giao diện đều
     *   bị bỏ qua. Hậu quả: đọc thông báo xong chấm đỏ vẫn còn nguyên, và
     *   người xem sẽ kết luận là tính năng hỏng — trong khi với CSDL thật nó
     *   chạy đúng.
     *
     *   Dữ liệu giả mà mô tả sai hành vi thật còn tệ hơn không có dữ liệu.
     *
     * Set tĩnh, sống trong bộ nhớ, mất khi tắt server — đúng như mọi thứ khác
     * trong file này.
     */
    private static final java.util.Set<Integer> READ = new java.util.HashSet<>();

    /** Đánh dấu đã đọc — bản giả lập của NotificationDAO.markAllRead(). */
    public static void markRead(int userId) {
        READ.add(userId);
    }

    /** Coi như đang theo dõi Mộc Miên (2) và Kiếm Vũ (4). */
    public static boolean following(int authorId) {
        return authorId == 2 || authorId == 4;
    }

    public static int followerCount(int authorId) {
        // Số cố định theo id để mỗi tác giả một con số, không đổi giữa các lần tải.
        return 12 + authorId * 7;
    }

    public static List<Follow> followingList(int followerId) {
        List<Follow> out = new ArrayList<>();
        for (int id : new int[]{2, 4}) {
            User u = user(id);
            Follow f = new Follow();
            f.setFollowerId(followerId);
            f.setAuthorId(id);
            f.setAuthorName(u.getName());
            f.setAuthorUsername(u.getUsername());
            f.setAuthorStoryCount(storiesByAuthor(id).size());
            f.setCreatedAt(T0.minusDays(id * 5));
            out.add(f);
        }
        return out;
    }

    // ========================================================================
    //  THÔNG BÁO
    // ========================================================================

    private static Notification notif(int id, int userId, int storyId, int chapNo,
                                      boolean read, int hoursAgo) {
        Story s = story(storyId);
        Notification n = new Notification();
        n.setId(id);
        n.setUserId(userId);
        n.setStoryId(storyId);
        n.setChapterId(storyId * 100 + chapNo);
        n.setType("NEW_CHAPTER");
        n.setMessage(s.getAuthorName() + " vừa đăng chương " + chapNo
                   + " của \"" + s.getTitle() + "\"");
        n.setRead(read);
        n.setCreatedAt(T0.minusHours(hoursAgo));
        return n;
    }

    public static List<Notification> notifications(int userId) {
        boolean seen = READ.contains(userId);
        List<Notification> all = new ArrayList<>(Arrays.asList(
            notif(1, userId, 3, 9,  false, 2),
            notif(2, userId, 7, 8,  false, 26),
            notif(3, userId, 2, 12, true,  50),
            notif(4, userId, 6, 11, true,  96)
        ));
        if (seen) {
            for (Notification n : all) n.setRead(true);
        }
        return all;
    }

    public static int unreadCount(int userId) {
        int n = 0;
        for (Notification x : notifications(userId)) {
            if (!x.isRead()) n++;
        }
        return n;
    }

    // ========================================================================
    //  BÁO CÁO VI PHẠM
    // ========================================================================

    private static Report report(int id, int reporterId, String type, int targetId,
                                 String reason, String status, int hoursAgo,
                                 String title) {
        Report r = new Report();
        r.setId(id);
        r.setReporterId(reporterId);
        r.setReporterName(user(reporterId).getName());
        r.setTargetType(type);
        r.setTargetId(targetId);
        r.setTargetTitle(title);
        r.setReason(reason);
        r.setStatus(status);
        r.setCreatedAt(T0.minusHours(hoursAgo));
        if (!"PENDING".equals(status)) {
            r.setHandledAt(T0.minusHours(hoursAgo - 1));
        }
        return r;
    }

    public static List<Report> reports(String status) {
        List<Report> all = new ArrayList<>(Arrays.asList(
            report(1, 5, "COMMENT", 99, "Bình luận chứa đường dẫn quảng cáo.",
                   "PENDING", 4, "Mua ngay tại shop... link rút gọn"),
            report(2, 3, "STORY", 7, "Nội dung chương 5 chưa gắn cảnh báo phù hợp.",
                   "PENDING", 30, "Trấn yêu lục"),
            report(3, 5, "COMMENT", 98, "Xúc phạm người khác trong phần bình luận.",
                   "RESOLVED", 72, "câu bình luận đã bị ẩn"),
            report(4, 2, "STORY", 4, "Nghi ngờ đăng lại của người khác.",
                   "DISMISSED", 120, "Đêm không trăng")
        ));
        if (status == null || status.isEmpty()) return all;

        List<Report> out = new ArrayList<>();
        for (Report r : all) {
            if (status.equals(r.getStatus())) out.add(r);
        }
        return out;
    }

    // ========================================================================
    //  BÌNH LUẬN CHO TRANG QUẢN TRỊ
    // ========================================================================

    public static List<Comment> allComments(boolean onlyHidden) {
        List<Comment> out = new ArrayList<>();
        for (Story s : stories()) {
            for (Comment c : comments(s.getId())) {
                c.setStoryTitle(s.getTitle());
                // Cho một bình luận ở trạng thái đã ẩn để thấy được cả hai màu.
                if (c.getId() == 2) c.setStatus("HIDDEN");
                if (!onlyHidden || c.isHidden()) out.add(c);
            }
        }
        return out;
    }

    // ========================================================================
    //  THỐNG KÊ
    // ========================================================================

    /** [lượt xem, số người đánh dấu, số bình luận] cho một truyện. */
    public static int[] statsOf(int storyId) {
        Story s = story(storyId);
        if (s == null) return new int[]{0, 0, 0};
        return new int[]{
                s.getViewCount(),
                bookmarked(storyId) ? 4 : 1,
                comments(storyId).size()
        };
    }

    /**
     * [0] truyện · [1] chương · [2] tài khoản
     * [3] tổng lượt xem · [4] bình luận · [5] truyện đã gỡ
     *
     * Thứ tự này phải khớp CHÍNH XÁC với StoryDAO.adminOverview(), vì
     * AdminDashboardServlet đọc theo chỉ số. Lệch một ô là số chương hiện
     * thành số tài khoản mà không có gì báo lỗi.
     */
    /**
     * Đếm theo ngày — bản giả lập.
     *
     * Sinh theo công thức chứ không random: mỗi lần tải trang phải ra đúng
     * biểu đồ cũ, không thì người xem tưởng số liệu đang nhảy.
     *
     * Cuối tuần cho cao hơn ngày thường — số liệu giả mà phẳng lì thì nhìn
     * là biết giả, và cũng không kiểm được biểu đồ có vẽ đúng đỉnh hay không.
     */
    public static int[] countByDay(String what, int days) {
        int[] out = new int[days];
        int base = "views".equals(what) ? 40 : ("users".equals(what) ? 3 : 2);
        java.time.LocalDate d = java.time.LocalDate.now().minusDays(days - 1L);
        for (int i = 0; i < days; i++, d = d.plusDays(1)) {
            int dow = d.getDayOfWeek().getValue();          // 6,7 = cuối tuần
            int wave = (i * 7 + base * 3) % 11;
            out[i] = Math.max(0, base + wave / 2 + (dow >= 6 ? base : 0));
        }
        return out;
    }

    /** Lượt xem theo ngày của một truyện — bản giả lập. */
    public static int[] viewsByDay(int storyId, int days) {
        int[] out = new int[days];
        Story s = story(storyId);
        int base = s == null ? 3 : Math.max(1, s.getViewCount() / 4000);
        for (int i = 0; i < days; i++) {
            out[i] = Math.max(0, base + (i * storyId + 5) % (base + 6) - 2);
        }
        return out;
    }

    public static int[] adminOverview() {
        int chapters = 0, views = 0, cmt = 0;
        for (Story s : stories()) {
            chapters += s.getChapterCount();
            views += s.getViewCount();
            cmt += comments(s.getId()).size();
        }
        return new int[]{stories().size(), chapters, users().size(), views, cmt, 1};
    }
}
