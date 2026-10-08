-- =============================================================================
-- Migration 027: Tách VỊ TRÍ ĐỌC khỏi TRUYỆN ĐÃ LƯU + đánh dấu đoạn văn
-- =============================================================================
--  Chạy:  mysql -u root -p < database/migration-027-reading-progress.sql
--
--  VÌ SAO PHẢI TÁCH
--    Trước đây bảng bookmarks gánh hai việc: "truyện tôi bấm lưu" và "tôi đang
--    đọc tới chương mấy". Mở một chương là ChapterServlet ghi vị trí bằng
--    INSERT ... ON DUPLICATE KEY — nên truyện nào lỡ mở một chương cũng tự
--    nhảy vào "Truyện đã lưu", và bấm "Bỏ lưu" thì mất luôn vị trí đọc.
--
--    Giờ mỗi việc một bảng:
--      bookmarks         — chỉ những truyện người dùng TỰ BẤM lưu
--      reading_progress  — vị trí đọc: chương + đoạn văn, tự ghi khi đọc
--      chapter_bookmarks — những ĐOẠN người dùng tự đánh dấu để đọc lại
--
--  Chạy lại nhiều lần không hỏng gì: IF NOT EXISTS + INSERT IGNORE.
-- =============================================================================

USE webdoctruyen;

CREATE TABLE IF NOT EXISTS reading_progress (
    user_id     INT NOT NULL,
    story_id    INT NOT NULL,

    -- NULL khi tác giả xoá đúng chương đang đọc dở.
    chapter_id  INT NULL,

    -- Đoạn văn trên cùng màn hình lúc rời trang, đếm từ 1. 0 = đầu chương.
    -- Lưu theo ĐOẠN chứ không theo pixel: đổi cỡ chữ, đổi máy thì số pixel
    -- vô nghĩa, còn "đoạn thứ 37" thì vẫn đúng.
    para_no     INT NOT NULL DEFAULT 0,

    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (user_id, story_id),
    FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE,
    FOREIGN KEY (story_id)   REFERENCES stories(id)  ON DELETE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS chapter_bookmarks (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT NOT NULL,
    chapter_id  INT NOT NULL,

    -- Đoạn được đánh dấu, đếm từ 1. 0 = đánh dấu cả chương.
    para_no     INT NOT NULL DEFAULT 0,

    -- Trích vài chục chữ đầu của đoạn, chụp lúc đánh dấu. Danh sách đánh dấu
    -- hiện được ngay mà không phải nạp và cắt lại nội dung từng chương.
    snippet     VARCHAR(200) NULL,

    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY uq_chapter_bookmark (user_id, chapter_id, para_no),
    INDEX idx_cb_user (user_id, created_at),
    FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Chuyển vị trí đọc cũ sang bảng mới. Cột bookmarks.last_chapter_id để
-- nguyên (không còn code nào đọc) — xoá cột phải xoá khoá ngoại theo tên do
-- MySQL tự đặt, khác nhau giữa các máy, không đáng rủi ro.
--
-- JOIN chapters: bỏ qua vị trí trỏ tới chương đã không còn (dữ liệu nạp lúc
-- tắt kiểm khoá ngoại). Những dòng đó trước đây cũng không bao giờ hiện ở
-- "Đọc tiếp" vì truy vấn cũ JOIN chapters.
--
-- CHỈ CHẠY KHI CỘT CŨ CÒN: cài MỚI từ schema.sql thì bookmarks đã không có
-- last_chapter_id — chạy thẳng câu INSERT là lỗi "Unknown column" và
-- setup-db.ps1 dừng giữa chừng. Câu lệnh động + kiểm INFORMATION_SCHEMA,
-- cùng kiểu với migration-020.
SET @has_old = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'bookmarks'
                  AND COLUMN_NAME = 'last_chapter_id');
SET @sql = IF(@has_old = 0, 'SELECT 1',
    'INSERT IGNORE INTO reading_progress (user_id, story_id, chapter_id, para_no, updated_at)
     SELECT b.user_id, b.story_id, b.last_chapter_id, 0, b.created_at
     FROM bookmarks b
     JOIN chapters c ON c.id = b.last_chapter_id');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
