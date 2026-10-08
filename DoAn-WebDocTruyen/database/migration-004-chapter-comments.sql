-- =============================================================================
--  migration-004-chapter-comments.sql — Bình luận theo từng chương (ISSUE-004)
-- =============================================================================
--  Mục đích:
--    Thêm cột chapter_id (NULL-able) vào bảng comments để cho phép độc giả
--    thảo luận trực tiếp dưới chân từng chương đọc.
--    - chapter_id IS NULL     : bình luận ở cấp độ truyện (trang chi tiết)
--    - chapter_id IS NOT NULL : bình luận riêng của một chương
-- =============================================================================

USE webdoctruyen;

-- 1. Thêm cột chapter_id, ràng buộc khoá ngoại và chỉ mục tìm kiếm
--
-- Bọc trong kiểm tra INFORMATION_SCHEMA để CHẠY LẠI ĐƯỢC: setup-db.ps1 chạy
-- mọi migration theo thứ tự mỗi lần cài, và ALTER ... ADD COLUMN chạy lần hai
-- là lỗi "Duplicate column" làm dừng cả quá trình cài.
SET @has_col = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'comments'
                  AND COLUMN_NAME = 'chapter_id');
SET @sql = IF(@has_col > 0, 'SELECT 1',
    'ALTER TABLE comments
        ADD COLUMN chapter_id INT NULL AFTER story_id,
        ADD CONSTRAINT fk_comments_chapter
            FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE,
        ADD INDEX idx_comments_chapter (chapter_id, created_at)');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================================================================
--  LỆNH QUAY LUI (ROLLBACK) — khi cần hoàn tác:
-- =============================================================================
--  ALTER TABLE comments DROP FOREIGN KEY fk_comments_chapter;
--  ALTER TABLE comments DROP INDEX idx_comments_chapter;
--  ALTER TABLE comments DROP COLUMN chapter_id;
-- =============================================================================
