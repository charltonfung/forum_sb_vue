package com.jayfengk.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jayfengk.forum.entity.Article;

/**
 * Article mapper
 * ============================================================
 * BaseMapper 已經提供 selectPage、selectById 等 CRUD，
 * 文章列表 / 單篇查詢都用 BaseMapper 就夠了。
 *
 * 軟刪除自動處理（entity 有 @TableLogic）：
 *   - mapper.deleteById(1)  ↦  UPDATE articles SET deleted_at = NOW() WHERE id = 1
 *   - selectById / selectList 自動加 WHERE deleted_at IS NULL
 */
public interface ArticleMapper extends BaseMapper<Article> {
}
