package com.jayfengk.forum.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登入請求 DTO
 */
@Data
public class LoginRequest {

    @NotBlank(message = "email 必填")
    @Email(message = "email 格式錯誤")
    private String email;

    @NotBlank(message = "password 必填")
    private String password;
}
