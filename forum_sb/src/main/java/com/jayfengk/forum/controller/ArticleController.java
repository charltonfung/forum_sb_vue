package com.jayfengk.forum.controller;

import com.jayfengk.forum.common.api.Result;
import com.jayfengk.forum.common.constant.PaginationConstants;
import com.jayfengk.forum.dto.ArticleDto;
import com.jayfengk.forum.dto.ArticleRequest;
import com.jayfengk.forum.dto.PageResult;
import com.jayfengk.forum.security.AuthUser;
import com.jayfengk.forum.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文章 API
 * ============================================================
 *   GET    /api/articles?page=1&pageSize=10   列表（公開）
 *   GET    /api/articles/{id}                 單篇（公開）
 *   POST   /api/articles                      建立（需登入）
 *   PUT    /api/articles/{id}                 更新（需登入，僅作者）
 *   DELETE /api/articles/{id}                 刪除（需登入，僅作者）
 *
 * @AuthenticationPrincipal：
 *   Spring Security 從 SecurityContext 自動取出當前登入者（AuthUser 物件）。
 *   未登入時是 null，但這 controller 的非公開端點 SecurityConfig 已擋掉未登入，
 *   所以走到方法內時 currentUser 一定非 null。
 */
@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping
    public Result<PageResult<ArticleDto>> list(
            @AuthenticationPrincipal AuthUser currentUser,   // 公開端點，未登入時為 null
            @RequestParam(required = false) String q,        // 模糊搜尋標題，可選
            @RequestParam(required = false) Long userId,     // 只看某使用者的文章，可選
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE) long page,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE) long pageSize) {
        Long uid = currentUser == null ? null : currentUser.getId();
        // 用 Math.min 卡住上限，防 ?pageSize=999999 把 DB / 記憶體撈爆
        long safePageSize = Math.min(pageSize, PaginationConstants.MAX_PAGE_SIZE);
        return Result.success(articleService.list(uid, q, userId, page, safePageSize));
    }

    @GetMapping("/{id}")
    public Result<ArticleDto> get(@AuthenticationPrincipal AuthUser currentUser,
                                  @PathVariable Long id) {
        Long uid = currentUser == null ? null : currentUser.getId();
        return Result.success(articleService.get(uid, id));
    }

    @PostMapping
    public Result<ArticleDto> create(
            @AuthenticationPrincipal AuthUser currentUser,
            @Valid @RequestBody ArticleRequest req) {
        return Result.success(articleService.create(currentUser.getId(), req), "文章發表成功");
    }

    @PutMapping("/{id}")
    public Result<ArticleDto> update(
            @AuthenticationPrincipal AuthUser currentUser,
            @PathVariable Long id,
            @Valid @RequestBody ArticleRequest req) {
        return Result.success(articleService.update(currentUser.getId(), id, req), "文章更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @AuthenticationPrincipal AuthUser currentUser,
            @PathVariable Long id) {
        articleService.delete(currentUser.getId(), id);
        return Result.success(null, "文章已刪除");
    }
}
