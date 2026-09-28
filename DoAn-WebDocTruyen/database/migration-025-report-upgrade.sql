-- =============================================================================
-- Migration cho ISSUE-025: Nâng cấp báo cáo vi phạm
--   1. Cột `category` — 8 loại vi phạm
--   2. Bảng `report_evidence` — ảnh bằng chứng, tối đa 3 ảnh mỗi báo cáo
--   3. Index cho bộ lọc theo loại ở trang quản trị
--
-- Chạy:  mysql -u root -p webdoctruyen < database/migration-025-report-upgrade.sql
--
-- AN TOÀN VỚI DỮ LIỆU CŨ: mọi báo cáo đã có rơi vào category = 'OTHER' nhờ
-- DEFAULT, không dòng nào phải sửa tay và không dòng nào mất.
-- =============================================================================

USE webdoctruyen;

-- ---------------------------------------------------------------------------
-- 1. Cột phân loại
-- ---------------------------------------------------------------------------
-- MySQL 8 không có "ADD COLUMN IF NOT EXISTS", nên bọc trong một thủ tục để
-- chạy lại lần hai không báo lỗi. Người trong nhóm hay lỡ chạy migration hai
-- lần — lỗi đỏ giữa chừng làm họ tưởng hỏng schema.
DROP PROCEDURE IF EXISTS migrate_025;
DELIMITER //
CREATE PROCEDURE migrate_025()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME   = 'reports'
          AND COLUMN_NAME  = 'category'
    ) THEN
        ALTER TABLE reports
            ADD COLUMN category ENUM('SPAM','ADULT','VIOLENCE','PRIVACY',
                                     'PLAGIARISM','WRONG_INFO','HARASSMENT','OTHER')
                       NOT NULL DEFAULT 'OTHER'
                       AFTER target_id;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME   = 'reports'
          AND INDEX_NAME   = 'idx_report_category'
    ) THEN
        ALTER TABLE reports
            ADD INDEX idx_report_category (category, status, created_at);
    END IF;
END //
DELIMITER ;

CALL migrate_025();
DROP PROCEDURE migrate_025;

-- ---------------------------------------------------------------------------
-- 2. Bảng ảnh bằng chứng
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS report_evidence (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    report_id  INT NOT NULL,

    -- Luôn bắt đầu bằng "evidence/" — UploadedFileServlet nhìn tiền tố này
    -- để chặn người không phải admin. Xem ISSUE-025 phase-2 §3.2.
    file_path  VARCHAR(255) NOT NULL,
    file_size  INT NOT NULL,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_evidence_report
        FOREIGN KEY (report_id) REFERENCES reports(id) ON DELETE CASCADE,

    INDEX idx_evidence_report (report_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- 3. Kiểm tra
-- ---------------------------------------------------------------------------
SELECT 'reports.category' AS kiem_tra,
       COUNT(*)           AS so_bao_cao,
       SUM(category = 'OTHER') AS loai_khac
FROM reports;

SELECT 'report_evidence' AS kiem_tra, COUNT(*) AS so_anh FROM report_evidence;
