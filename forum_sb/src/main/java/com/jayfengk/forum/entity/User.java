package com.jayfengk.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * users 表對應 entity
 * ============================================================
 * MyBatis-Plus 註解：
 *   @TableName  - 對應表名（如果類別名 = 表名駝峰可省略，這裡顯式寫清楚）
 *   @TableId    - 主鍵欄位 + 策略（AUTO = AUTO_INCREMENT）
 *
 * password 加 @JsonIgnore：entity 直接被 controller 回傳時不外洩 hash。
 * 雖然 controller 都已改用 DTO 隔開，這層是保險。
 *
 * 不在 entity 寫 relationship（hasMany articles）：MyBatis 不會自動處理，
 * 要關聯資料就在 service 層用 mapper 顯式查，明確、可控。
 */
@Data
@TableName("users")
public class User implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String email;

    @JsonIgnore // 不外洩密碼 hash
    private String password;

    private LocalDateTime emailVerifiedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
