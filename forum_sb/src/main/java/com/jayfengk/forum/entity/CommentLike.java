package com.jayfengk.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * comment_likes 表對應 entity（同 ArticleLike，只是目標換成 comment）
 */
@Data
@TableName("comment_likes")
public class CommentLike implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long commentId;
    private Long userId;
    private LocalDateTime createdAt;
}
