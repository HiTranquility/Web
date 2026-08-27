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
--  Kiểm tra
-- =============================================================================
SHOW TABLES;
