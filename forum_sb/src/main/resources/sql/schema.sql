-- ============================================================
-- Forum 資料庫 schema（MySQL 8.x）
-- ============================================================
-- 純 schema 定義 — CREATE TABLE 全部用 IF NOT EXISTS，可重複執行不會出錯。
-- 用途：
--   1. 第一次 setup 時建表
--   2. 之後加新表時，再次跑這個檔，舊表保留、新表建立（idempotent）
--
-- 假設：資料庫 `forum` 已經存在（由 Docker MYSQL_DATABASE env var 建好）。
-- 這個檔不負責 CREATE DATABASE，只負責 schema。
-- ============================================================

-- ------------------------------------------------------------
-- users 表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主鍵',
    name              VARCHAR(255)    NOT NULL                COMMENT '顯示名稱',
    email             VARCHAR(255)    NOT NULL                COMMENT '登入帳號（唯一）',
    email_verified_at DATETIME        NULL                    COMMENT 'Email 驗證時間（MVP 暫不使用）',
    password          VARCHAR(255)    NOT NULL                COMMENT 'BCrypt 雜湊後的密碼',
    created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='使用者';

-- ------------------------------------------------------------
-- articles 表（軟刪除：deleted_at）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS articles (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    title      VARCHAR(255)    NOT NULL                COMMENT '文章標題',
    content    TEXT            NOT NULL                COMMENT '文章內容（純文字）',
    state      VARCHAR(20)     NOT NULL DEFAULT 'published' COMMENT 'draft / published',
    user_id    BIGINT UNSIGNED NOT NULL                COMMENT '作者 user_id',
    deleted_at DATETIME        NULL                    COMMENT '軟刪除時間（MyBatis-Plus @TableLogic）',
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_articles_user_id (user_id),
    KEY idx_articles_created_at (created_at),
    CONSTRAINT fk_articles_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章';

-- ------------------------------------------------------------
-- password_reset_tokens 表
-- ------------------------------------------------------------
-- 一個 email 只保留一筆有效 token：插入前先 DELETE WHERE email = ?。
-- token 存 SHA-256 hash，不存明文；明文只透過 email 寄給使用者。
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    email      VARCHAR(255) NOT NULL,
    token      VARCHAR(255) NOT NULL                COMMENT 'SHA-256 雜湊後的 token',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='密碼重設 token';

-- ------------------------------------------------------------
-- comments 表（文章留言，軟刪除）
-- ------------------------------------------------------------
-- 一篇文章下一串平鋪留言（不嵌套）。按 created_at 由舊到新顯示。
CREATE TABLE IF NOT EXISTS comments (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    article_id BIGINT UNSIGNED NOT NULL                COMMENT '留言所屬文章',
    user_id    BIGINT UNSIGNED NOT NULL                COMMENT '留言者',
    content    TEXT            NOT NULL                COMMENT '留言內容',
    deleted_at DATETIME        NULL                    COMMENT '軟刪除時間',
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_comments_article_id (article_id),
    KEY idx_comments_user_id (user_id),
    CONSTRAINT fk_comments_article FOREIGN KEY (article_id) REFERENCES articles (id),
    CONSTRAINT fk_comments_user    FOREIGN KEY (user_id)    REFERENCES users    (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章留言';

-- ------------------------------------------------------------
-- article_likes / comment_likes 表（點讚）
-- ------------------------------------------------------------
-- UNIQUE KEY 保證一個使用者對同一個目標只能讚一次。
-- 沒有軟刪除：unlike 直接 DELETE row。
CREATE TABLE IF NOT EXISTS article_likes (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    article_id BIGINT UNSIGNED NOT NULL,
    user_id    BIGINT UNSIGNED NOT NULL,
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_article_likes_article_user (article_id, user_id),
    KEY idx_article_likes_user_id (user_id),
    CONSTRAINT fk_article_likes_article FOREIGN KEY (article_id) REFERENCES articles (id),
    CONSTRAINT fk_article_likes_user    FOREIGN KEY (user_id)    REFERENCES users    (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章點讚';

CREATE TABLE IF NOT EXISTS comment_likes (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    comment_id BIGINT UNSIGNED NOT NULL,
    user_id    BIGINT UNSIGNED NOT NULL,
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_comment_likes_comment_user (comment_id, user_id),
    KEY idx_comment_likes_user_id (user_id),
    CONSTRAINT fk_comment_likes_comment FOREIGN KEY (comment_id) REFERENCES comments (id),
    CONSTRAINT fk_comment_likes_user    FOREIGN KEY (user_id)    REFERENCES users    (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='留言點讚';
