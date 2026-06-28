package com.jayfengk.forum.controller;

import com.jayfengk.forum.common.api.Result;
import com.jayfengk.forum.dto.CommentDto;
import com.jayfengk.forum.dto.CommentRequest;
import com.jayfengk.forum.security.AuthUser;
import com.jayfengk.forum.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 留言 API
 * ============================================================
 *   GET    /api/articles/{articleId}/comments   列出文章下所有留言（公開）
 *   POST   /api/articles/{articleId}/comments   新增留言（需登入）
 *   DELETE /api/comments/{commentId}            刪除留言（需登入，僅作者）
 *
 * 路徑設計刻意把「列表 / 新增」放 articles/{id}/comments 下，
 * 因為這兩個動作的語意「依附於某篇文章」；
 * 「刪除」用 /api/comments/{id} 因為刪除只需要 comment id 就夠。
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/articles/{articleId}/comments")
    public Result<List<CommentDto>> list(@AuthenticationPrincipal AuthUser currentUser,
                                         @PathVariable Long articleId) {
        Long uid = currentUser == null ? null : currentUser.getId();
        return Result.success(commentService.listByArticle(uid, articleId));
    }

    @PostMapping("/articles/{articleId}/comments")
    public Result<CommentDto> create(
            @AuthenticationPrincipal AuthUser currentUser,
            @PathVariable Long articleId,
            @Valid @RequestBody CommentRequest req) {
        return Result.success(
                commentService.create(currentUser.getId(), articleId, req.getContent()),
                "留言成功");
    }

    @DeleteMapping("/comments/{commentId}")
    public Result<Void> delete(
            @AuthenticationPrincipal AuthUser currentUser,
            @PathVariable Long commentId) {
        commentService.delete(currentUser.getId(), commentId);
        return Result.success(null, "留言已刪除");
    }
}
