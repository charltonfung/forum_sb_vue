package com.jayfengk.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新個人資料（目前只開放改名稱；email 走另一支驗證流程）
 */
@Data
public class UpdateProfileRequest {

    @NotBlank(message = "名稱必填")
    @Size(max = 255, message = "名稱過長")
    private String name;
}
