package com.jayfengk.forum.dto;

import com.jayfengk.forum.entity.Comment;
import com.jayfengk.forum.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 對外回傳的留言資料（含留言者姓名）
 * ============================================================
 * 前端需要顯示「誰留的言」，所以列表時順帶把 author name 一起回，
 * 不需要前端再針對每個 userId 打一次 user API。
 */
@Data
public class CommentDto {
    private Long id;
    private Long articleId;
    private Long userId;
    private String authorName;
    private String content;
    private LocalDateTime createdAt;

    private long likeCount;
    private boolean likedByMe;

    public static CommentDto from(Comment comment, User author, long likeCount, boolean likedByMe) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setArticleId(comment.getArticleId());
        dto.setUserId(comment.getUserId());
        dto.setAuthorName(author == null ? null : author.getName());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setLikeCount(likeCount);
        dto.setLikedByMe(likedByMe);
        return dto;
    }
}
