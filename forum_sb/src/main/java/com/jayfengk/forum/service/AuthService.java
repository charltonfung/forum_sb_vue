package com.jayfengk.forum.service;

import com.jayfengk.forum.dto.LoginRequest;
import com.jayfengk.forum.dto.LoginResponse;
import com.jayfengk.forum.dto.RegisterRequest;

public interface AuthService {

    /** 註冊：成功直接回傳 JWT（註冊後立即登入） */
    LoginResponse register(RegisterRequest req);

    /** 登入：成功回 JWT */
    LoginResponse login(LoginRequest req);

    /** 寄密碼重設信件（即使 email 不存在也回成功，避免帳號列舉） */
    void forgotPassword(String email);

    /** 用 token 重設密碼 */
    void resetPassword(String email, String token, String newPassword);
}
