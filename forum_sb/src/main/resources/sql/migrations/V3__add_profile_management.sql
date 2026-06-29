-- ============================================================
-- Migration V3：個人資料管理（變更密碼 / email / 名稱）
-- ============================================================
-- 給「已執行過舊 schema 的人」用的補丁。動兩件事：
--   1. users 表加 credentials_changed_at 欄位（作為 JWT 失效水位線）
--   2. 新增 email_change_tokens 表（變更 email 的驗證 token）
--
-- 執行：
--   docker compose exec -T db mysql --default-character-set=utf8mb4 \
--     -uroot -proot forum < forum_sb/src/main/resources/sql/migrations/V3__add_profile_management.sql
--
-- 全新 setup（./db-reset.sh）不必跑這個，schema.sql 已包含。
-- ============================================================

USE forum;

-- ------------------------------------------------------------
-- 0. users.name 加 UNIQUE 限制（避免不同人撞同名）
-- ------------------------------------------------------------
-- 若 DB 已經有重名，這條會失敗，要先手動清乾淨：
--   SELECT name, COUNT(*) FROM users GROUP BY name HAVING COUNT(*) > 1;
-- 然後改名 / 砍掉重複的，再跑 migration。
-- 用 raw SQL 不用 IF NOT EXISTS（MySQL ADD CONSTRAINT 沒這個語法），
-- 重複跑會報 "Duplicate key name 'uk_users_name'"，那是預期、忽略即可。
ALTER TABLE users ADD UNIQUE KEY uk_users_name (name);

-- ------------------------------------------------------------
-- 1. users 加 credentials_changed_at
-- ------------------------------------------------------------
-- 改密碼 / 改 email 時更新成 NOW()。
-- JWT 驗證時若 token 的 iat < credentials_changed_at 視為失效，強制重新登入。
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS credentials_changed_at DATETIME NULL
        COMMENT '帳密變更時間，作為 JWT 失效水位線'
        AFTER password;

-- ------------------------------------------------------------
-- 2. email_change_tokens 表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS email_change_tokens (
    user_id    BIGINT UNSIGNED NOT NULL,
    new_email  VARCHAR(255)    NOT NULL                COMMENT '要改成的新 email',
    token      VARCHAR(255)    NOT NULL                COMMENT 'SHA-256 雜湊後的 token',
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    CONSTRAINT fk_email_change_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='變更 email 驗證 token';
