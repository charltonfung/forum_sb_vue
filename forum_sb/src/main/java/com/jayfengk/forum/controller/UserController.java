package com.jayfengk.forum.controller;

import com.jayfengk.forum.common.api.Result;
import com.jayfengk.forum.dto.UserDto;
import com.jayfengk.forum.security.AuthUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 使用者 API
 * ============================================================
 *   GET /api/users/me  取得當前登入者資料
 *
 * MVP 只有「查自己」。之後可加：
 *   - PUT /me              更新 name / email
 *   - PUT /me/password     更新密碼
 *   - DELETE /me           刪除帳號
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public Result<UserDto> me(@AuthenticationPrincipal AuthUser currentUser) {
        return Result.success(UserDto.from(currentUser.getUser()));
    }
}
