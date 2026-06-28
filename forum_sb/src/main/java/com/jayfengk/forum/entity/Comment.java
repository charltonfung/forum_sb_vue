package com.jayfengk.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * comments 表對應 entity（文章留言，軟刪除）
 * ============================================================
 * 一篇文章 (article_id) 下有多筆留言，每筆留言有作者 (user_id)。
 * 不嵌套（沒有 parent_id），就是一層平鋪的留言串。
 *
 * 軟刪除靠 @TableLogic：mapper.deleteById() 會轉成
 * UPDATE comments SET deleted_at = NOW()，select 自動加 deleted_at IS NULL。
 */
@Data
@TableName("comments")
public class Comment implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long articleId;
    private Long userId;
    private String content;

    @TableLogic
    private LocalDateTime deletedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
