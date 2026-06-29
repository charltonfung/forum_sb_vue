package com.jayfengk.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jayfengk.forum.common.api.ResultCode;
import com.jayfengk.forum.common.constant.SecurityConstants;
import com.jayfengk.forum.common.exception.ApiException;
import com.jayfengk.forum.dto.ChangePasswordRequest;
import com.jayfengk.forum.dto.RequestEmailChangeRequest;
import com.jayfengk.forum.dto.UpdateProfileRequest;
import com.jayfengk.forum.dto.UserDto;
import com.jayfengk.forum.entity.EmailChangeToken;
import com.jayfengk.forum.entity.User;
import com.jayfengk.forum.mapper.EmailChangeTokenMapper;
import com.jayfengk.forum.mapper.UserMapper;
import com.jayfengk.forum.service.MailService;
import com.jayfengk.forum.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
 * UserService 實作
 * ============================================================
 * 安全規範統一：
 *   - 改密碼 / 申請改 email 都要驗舊密碼（被偷 session 也不能一鍵改）
 *   - 改完密碼 / email 都會推 credentialsChangedAt → 舊 JWT 失效
 *   - email 變更採「先寄信到新 email 驗證、點連結才生效」流程，
 *     防止把帳號劫持改成攻擊者的 email
 *
 * token 演算法（同 AuthServiceImpl 的密碼重設流程）：
 *   - SecureRandom 生 32 bytes → Base64URL 編成明文 token
 *   - DB 只存 SHA-256 hash，明文只透過 email 寄給使用者
 *   - 一個使用者只保留一筆有效 token，再次申請會覆蓋
 *   - 用完即刪（一次性）
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final EmailChangeTokenMapper emailChangeTokenMapper;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    @Value("${forum.email-change.expiration-minutes}")
    private long emailChangeExpirationMinutes;

    @Value("${forum.email-change.frontend-verify-url}")
    private String frontendVerifyUrl;

    // ============================================================
    // 改名稱
    // ============================================================
    @Override
    @Transactional
    public UserDto updateProfile(Long currentUserId, UpdateProfileRequest req) {
        User user = userMapper.selectById(currentUserId);
        if (user == null) {
            throw new ApiException(ResultCode.NOT_FOUND, "使用者不存在");
        }
        // 名稱沒變就跳過檢查（避免「改成跟自己一樣」也被擋）
        if (!req.getName().equals(user.getName())) {
            Long taken = userMapper.selectCount(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getName, req.getName())
                            .ne(User::getId, currentUserId));   // 排除自己
            if (taken != null && taken > 0) {
                throw new ApiException(ResultCode.VALIDATE_FAILED, "此名稱已被使用");
            }
            user.setName(req.getName());
            userMapper.updateById(user);
        }
        return UserDto.from(user);
    }

    // ============================================================
    // 改密碼
    // ============================================================
    @Override
    @Transactional
    public void changePassword(Long currentUserId, ChangePasswordRequest req) {
        User user = userMapper.selectById(currentUserId);
        if (user == null) {
            throw new ApiException(ResultCode.NOT_FOUND, "使用者不存在");
        }
        // 驗舊密碼
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            throw new ApiException(ResultCode.VALIDATE_FAILED, "目前密碼不正確");
        }
        // 新密碼不能跟舊的一樣
        if (passwordEncoder.matches(req.getNewPassword(), user.getPassword())) {
            throw new ApiException(ResultCode.VALIDATE_FAILED, "新密碼不能跟目前密碼相同");
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        user.setCredentialsChangedAt(LocalDateTime.now()); // ← 讓舊 JWT 失效
        userMapper.updateById(user);
    }

    // ============================================================
    // 申請改 email（寄驗證信到「新 email」）
    // ============================================================
    @Override
    @Transactional
    public void requestEmailChange(Long currentUserId, RequestEmailChangeRequest req) {
        User user = userMapper.selectById(currentUserId);
        if (user == null) {
            throw new ApiException(ResultCode.NOT_FOUND, "使用者不存在");
        }
        // 驗舊密碼
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            throw new ApiException(ResultCode.VALIDATE_FAILED, "目前密碼不正確");
        }
        // 新 email 不能跟現有相同
        if (req.getNewEmail().equalsIgnoreCase(user.getEmail())) {
            throw new ApiException(ResultCode.VALIDATE_FAILED, "新 email 跟目前一樣");
        }
        // 新 email 不能被其他人用
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getEmail, req.getNewEmail()));
        if (count != null && count > 0) {
            throw new ApiException(ResultCode.VALIDATE_FAILED, "此 email 已被其他帳號使用");
        }

        // 生 token + 存 hash（一個 user 只留一筆有效）
        String plainToken = generateRandomToken();
        String tokenHash = sha256(plainToken);

        emailChangeTokenMapper.deleteById(currentUserId);
        EmailChangeToken record = new EmailChangeToken();
        record.setUserId(currentUserId);
        record.setNewEmail(req.getNewEmail());
        record.setToken(tokenHash);
        record.setCreatedAt(LocalDateTime.now());
        emailChangeTokenMapper.insert(record);

        // 寄信到「新 email」，連結指向前端的 verify 頁
        String verifyUrl = "%s?token=%s".formatted(frontendVerifyUrl, plainToken);
        mailService.sendEmailChangeVerificationMail(req.getNewEmail(), verifyUrl);
    }

    // ============================================================
    // 驗證 email 變更
    // ============================================================
    @Override
    @Transactional
    public void verifyEmailChange(String plainToken) {
        // 比對 hash 找對應的 token 紀錄
        String inputHash = sha256(plainToken);
        EmailChangeToken record = emailChangeTokenMapper.selectOne(
                new LambdaQueryWrapper<EmailChangeToken>().eq(EmailChangeToken::getToken, inputHash));
        if (record == null) {
            throw new ApiException(ResultCode.VALIDATE_FAILED, "驗證連結無效或已過期");
        }

        // 時效檢查
        if (record.getCreatedAt().plus(Duration.ofMinutes(emailChangeExpirationMinutes))
                .isBefore(LocalDateTime.now())) {
            emailChangeTokenMapper.deleteById(record.getUserId());
            throw new ApiException(ResultCode.VALIDATE_FAILED, "驗證連結已過期，請重新申請");
        }

        // 再次檢查 new_email 沒被搶走（rare race condition：申請後在 verify 前剛好有人註冊同 email）
        Long taken = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getEmail, record.getNewEmail()));
        if (taken != null && taken > 0) {
            emailChangeTokenMapper.deleteById(record.getUserId());
            throw new ApiException(ResultCode.VALIDATE_FAILED, "此 email 已被其他帳號使用，請重新申請其他 email");
        }

        User user = userMapper.selectById(record.getUserId());
        if (user == null) {
            throw new ApiException(ResultCode.NOT_FOUND, "使用者不存在");
        }
        user.setEmail(record.getNewEmail());
        user.setCredentialsChangedAt(LocalDateTime.now()); // ← 讓舊 JWT 失效
        userMapper.updateById(user);

        emailChangeTokenMapper.deleteById(record.getUserId()); // 用完即刪
    }

    // ============================================================
    // 私有 helpers（跟 AuthServiceImpl 的 token 流程一致）
    // ============================================================

    private String generateRandomToken() {
        byte[] bytes = new byte[SecurityConstants.PASSWORD_RESET_TOKEN_BYTES];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
