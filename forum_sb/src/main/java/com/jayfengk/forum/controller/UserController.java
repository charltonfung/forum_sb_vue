package com.jayfengk.forum.controller;

import com.jayfengk.forum.common.api.Result;
import com.jayfengk.forum.dto.ChangePasswordRequest;
import com.jayfengk.forum.dto.RequestEmailChangeRequest;
import com.jayfengk.forum.dto.UpdateProfileRequest;
import com.jayfengk.forum.dto.UserDto;
import com.jayfengk.forum.dto.VerifyEmailChangeRequest;
import com.jayfengk.forum.security.AuthUser;
import com.jayfengk.forum.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 使用者 API
 * ============================================================
 *   GET  /api/users/me                     取得當前登入者
 *   PUT  /api/users/me                     更新個人資料（名稱）
 *   POST /api/users/me/password            變更密碼（需驗舊密碼，會讓舊 JWT 失效）
 *   POST /api/users/me/email-change        申請變更 email（寄驗證信到新 email）
 *   POST /api/users/verify-email-change    驗證 email 變更（公開，不需登入）
 *
 * verify-email-change 為何不需要登入？
 *   使用者可能在另一台裝置 / 不同瀏覽器點信件連結，無 session。
 *   實際安全靠 token 本身（SHA-256 hash 比對 + 時效 + 一次性）。
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public Result<UserDto> me(@AuthenticationPrincipal AuthUser currentUser) {
        return Result.success(UserDto.from(currentUser.getUser()));
    }

    @PutMapping("/me")
    public Result<UserDto> updateProfile(@AuthenticationPrincipal AuthUser currentUser,
                                         @Valid @RequestBody UpdateProfileRequest req) {
        return Result.success(userService.updateProfile(currentUser.getUser().getId(), req));
    }

    @PostMapping("/me/password")
    public Result<Void> changePassword(@AuthenticationPrincipal AuthUser currentUser,
                                       @Valid @RequestBody ChangePasswordRequest req) {
        userService.changePassword(currentUser.getUser().getId(), req);
        return Result.success(null, "密碼已更新，請使用新密碼重新登入");
    }

    @PostMapping("/me/email-change")
    public Result<Void> requestEmailChange(@AuthenticationPrincipal AuthUser currentUser,
                                           @Valid @RequestBody RequestEmailChangeRequest req) {
        userService.requestEmailChange(currentUser.getUser().getId(), req);
        return Result.success(null, "驗證信已寄到新 email，請點連結完成變更");
    }

    @PostMapping("/verify-email-change")
    public Result<Void> verifyEmailChange(@Valid @RequestBody VerifyEmailChangeRequest req) {
        userService.verifyEmailChange(req.getToken());
        return Result.success(null, "Email 已變更，請使用新 email 重新登入");
    }
}
