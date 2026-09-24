-- =============================================================================
--  migration-001-google.sql — Cập nhật CSDL cho ISSUE-001 (Phase 1)
-- =============================================================================
--  Áp dụng cho CSDL hiện tại đang có dữ liệu mà không cần nạp lại từ đầu.
--  Chạy:
--      mysql -u root -p webdoctruyen < database/migration-001-google.sql
-- =============================================================================

USE webdoctruyen;

-- 1. Nới lỏng ràng buộc password_hash để cho phép NULL
--    Tài khoản đăng nhập hoàn toàn bằng Google không có mật khẩu băm để lưu.
ALTER TABLE users MODIFY password_hash VARCHAR(255) NULL;

-- 2. Tạo bảng user_identities liên kết các phương thức đăng nhập ngoại vi
CREATE TABLE IF NOT EXISTS user_identities (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    user_id      INT NOT NULL,
    provider     ENUM('GOOGLE') NOT NULL,
    provider_uid VARCHAR(255) NOT NULL,
    email        VARCHAR(150),
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uq_identity_provider (provider, provider_uid),
    UNIQUE KEY uq_identity_user (user_id, provider)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
--  LỆNH QUAY LUI (ROLLBACK) — khi cần hoàn tác:
-- =============================================================================
--  DROP TABLE IF EXISTS user_identities;
--  ALTER TABLE users MODIFY password_hash VARCHAR(255) NOT NULL;
-- =============================================================================
