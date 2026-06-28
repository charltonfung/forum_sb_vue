package com.jayfengk.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jayfengk.forum.common.api.ResultCode;
import com.jayfengk.forum.common.exception.ApiException;
import com.jayfengk.forum.entity.ArticleLike;
import com.jayfengk.forum.entity.CommentLike;
import com.jayfengk.forum.mapper.ArticleLikeMapper;
import com.jayfengk.forum.mapper.ArticleMapper;
import com.jayfengk.forum.mapper.CommentLikeMapper;
import com.jayfengk.forum.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * LikeService 實作
 * ============================================================
 * idempotent 策略：
 *   - like：先 insert，如果 UNIQUE KEY 撞了（DuplicateKeyException）就忽略。
 *           比「先 SELECT 再 INSERT」少一次 query、又沒 race condition。
 *   - unlike：直接 DELETE，影響 0 列也算成功。
 *
 * 為什麼讚之前要先檢查文章 / 留言存在？
 *   FK 會擋掉「不存在的 article_id」插入，但 FK 失敗的錯誤訊息很醜
 *   （MySQL 1452）。先 selectById 一下，回應 404 比較清楚。
 */
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements com.jayfengk.forum.service.LikeService {

    private final ArticleLikeMapper articleLikeMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final ArticleMapper articleMapper;
    private final CommentMapper commentMapper;

    // ============================================================
    // Article
    // ============================================================
    @Override
    @Transactional
    public void likeArticle(Long currentUserId, Long articleId) {
        if (articleMapper.selectById(articleId) == null) {
            throw new ApiException(ResultCode.NOT_FOUND, "文章不存在");
        }
        ArticleLike like = new ArticleLike();
        like.setArticleId(articleId);
        like.setUserId(currentUserId);
        try {
            articleLikeMapper.insert(like);
        } catch (DuplicateKeyException ignored) {
            // 已經讚過，當作成功（idempotent）
        }
    }

    @Override
    @Transactional
    public void unlikeArticle(Long currentUserId, Long articleId) {
        articleLikeMapper.delete(
                new LambdaQueryWrapper<ArticleLike>()
                        .eq(ArticleLike::getArticleId, articleId)
                        .eq(ArticleLike::getUserId, currentUserId));
    }

    // ============================================================
    // Comment
    // ============================================================
    @Override
    @Transactional
    public void likeComment(Long currentUserId, Long commentId) {
        if (commentMapper.selectById(commentId) == null) {
            throw new ApiException(ResultCode.NOT_FOUND, "留言不存在");
        }
        CommentLike like = new CommentLike();
        like.setCommentId(commentId);
        like.setUserId(currentUserId);
        try {
            commentLikeMapper.insert(like);
        } catch (DuplicateKeyException ignored) {
        }
    }

    @Override
    @Transactional
    public void unlikeComment(Long currentUserId, Long commentId) {
        commentLikeMapper.delete(
                new LambdaQueryWrapper<CommentLike>()
                        .eq(CommentLike::getCommentId, commentId)
                        .eq(CommentLike::getUserId, currentUserId));
    }
}
