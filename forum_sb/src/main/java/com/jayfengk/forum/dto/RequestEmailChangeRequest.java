package com.jayfengk.forum.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 申請變更 email：使用者輸入新 email + 目前密碼，
 * 後端寄驗證信到「新 email」，使用者點連結才會真正生效。
 *
 * 為什麼要驗目前密碼：
 *   - 防止「session 被偷後攻擊者把帳號 email 改成自己的」
 *   - 跟改密碼一致的安全標準
 */
@Data
public class RequestEmailChangeRequest {

    @NotBlank(message = "新 email 必填")
    @Email(message = "email 格式錯誤")
    private String newEmail;

    @NotBlank(message = "目前密碼必填")
    private String currentPassword;
}
