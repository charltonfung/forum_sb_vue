package com.jayfengk.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jayfengk.forum.common.api.ResultCode;
import com.jayfengk.forum.common.constant.SecurityConstants;
import com.jayfengk.forum.common.exception.ApiException;
import com.jayfengk.forum.dto.LoginRequest;
import com.jayfengk.forum.dto.LoginResponse;
import com.jayfengk.forum.dto.RegisterRequest;
import com.jayfengk.forum.dto.UserDto;
import com.jayfengk.forum.entity.PasswordResetToken;
import com.jayfengk.forum.entity.User;
import com.jayfengk.forum.mapper.PasswordResetTokenMapper;
import com.jayfengk.forum.mapper.UserMapper;
import com.jayfengk.forum.security.JwtProperties;
import com.jayfengk.forum.security.JwtTokenProvider;
import com.jayfengk.forum.service.AuthService;
import com.jayfengk.forum.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

/**
 * AuthService 實作
 * ============================================================
 *   1. 註冊（檢查 email 唯一 → 加密密碼 → 寫 DB → 簽 JWT）
 *   2. 登入（委派給 Spring Security 的 AuthenticationManager → 簽 JWT）
 *   3. 寄送密碼重設信（防帳號列舉：email 不存在也回成功）
 *   4. 用 token 重設密碼（SHA-256 hash 比對 + 常數時間 + 一次性）
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordResetTokenMapper resetTokenMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final JwtProperties jwtProps;
    private final MailService mailService;

    @Value("${forum.password-reset.expiration-minutes}")
    private long resetExpirationMinutes;

    @Value("${forum.password-reset.frontend-reset-url}")
    private String frontendResetUrl;

    // ============================================================
    // 註冊
    // ============================================================
    @Override
    @Transactional
    public LoginResponse register(RegisterRequest req) {
        // 1. 檢查 email 是否已被註冊
        Long emailCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getEmail, req.getEmail()));
        if (emailCount != null && emailCount > 0) {
            throw new ApiException(ResultCode.VALIDATE_FAILED, "email 已被註冊");
        }

        // 2. 檢查名稱不能跟別人撞
        Long nameCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getName, req.getName()));
        if (nameCount != null && nameCount > 0) {
            throw new ApiException(ResultCode.VALIDATE_FAILED, "此名稱已被使用");
        }

        // 3. 寫入 user
        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        userMapper.insert(user);

        // 4. 簽 token 直接回（註冊後立即登入）
        return issueToken(user);
    }

    // ============================================================
    // 登入
    // ============================================================
    @Override
    public LoginResponse login(LoginRequest req) {
        // 把驗證工作丟給 Spring Security 的 AuthenticationManager
        // 它會自動呼叫 UserDetailsServiceImpl 查 user，再用 PasswordEncoder 比對密碼
        // 失敗會丟 BadCredentialsException，由 GlobalExceptionHandler 接住
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));

        // 驗證成功，從 UserDetails 取出 User 簽 token
        com.jayfengk.forum.security.AuthUser authUser =
                (com.jayfengk.forum.security.AuthUser) auth.getPrincipal();
        return issueToken(authUser.getUser());
    }

    // ============================================================
    // 寄送密碼重設信
    // ============================================================
    @Override
    @Transactional
    public void forgotPassword(String email) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getEmail, email));

        // 安全考量：即使 email 不存在也直接 return，不告訴攻擊者「這個 email 不存在」
        if (user == null) {
            return;
        }

        // 1. 生 token（明文 32 bytes 隨機 → Base64URL）
        String plainToken = generateRandomToken();
        // 2. DB 只存 hash，避免 DB 外洩時 token 直接被用
        String tokenHash = sha256(plainToken);

        // 3. 一個 email 只保留一筆有效 token（先刪舊的）
        resetTokenMapper.deleteById(email);
        PasswordResetToken record = new PasswordResetToken();
        record.setEmail(email);
        record.setToken(tokenHash);
        record.setCreatedAt(LocalDateTime.now());
        resetTokenMapper.insert(record);

        // 4. 寄信，連結指向前端的 reset 頁
        String resetUrl = "%s?token=%s&email=%s".formatted(
                frontendResetUrl, plainToken, java.net.URLEncoder.encode(email, StandardCharsets.UTF_8));
        mailService.sendPasswordResetMail(email, resetUrl);
    }

    // ============================================================
    // 用 token 重設密碼
    // ============================================================
    @Override
    @Transactional
    public void resetPassword(String email, String plainToken, String newPassword) {
        PasswordResetToken record = resetTokenMapper.selectById(email);
        if (record == null) {
            throw new ApiException(ResultCode.VALIDATE_FAILED, "重設連結無效或已過期");
        }

        // 1. 檢查時效
        if (record.getCreatedAt().plus(Duration.ofMinutes(resetExpirationMinutes))
                .isBefore(LocalDateTime.now())) {
            resetTokenMapper.deleteById(email);
            throw new ApiException(ResultCode.VALIDATE_FAILED, "重設連結已過期");
        }

        // 2. 比對 token hash
        String inputHash = sha256(plainToken);
        if (!constantTimeEquals(inputHash, record.getToken())) {
            throw new ApiException(ResultCode.VALIDATE_FAILED, "重設連結無效");
        }

        // 3. 更新密碼
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (user == null) {
            throw new ApiException(ResultCode.NOT_FOUND, "使用者不存在");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);

        // 4. 用完即刪
        resetTokenMapper.deleteById(email);
    }

    // ============================================================
    // 私有：JWT 簽發 + token 生成 helper
    // ============================================================
    private LoginResponse issueToken(User user) {
        String token = tokenProvider.createToken(user.getId(), user.getEmail());
        return new LoginResponse(
                token,
                jwtProps.getPrefix().trim(),
                jwtProps.getExpirationSeconds(),
                UserDto.from(user));
    }

    /** 生隨機 token，編成 URL-safe Base64。byte 長度從 SecurityConstants 取（預設 32 = 256-bit）。 */
    private String generateRandomToken() {
        byte[] bytes = new byte[SecurityConstants.PASSWORD_RESET_TOKEN_BYTES];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** SHA-256 hash → hex string */
    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** 等長時間比對字串，避免 timing attack */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return false;
        int diff = 0;
        for (int i = 0; i < a.length(); i++) {
            diff |= a.charAt(i) ^ b.charAt(i);
        }
        return diff == 0;
    }
}
