package com.jayfengk.forum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登入成功回應
 * ============================================================
 * 回傳 token + 過期秒數 + 使用者基本資料（前端拿來顯示登入狀態）。
 */
@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String tokenType;        // "Bearer"
    private long expiresIn;          // 秒
    private UserDto user;
}
