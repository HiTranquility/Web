-- =============================================================================
-- Migration cho Gamification: Điểm danh nhận Xu & Nhiệm vụ hàng ngày
-- =============================================================================

USE webdoctruyen;

CREATE TABLE IF NOT EXISTS daily_checkins (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    user_id      INT NOT NULL,
    checkin_date DATE NOT NULL,
    streak_days  INT NOT NULL DEFAULT 1,
    reward_coins INT NOT NULL DEFAULT 10,
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_user_checkin (user_id, checkin_date),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_checkin_user (user_id, checkin_date DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS daily_quest_claims (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    user_id      INT NOT NULL,
    quest_key    VARCHAR(50) NOT NULL,
    claim_date   DATE NOT NULL,
    reward_coins INT NOT NULL DEFAULT 10,
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_user_quest_date (user_id, quest_key, claim_date),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
