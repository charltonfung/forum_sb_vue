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

    /**
     * 帳密變更時間（改密碼 / 改 email 時更新）。
     * JWT filter 驗證時若 token.iat < credentialsChangedAt 視為失效，強制重新登入。
     * 加 @JsonIgnore 避免外洩到前端（內部安全欄位）。
     */
    @JsonIgnore
    private LocalDateTime credentialsChangedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
