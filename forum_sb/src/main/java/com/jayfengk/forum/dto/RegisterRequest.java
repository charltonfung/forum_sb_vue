package com.jayfengk.forum.dto;

import com.jayfengk.forum.common.constant.ValidationConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 註冊請求 DTO
 * ============================================================
 * @NotBlank / @Email / @Size 是 Bean Validation 規則。Controller 加 @Valid
 * 後會自動檢查，不通過丟 MethodArgumentNotValidException，由
 * GlobalExceptionHandler 統一接住轉成 400 回應。
 *
 * 所有長度從 ValidationConstants 取，避免散落的魔術數字。
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "name 必填")
    @Size(max = ValidationConstants.USER_NAME_MAX, message = "name 最長 255 字")
    private String name;

    @NotBlank(message = "email 必填")
    @Email(message = "email 格式錯誤")
    @Size(max = ValidationConstants.USER_EMAIL_MAX)
    private String email;

    @NotBlank(message = "password 必填")
    @Size(min = ValidationConstants.PASSWORD_MIN, max = ValidationConstants.PASSWORD_MAX,
          message = "password 至少 8 字")
    private String password;
}
