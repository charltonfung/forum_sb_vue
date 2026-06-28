package com.jayfengk.forum.controller;

import com.jayfengk.forum.common.api.Result;
import com.jayfengk.forum.dto.ForgotPasswordRequest;
import com.jayfengk.forum.dto.LoginRequest;
import com.jayfengk.forum.dto.LoginResponse;
import com.jayfengk.forum.dto.RegisterRequest;
import com.jayfengk.forum.dto.ResetPasswordRequest;
import com.jayfengk.forum.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 驗證相關 API（公開端點，SecurityConfig 已 permitAll）
 * ============================================================
 *   POST /api/auth/register         註冊
 *   POST /api/auth/login            登入
 *   POST /api/auth/forgot-password  寄密碼重設信
 *   POST /api/auth/reset-password   用 token 重設密碼
 *
 * 沒有 /logout — JWT 無狀態，前端把 token 從 localStorage 刪掉就等於登出。
 * 要做 server-side logout 需 Redis 黑名單，後期再加（跟 e-commerce roadmap 一致）。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest req) {
        return Result.success(authService.register(req));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return Result.success(authService.login(req));
    }

    @PostMapping("/forgot-password")
    public Result<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        authService.forgotPassword(req.getEmail());
        // 無論 email 是否存在，一律回成功（防帳號列舉）
        return Result.success(null, "若信箱存在，重設信件已寄出");
    }

    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req.getEmail(), req.getToken(), req.getPassword());
        return Result.success(null, "密碼已更新，請使用新密碼登入");
    }
}
