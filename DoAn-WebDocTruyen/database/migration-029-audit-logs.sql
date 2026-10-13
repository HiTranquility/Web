-- =============================================================================
--  migration-029-audit-logs.sql — Nhật ký thao tác quản trị (ISSUE-023)
-- =============================================================================
--  CHỈ GHI THÊM, KHÔNG SỬA, KHÔNG XOÁ (Append-only Audit Trail).
--  Mọi thao tác quản trị quan trọng (gỡ/khôi phục truyện, khoá/mở tài khoản,
--  ẩn bình luận, cấp/thu xu) đều được ghi lại để chống lạm quyền và truy vết.
-- =============================================================================

CREATE TABLE IF NOT EXISTS audit_logs (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id     INT NULL,
    action       ENUM('DELETE_STORY','RESTORE_STORY',
                      'BAN_USER','UNBAN_USER',
                      'HIDE_COMMENT','SHOW_COMMENT',
                      'GRANT_COIN','REVOKE_COIN') NOT NULL,
    target_type  ENUM('STORY','USER','COMMENT','WALLET') NOT NULL,
    target_id    INT NOT NULL,
    target_label VARCHAR(200) NULL,
    reason       VARCHAR(500) NULL,
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE SET NULL,

    INDEX idx_audit_time (created_at DESC),
    INDEX idx_audit_admin (admin_id, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
