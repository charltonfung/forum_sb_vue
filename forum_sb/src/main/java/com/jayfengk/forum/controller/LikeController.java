package com.jayfengk.forum.controller;

import com.jayfengk.forum.common.api.Result;
import com.jayfengk.forum.security.AuthUser;
import com.jayfengk.forum.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 點讚 API（全部需登入）
 * ============================================================
 *   POST   /api/articles/{id}/like   讚文章
 *   DELETE /api/articles/{id}/like   取消文章讚
 *   POST   /api/comments/{id}/like   讚留言
 *   DELETE /api/comments/{id}/like   取消留言讚
 *
 * REST 風格：POST = 建立讚的關係，DELETE = 移除。
 * 都是 idempotent — 重複呼叫不會報錯，前端可以放心。
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/articles/{articleId}/like")
    public Result<Void> likeArticle(@AuthenticationPrincipal AuthUser currentUser,
                                    @PathVariable Long articleId) {
        likeService.likeArticle(currentUser.getId(), articleId);
        return Result.success();
    }

    @DeleteMapping("/articles/{articleId}/like")
    public Result<Void> unlikeArticle(@AuthenticationPrincipal AuthUser currentUser,
                                      @PathVariable Long articleId) {
        likeService.unlikeArticle(currentUser.getId(), articleId);
        return Result.success();
    }

    @PostMapping("/comments/{commentId}/like")
    public Result<Void> likeComment(@AuthenticationPrincipal AuthUser currentUser,
                                    @PathVariable Long commentId) {
        likeService.likeComment(currentUser.getId(), commentId);
        return Result.success();
    }

    @DeleteMapping("/comments/{commentId}/like")
    public Result<Void> unlikeComment(@AuthenticationPrincipal AuthUser currentUser,
                                      @PathVariable Long commentId) {
        likeService.unlikeComment(currentUser.getId(), commentId);
        return Result.success();
    }
}
