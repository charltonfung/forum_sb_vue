package com.jayfengk.forum.dto;

import com.jayfengk.forum.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 對外回傳的使用者資料（不含密碼）
 * ============================================================
 * 為什麼不直接回傳 User entity？
 *   - entity 是「DB 對應」，DTO 是「對外契約」。兩個分開未來可獨立演化
 *   - 萬一 entity 加了敏感欄位（手機、地址），不會意外外洩
 */
@Data
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private LocalDateTime createdAt;

    public static UserDto from(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
