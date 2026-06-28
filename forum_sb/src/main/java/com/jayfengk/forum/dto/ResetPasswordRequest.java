package com.jayfengk.forum.dto;

import com.jayfengk.forum.common.constant.ValidationConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank @Email
    private String email;

    @NotBlank(message = "token 必填")
    private String token;

    @NotBlank(message = "password 必填")
    @Size(min = ValidationConstants.PASSWORD_MIN, max = ValidationConstants.PASSWORD_MAX,
          message = "password 至少 8 字")
    private String password;
}
