package com.jayfengk.forum.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 驗證 email 變更：使用者從信件連結點進來時帶 token，
 * 後端比對 token 通過後把 users.email 改成 new_email。
 *
 * 這支不需要登入，因為使用者可能在另一台裝置 / 另一個瀏覽器
 * 開信件連結，沒有 session。
 */
@Data
public class VerifyEmailChangeRequest {

    @NotBlank(message = "token 必填")
    private String token;
}
