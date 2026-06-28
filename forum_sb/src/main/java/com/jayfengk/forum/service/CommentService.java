package com.jayfengk.forum.service;

import com.jayfengk.forum.dto.CommentDto;

import java.util.List;

public interface CommentService {

    /** 列出某篇文章的所有留言（依時間由舊到新）。currentUserId 為 null = 未登入。 */
    List<CommentDto> listByArticle(Long currentUserId, Long articleId);

    /** 新增留言（需登入） */
    CommentDto create(Long currentUserId, Long articleId, String content);

    /** 刪除留言（軟刪除，僅留言作者可刪） */
    void delete(Long currentUserId, Long commentId);
}
