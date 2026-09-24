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
ALTER TABLE comments
    ADD COLUMN chapter_id INT NULL AFTER story_id,
    ADD CONSTRAINT fk_comments_chapter
        FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE,
    ADD INDEX idx_comments_chapter (chapter_id, created_at);

-- =============================================================================
--  LỆNH QUAY LUI (ROLLBACK) — khi cần hoàn tác:
-- =============================================================================
--  ALTER TABLE comments DROP FOREIGN KEY fk_comments_chapter;
--  ALTER TABLE comments DROP INDEX idx_comments_chapter;
--  ALTER TABLE comments DROP COLUMN chapter_id;
-- =============================================================================
