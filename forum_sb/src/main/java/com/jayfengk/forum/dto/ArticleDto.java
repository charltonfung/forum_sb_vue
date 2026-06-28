package com.jayfengk.forum.dto;

import com.jayfengk.forum.entity.Article;
import com.jayfengk.forum.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 對外回傳的文章資料（含作者基本資料）
 * ============================================================
 * 為什麼帶 author 而不是 userId？
 *   - 前端列表頁要顯示作者姓名，如果只給 userId 還要再打一支 API 查
 *   - 一次帶回來省一次 round-trip
 */
@Data
public class ArticleDto {
    private Long id;
    private String title;
    private String content;
    private Long userId;
    private String authorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 該文章的總讚數 */
    private long likeCount;

    /** 當前登入者是否已讚過（未登入時恆為 false） */
    private boolean likedByMe;

    /** 從 Article + 作者 + 點讚資訊 組裝 */
    public static ArticleDto from(Article article, User author, long likeCount, boolean likedByMe) {
        ArticleDto dto = new ArticleDto();
        dto.setId(article.getId());
        dto.setTitle(article.getTitle());
        dto.setContent(article.getContent());
        dto.setUserId(article.getUserId());
        dto.setAuthorName(author == null ? null : author.getName());
        dto.setCreatedAt(article.getCreatedAt());
        dto.setUpdatedAt(article.getUpdatedAt());
        dto.setLikeCount(likeCount);
        dto.setLikedByMe(likedByMe);
        return dto;
    }
}
