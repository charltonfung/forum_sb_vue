package com.jayfengk.forum.service;

public interface LikeService {

    /** 讚一篇文章（idempotent：已讚過再呼叫也回成功） */
    void likeArticle(Long currentUserId, Long articleId);

    /** 取消文章讚（idempotent：沒讚過也回成功） */
    void unlikeArticle(Long currentUserId, Long articleId);

    /** 讚一則留言 */
    void likeComment(Long currentUserId, Long commentId);

    /** 取消留言讚 */
    void unlikeComment(Long currentUserId, Long commentId);
}
