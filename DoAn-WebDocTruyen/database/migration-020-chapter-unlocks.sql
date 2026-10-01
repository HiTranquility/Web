-- =============================================================================
-- Migration cho ISSUE-020: Mở khoá chương bằng xu ảo & Chương VIP
-- =============================================================================

USE webdoctruyen;

-- 1. Thêm cột is_vip và coin_price vào bảng chapters nếu chưa có
SET @dbname = DATABASE();
SET @tablename = 'chapters';
SET @columnname = 'is_vip';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      TABLE_SCHEMA = @dbname
      AND TABLE_NAME = @tablename
      AND COLUMN_NAME = @columnname
  ) > 0,
  'SELECT 1',
  'ALTER TABLE chapters ADD COLUMN is_vip BOOLEAN NOT NULL DEFAULT FALSE, ADD COLUMN coin_price INT NOT NULL DEFAULT 0;'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 2. Tạo bảng chapter_unlocks ghi nhận độc giả mở khoá chương
CREATE TABLE IF NOT EXISTS chapter_unlocks (
    user_id     INT NOT NULL,
    chapter_id  INT NOT NULL,
    price_paid  INT NOT NULL,
    unlocked_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, chapter_id),
    FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE,
    INDEX idx_unlock_chapter (chapter_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Đặt mẫu chương 3 truyện 1 làm chương VIP (20 xu) để demo kiểm thử
UPDATE chapters SET is_vip = TRUE, coin_price = 20 WHERE story_id = 1 AND chapter_no = 3;
