package com.jayfengk.forum.service;

import com.jayfengk.forum.dto.ArticleDto;
import com.jayfengk.forum.dto.ArticleRequest;
import com.jayfengk.forum.dto.PageResult;

public interface ArticleService {

    /**
     * 文章列表（分頁，新到舊）。
     * @param currentUserId 為 null 表示未登入，likedByMe 永遠為 false
     * @param keyword 模糊搜尋標題，null / 空字串 = 不過濾
     */
    PageResult<ArticleDto> list(Long currentUserId, String keyword, long page, long pageSize);

    /** 單篇文章。currentUserId 為 null 表示未登入。 */
    ArticleDto get(Long currentUserId, Long id);

    /** 建立文章（需登入，作者 = 當前使用者） */
    ArticleDto create(Long currentUserId, ArticleRequest req);

    /** 更新文章（只有作者可改） */
    ArticleDto update(Long currentUserId, Long id, ArticleRequest req);

    /** 刪除文章（軟刪除，只有作者可刪） */
    void delete(Long currentUserId, Long id);
}
