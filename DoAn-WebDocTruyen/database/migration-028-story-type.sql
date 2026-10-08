-- =============================================================================
-- Migration 028: LOẠI TRUYỆN — truyện chữ (TEXT) và truyện tranh (COMIC)
-- =============================================================================
--  Chạy:  mysql -u root -p < database/migration-028-story-type.sql
--
--  THIẾT KẾ
--    stories.story_type   — mỗi truyện thuộc ĐÚNG MỘT loại. Mặc định TEXT nên
--                           mọi truyện cũ giữ nguyên, không phải sửa dữ liệu.
--    chapter_pages        — ảnh từng trang của chương truyện tranh, theo thứ tự.
--                           Chương truyện chữ không có dòng nào ở đây; chương
--                           truyện tranh để chapters.content rỗng.
--
--  VÌ SAO KHÔNG NHỒI DANH SÁCH ẢNH VÀO chapters.content
--    Được, nhưng sửa thứ tự một trang là phải tách chuỗi, ghép lại, và không
--    còn khoá ngoại nào nhắc khi file ảnh mồ côi. Một bảng riêng cho phép
--    sắp xếp bằng một cột số, đếm bằng COUNT, và xoá chương là trang tự đi theo.
--
--  THÊM LOẠI MỚI SAU NÀY (ví dụ AUDIO — truyện nói)
--    1. ALTER ENUM thêm giá trị   2. Story.TYPE_* + Story.normalizeType()
--    3. Một nhánh hiển thị ở common/chapter/_block.jsp và form soạn chương.
--    Mọi chỗ còn lại (lọc, nhãn, gợi ý, đánh dấu, đọc tiếp) đã đi qua
--    Story.storyType nên tự chạy.
--
--  Chạy lại nhiều lần không hỏng gì.
-- =============================================================================

USE webdoctruyen;

SET @has_col = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'stories'
                  AND COLUMN_NAME = 'story_type');
SET @sql = IF(@has_col > 0, 'SELECT 1',
    'ALTER TABLE stories
        ADD COLUMN story_type ENUM(''TEXT'',''COMIC'') NOT NULL DEFAULT ''TEXT'' AFTER progress,
        ADD INDEX idx_stories_type (status, story_type)');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS chapter_pages (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    chapter_id  INT NOT NULL,

    -- Thứ tự trang trong chương, đếm từ 1. Cũng chính là "para_no" khi đánh
    -- dấu / đọc tiếp: với truyện tranh, một trang đóng vai một đoạn văn.
    page_no     INT NOT NULL,

    -- Đường dẫn tương đối trong thư mục uploads: pages/{storyId}/{chapterId}/x.jpg
    -- UploadedFileServlet KIỂM QUYỀN ĐỌC chương trước khi phát ảnh trong pages/.
    image_path  VARCHAR(255) NOT NULL,

    -- Kích thước gốc, đọc từ phần đầu file lúc tải lên. Trang đọc dùng để
    -- giữ chỗ đúng tỉ lệ TRƯỚC khi ảnh tải xong — không có thì trang nhảy
    -- liên tục khi ảnh lần lượt hiện, và "đọc tiếp tới trang 20" cuộn sai chỗ.
    width       INT NULL,
    height      INT NULL,

    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY uq_chapter_page (chapter_id, page_no),
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
