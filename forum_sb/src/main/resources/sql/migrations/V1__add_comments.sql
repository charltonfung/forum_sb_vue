-- ============================================================
-- Migration V1：新增留言功能（comments 表）
-- ============================================================
-- 給「已執行過 init.sql 的人」用的補丁，不會動到現有 users / articles 資料。
--
-- 執行：
--   mysql -uroot -p forum < V1__add_comments.sql
-- 或在 MySQL CLI 內：
--   USE forum;
--   source D:/program-language/Java/forum/forum_sb/src/main/resources/sql/migrations/V1__add_comments.sql
-- ============================================================

USE forum;

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

-- 給 demo 文章塞一筆示範留言
INSERT INTO comments (article_id, user_id, content)
SELECT 1, 1, '這是示範留言，確認列表 / 新增 / 刪除流程都能跑。'
WHERE EXISTS (SELECT 1 FROM articles WHERE id = 1)
  AND EXISTS (SELECT 1 FROM users WHERE id = 1);
