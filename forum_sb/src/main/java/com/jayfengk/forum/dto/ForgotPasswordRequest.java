package com.jayfengk.forum.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest {

    @NotBlank(message = "email 必填")
    @Email(message = "email 格式錯誤")
    private String email;
}
