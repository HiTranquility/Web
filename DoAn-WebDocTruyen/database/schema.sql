-- =============================================================================
--  WEB ĐỌC TRUYỆN — Lược đồ cơ sở dữ liệu
-- =============================================================================
--  Chạy file này MỘT LẦN để tạo database và toàn bộ bảng.
--  Xem README.md phần "Cài đặt database" để biết lệnh chạy.
-- =============================================================================

DROP DATABASE IF EXISTS webdoctruyen;
CREATE DATABASE webdoctruyen
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
-- utf8mb4 chứ KHÔNG phải utf8: bảng mã "utf8" của MySQL chỉ chứa được 3 byte,
-- không đủ cho emoji. Truyện và bình luận chắc chắn sẽ có emoji.
-- Collation _unicode_ci: so sánh không phân biệt hoa thường, sắp xếp đúng
-- tiếng Việt có dấu.

USE webdoctruyen;


-- =============================================================================
--  users — tài khoản
-- =============================================================================
--  CHỈ CÓ MỘT BẢNG NGƯỜI DÙNG, KHÔNG CÓ BẢNG "authors" RIÊNG.
--  "Tác giả" không phải một loại người — nó là quan hệ: ai đăng truyện thì là
--  tác giả của truyện đó (stories.author_id). Cùng một tài khoản vừa có thể
--  là tác giả truyện A, vừa là độc giả truyện B. Tách bảng là nhân đôi dữ liệu.
-- =============================================================================
CREATE TABLE users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(150) NOT NULL UNIQUE,

    -- Lưu chuỗi BĂM, tuyệt đối không lưu mật khẩu gốc.
    -- 255 ký tự để chứa thoải mái chuỗi băm BCrypt (60) hoặc dài hơn sau này.
    password_hash VARCHAR(255) NOT NULL,

    display_name  VARCHAR(100),
    avatar_url    VARCHAR(255),
    bio           VARCHAR(500),

    -- Chỉ hai vai trò. Không cần AUTHOR — xem ghi chú đầu bảng.
    role          ENUM('USER','ADMIN')      NOT NULL DEFAULT 'USER',

    -- BANNED phục vụ chức năng "admin ban tài khoản".
    -- Bị ban thì chặn đăng nhập, nhưng TRUYỆN VẪN GIỮ NGUYÊN — độc giả đang
    -- đọc dở không bị mất. Đây là quyết định thiết kế, không phải thiếu sót.
    status        ENUM('ACTIVE','BANNED')   NOT NULL DEFAULT 'ACTIVE',
    ban_reason    VARCHAR(255),

    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_users_status (status)
) ENGINE=InnoDB;


-- =============================================================================
--  stories — truyện
-- =============================================================================
CREATE TABLE stories (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(200) NOT NULL,

    -- slug: chuỗi thân thiện URL, ví dụ "dau-pha-thuong-khung".
    -- Dùng cho URL đẹp và để chia sẻ link. UNIQUE để không đụng nhau.
    slug         VARCHAR(220) NOT NULL UNIQUE,

    description  TEXT,
    cover_url    VARCHAR(255),

    author_id    INT NOT NULL,

    -- DRAFT     : tác giả đang viết, chưa ai thấy
    -- PUBLISHED : hiện công khai
    -- DELETED   : admin đã gỡ — XOÁ MỀM, không xoá thật khỏi bảng.
    --             Xoá thật thì bình luận và bookmark trỏ tới nó sẽ mồ côi,
    --             phải xử lý dây chuyền rất mệt. Ẩn đi là đủ.
    status       ENUM('DRAFT','PUBLISHED','DELETED') NOT NULL DEFAULT 'DRAFT',

    -- ONGOING: đang ra chương;  COMPLETED: đã hoàn thành
    progress     ENUM('ONGOING','COMPLETED')         NOT NULL DEFAULT 'ONGOING',

    -- Đếm sẵn lượt xem thay vì COUNT() mỗi lần tải trang — nhanh hơn nhiều.
    view_count   INT NOT NULL DEFAULT 0,

    -- Điểm đánh giá, PHI CHUẨN HOÁ CÓ CHỦ Ý.
    --
    -- Điểm trung bình tính được từ bảng ratings bằng AVG(score). Nhưng lưới
    -- kho truyện hiện 24 truyện một trang, mỗi truyện một ngôi sao — làm đúng
    -- chuẩn là 24 câu AVG cho một lần tải trang.
    --
    -- Giữ sẵn TỔNG và SỐ LƯỢT, chia ra là có trung bình. Lưu tổng chứ không
    -- lưu thẳng trung bình: cộng thêm một điểm mới chỉ là sum+=score,
    -- count+=1. Lưu trung bình thì phải nhân ngược lại rồi chia, sai số dồn
    -- dần sau vài nghìn lượt.
    --
    -- CÁI GIÁ: hai chỗ phải cùng đúng. RatingDAO sửa cả hai trong MỘT
    -- transaction, không có đường nào khác chạm vào hai cột này.
    rating_sum   INT NOT NULL DEFAULT 0,
    rating_count INT NOT NULL DEFAULT 0,

    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                          ON UPDATE CURRENT_TIMESTAMP,

    -- ON DELETE CASCADE: xoá hẳn user thì truyện đi theo.
    -- (Thực tế ta chỉ BAN chứ không xoá user, nên nhánh này hiếm khi chạy.)
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,

    INDEX idx_stories_status  (status),
    INDEX idx_stories_author  (author_id),
    INDEX idx_stories_updated (updated_at)
) ENGINE=InnoDB;


-- =============================================================================
--  chapters — chương truyện
-- =============================================================================
--  Nội dung chương lưu thẳng trong DB (kiểu TEXT), không lưu ra file.
--  Lý do: đọc online theo từng chương, và nút "Tải truyện" chỉ việc ghép các
--  chương lại thành .txt. Lưu ra file thì phải quản lý thêm đường dẫn, sao
--  lưu, và deploy lại là mất.
-- =============================================================================
CREATE TABLE chapters (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    story_id    INT NOT NULL,

    -- Số thứ tự chương trong truyện (1, 2, 3...). Không dùng id để sắp xếp
    -- vì tác giả có thể chèn chương vào giữa.
    chapter_no  INT NOT NULL,

    title       VARCHAR(200) NOT NULL,

    -- MEDIUMTEXT chứa tới 16 MB. TEXT thường chỉ 64 KB — một chương dài
    -- tiếng Việt có dấu rất dễ vượt, và MySQL sẽ CẮT CỤT âm thầm.
    content     MEDIUMTEXT NOT NULL,

    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                         ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (story_id) REFERENCES stories(id) ON DELETE CASCADE,

    -- Trong một truyện không được có hai chương cùng số.
    -- Ràng buộc ở DB chứ không chỉ ở code: code có thể quên, DB thì không.
    UNIQUE KEY uq_story_chapter (story_id, chapter_no)
) ENGINE=InnoDB;


-- =============================================================================
--  tags — thể loại / nhãn
-- =============================================================================
CREATE TABLE tags (
    id    INT AUTO_INCREMENT PRIMARY KEY,
    name  VARCHAR(50)  NOT NULL UNIQUE,   -- "Tiên hiệp"
    slug  VARCHAR(60)  NOT NULL UNIQUE    -- "tien-hiep"  (dùng trong URL lọc)
) ENGINE=InnoDB;


-- =============================================================================
--  story_tags — bảng NỐI giữa truyện và tag  (quan hệ nhiều-nhiều)
-- =============================================================================
--  Một truyện có nhiều tag, một tag thuộc nhiều truyện. Quan hệ N–N thì BẮT
--  BUỘC phải có bảng nối — không thể nhét vào cột của bảng nào cả.
--
--  ĐỪNG làm kiểu stories.tags = "tien-hiep,huyen-huyen" (chuỗi ngăn phẩy).
--  Nhìn thì gọn, nhưng lọc theo tag sẽ phải LIKE '%tien-hiep%' — chậm, không
--  dùng được index, và khớp nhầm ("tien-hiep" khớp cả "tien-hiep-hai-huoc").
-- =============================================================================
CREATE TABLE story_tags (
    story_id  INT NOT NULL,
    tag_id    INT NOT NULL,

    -- Khoá chính gồm hai cột: một cặp (truyện, tag) chỉ tồn tại một lần.
    PRIMARY KEY (story_id, tag_id),

    FOREIGN KEY (story_id) REFERENCES stories(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id)   REFERENCES tags(id)    ON DELETE CASCADE,

    -- Index ngược, để truy vấn "tag này có những truyện nào" cũng nhanh.
    INDEX idx_story_tags_tag (tag_id)
) ENGINE=InnoDB;


-- =============================================================================
--  comments — bình luận
-- =============================================================================
--  Bình luận gắn với TRUYỆN, không gắn với từng chương. Đây là lựa chọn để
--  giảm độ phức tạp. Muốn nâng cấp sau: thêm cột chapter_id NULL-able, NULL
--  nghĩa là bình luận ở cấp truyện.
-- =============================================================================
CREATE TABLE comments (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    story_id   INT NOT NULL,
    user_id    INT NOT NULL,

    content    VARCHAR(1000) NOT NULL,

    -- Admin gỡ bình luận vi phạm nội quy thì đổi sang HIDDEN, không xoá hẳn —
    -- giữ lại để còn bằng chứng khi xử lý tài khoản.
    status     ENUM('VISIBLE','HIDDEN') NOT NULL DEFAULT 'VISIBLE',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (story_id) REFERENCES stories(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id)  REFERENCES users(id)   ON DELETE CASCADE,

    INDEX idx_comments_story (story_id, created_at)
) ENGINE=InnoDB;


-- =============================================================================
--  bookmarks — đánh dấu truyện
-- =============================================================================
--  Gộp HAI nhu cầu vào một bảng:
--    1. "lưu truyện để đọc sau"      -> chỉ cần (user_id, story_id)
--    2. "nhớ đang đọc tới chương mấy" -> cột last_chapter_id
--  Tách làm hai bảng cũng được, nhưng gộp thì ít việc hơn mà vẫn đủ dùng.
-- =============================================================================
CREATE TABLE bookmarks (
    user_id         INT NOT NULL,
    story_id        INT NOT NULL,

    -- NULL = đã lưu nhưng chưa đọc chương nào.
    last_chapter_id INT NULL,

    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Một người chỉ đánh dấu một truyện một lần.
    PRIMARY KEY (user_id, story_id),

    FOREIGN KEY (user_id)         REFERENCES users(id)    ON DELETE CASCADE,
    FOREIGN KEY (story_id)        REFERENCES stories(id)  ON DELETE CASCADE,

    -- SET NULL chứ không CASCADE: tác giả xoá chương đang đọc dở thì chỉ mất
    -- vị trí đọc, KHÔNG mất luôn cả bookmark.
    FOREIGN KEY (last_chapter_id) REFERENCES chapters(id) ON DELETE SET NULL
) ENGINE=InnoDB;


-- =============================================================================
--  ratings — chấm sao truyện
-- =============================================================================
--  MỖI NGƯỜI MỘT ĐIỂM CHO MỖI TRUYỆN — khoá chính kép ép điều đó ở tầng CSDL.
--  Chặn ở tầng Java thôi thì hai request gửi cùng lúc vẫn lọt được cả hai.
--  Cho phép chấm lại: UPDATE dòng cũ, không INSERT dòng mới.
-- =============================================================================
CREATE TABLE ratings (
    user_id    INT NOT NULL,
    story_id   INT NOT NULL,

    -- 1..5 sao. CHECK để dữ liệu rác không lọt vào bằng đường khác (import,
    -- sửa tay trong Workbench). MySQL 8.0.16 trở lên mới thực sự áp dụng CHECK.
    score      TINYINT NOT NULL CHECK (score BETWEEN 1 AND 5),

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (user_id, story_id),
    FOREIGN KEY (user_id)  REFERENCES users(id)   ON DELETE CASCADE,
    FOREIGN KEY (story_id) REFERENCES stories(id) ON DELETE CASCADE,

    -- Xếp hạng theo điểm cần quét theo truyện, không theo người.
    INDEX idx_rating_story (story_id)
) ENGINE=InnoDB;


-- =============================================================================
--  follows — theo dõi tác giả
-- =============================================================================
--  Quan hệ NHIỀU-NHIỀU của users với chính nó. Cả hai cột đều trỏ về users:
--  follower_id là người bấm theo dõi, author_id là người được theo dõi.
-- =============================================================================
CREATE TABLE follows (
    follower_id INT NOT NULL,
    author_id   INT NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (follower_id, author_id),
    FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id)   REFERENCES users(id) ON DELETE CASCADE,

    -- Đếm "tác giả này có bao nhiêu người theo dõi" quét theo author_id.
    -- Khoá chính đã phủ chiều follower_id rồi nên chỉ cần thêm chiều này.
    INDEX idx_follow_author (author_id),

    -- Không cho tự theo dõi chính mình.
    CHECK (follower_id <> author_id)
) ENGINE=InnoDB;


-- =============================================================================
--  notifications — thông báo chương mới
-- =============================================================================
--  SINH RA LÚC ĐĂNG CHƯƠNG, không phải lúc mở trang thông báo.
--
--  Cách khác là để trống bảng này rồi mỗi lần mở trang mới đi hỏi "có chương
--  nào mới hơn lần đọc trước không". Cách đó không cần bảng, nhưng không đánh
--  dấu được đã đọc/chưa đọc từng cái, và câu truy vấn nặng dần theo số truyện.
-- =============================================================================
CREATE TABLE notifications (
    id         INT AUTO_INCREMENT PRIMARY KEY,

    -- Người NHẬN thông báo.
    user_id    INT NOT NULL,

    story_id   INT NULL,
    chapter_id INT NULL,

    -- Để sẵn cho các loại sau: bình luận mới, truyện bị gỡ...
    type       ENUM('NEW_CHAPTER', 'SYSTEM') NOT NULL DEFAULT 'NEW_CHAPTER',

    message    VARCHAR(255) NOT NULL,
    is_read    BOOLEAN  NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE,
    FOREIGN KEY (story_id)   REFERENCES stories(id)  ON DELETE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE,

    -- Câu truy vấn nóng nhất: "thông báo của tôi, mới nhất trước".
    -- Index gộp hai cột theo đúng thứ tự đó.
    INDEX idx_notif_user (user_id, created_at DESC)
) ENGINE=InnoDB;


-- =============================================================================
--  reports — báo cáo vi phạm
-- =============================================================================
--  MỘT BẢNG CHO CẢ TRUYỆN LẪN BÌNH LUẬN, phân biệt bằng target_type.
--
--  Đây là đánh đổi có ý thức: KHÔNG khai được khoá ngoại cho target_id, vì nó
--  trỏ sang hai bảng khác nhau tuỳ dòng. Đổi lại, thêm loại báo cáo mới (báo
--  cáo người dùng chẳng hạn) chỉ cần thêm một giá trị ENUM, không phải tạo
--  bảng mới và không phải viết lại trang xử lý báo cáo.
--
--  Với đồ án, thiếu một khoá ngoại ở bảng phụ trợ này chấp nhận được. Nếu là
--  hệ thống thật thì nên tách reports_story và reports_comment.
-- =============================================================================
CREATE TABLE reports (
    id          INT AUTO_INCREMENT PRIMARY KEY,

    reporter_id INT NOT NULL,
    target_type ENUM('STORY', 'COMMENT') NOT NULL,
    target_id   INT NOT NULL,

    reason      VARCHAR(500) NOT NULL,

    -- PENDING: chờ xử lý · RESOLVED: đã xử lý · DISMISSED: bỏ qua
    status      ENUM('PENDING', 'RESOLVED', 'DISMISSED') NOT NULL DEFAULT 'PENDING',

    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    handled_at  DATETIME NULL,

    FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE CASCADE,

    -- Trang xử lý luôn lọc "còn chờ xử lý" trước.
    INDEX idx_report_status (status, created_at)
) ENGINE=InnoDB;


-- =============================================================================
--  view_logs — nhật ký lượt xem
-- =============================================================================
--  VÌ SAO PHẢI CÓ BẢNG NÀY KHI ĐÃ CÓ stories.view_count
--
--    view_count là MỘT con số cộng dồn từ ngày đăng. Nó trả lời được "truyện
--    này tổng cộng bao nhiêu lượt xem", nhưng KHÔNG trả lời được "tuần này
--    bao nhiêu" — vì nó không nhớ lượt xem xảy ra khi nào.
--
--    Muốn có bảng xếp hạng theo tuần/tháng thì bắt buộc phải ghi từng lượt xem
--    kèm thời điểm. Đây chính là chỗ đánh đổi giữa gộp sẵn (nhanh, mất chi
--    tiết) và ghi thô (chậm hơn, giữ đủ chi tiết) — dự án dùng CẢ HAI:
--      · view_count  cho con số hiển thị trên mọi thẻ truyện  (đọc rất nhiều)
--      · view_logs   cho bảng xếp hạng theo thời gian         (đọc ít)
--
--  BẢNG NÀY SẼ LỚN NHẤT HỆ THỐNG. Mỗi lượt mở truyện là một dòng. Hệ thống
--  thật sẽ dọn định kỳ (xoá dòng cũ hơn 90 ngày) vì bảng xếp hạng không cần
--  dữ liệu năm ngoái.
-- =============================================================================
CREATE TABLE view_logs (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    story_id  INT NOT NULL,

    -- NULL = khách chưa đăng nhập. Vẫn tính vào lượt xem.
    user_id   INT NULL,

    viewed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (story_id) REFERENCES stories(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id)  REFERENCES users(id)   ON DELETE SET NULL,

    -- Xếp hạng tuần chạy: WHERE viewed_at >= ? GROUP BY story_id
    -- Thời gian đứng TRƯỚC vì nó là điều kiện lọc, story_id đứng sau để
    -- MySQL gom nhóm ngay trên index, khỏi phải đọc bảng.
    INDEX idx_viewlog_time (viewed_at, story_id)
) ENGINE=InnoDB;


-- =============================================================================
--  password_resets — vé đặt lại mật khẩu
-- =============================================================================
--  VÉ DÙNG MỘT LẦN, CÓ HẠN.
--
--  Không đặt token vào bảng users vì đó là dữ liệu TẠM: sống vài chục phút rồi
--  bỏ. Nhét vào users là mỗi tài khoản phải mang thêm hai cột gần như luôn
--  rỗng, và không lưu được lịch sử ai xin đặt lại bao nhiêu lần.
-- =============================================================================
CREATE TABLE password_resets (
    -- Token là chuỗi ngẫu nhiên dài, KHÔNG đoán được. Dùng luôn làm khoá chính.
    token      CHAR(43)     NOT NULL PRIMARY KEY,

    user_id    INT NOT NULL,

    -- Hết hạn thì vé vô giá trị dù chưa dùng. Không có hạn thì một email cũ
    -- lộ ra sau ba năm vẫn đổi được mật khẩu.
    expires_at DATETIME NOT NULL,

    -- Đã dùng rồi thì không dùng lại. Giữ dòng lại thay vì xoá để còn thấy
    -- lịch sử.
    used_at    DATETIME NULL,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_reset_user (user_id)
) ENGINE=InnoDB;


-- =============================================================================
--  Kiểm tra
-- =============================================================================
SHOW TABLES;
