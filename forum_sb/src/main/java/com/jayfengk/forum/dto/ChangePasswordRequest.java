package com.jayfengk.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 變更密碼請求
 * ============================================================
 * 業界標準：需要先輸入舊密碼才能改新密碼，
 * 避免「拿到 session 就一鍵改密碼」的風險。
 */
@Data
public class ChangePasswordRequest {

    @NotBlank(message = "目前密碼必填")
    private String currentPassword;

    @NotBlank(message = "新密碼必填")
    @Size(min = 8, message = "新密碼至少 8 字")
    private String newPassword;
}
