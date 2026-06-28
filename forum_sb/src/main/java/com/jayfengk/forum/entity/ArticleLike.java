package com.jayfengk.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * article_likes 表對應 entity
 * ============================================================
 * 一筆 row = 一個使用者讚過一篇文章。
 * UNIQUE KEY (article_id, user_id) 由 DB 保證不會重複。
 * 沒有軟刪除：unlike 直接 DELETE。
 */
@Data
@TableName("article_likes")
public class ArticleLike implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long articleId;
    private Long userId;
    private LocalDateTime createdAt;
}
