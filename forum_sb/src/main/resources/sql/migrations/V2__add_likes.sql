-- ============================================================
-- Migration V2：點讚功能（article_likes + comment_likes 兩張表）
-- ============================================================
-- 給已執行過 init.sql 的 DB 用，不會動到現有資料。
--
-- 設計重點：
--   - 兩張獨立表（不用 polymorphic 一張 likes），FK 可設、查詢直觀
--   - UNIQUE KEY 保證一個使用者對同一個目標只能讚一次（重複插入會被 DB 拒絕）
--   - 沒有軟刪除：unlike 就直接 DELETE row（純行為紀錄，不需要保留歷史）
--
-- 執行：
--   mysql -uroot -p forum < V2__add_likes.sql
-- ============================================================

USE forum;

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
