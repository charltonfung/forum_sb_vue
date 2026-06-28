package com.jayfengk.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * password_reset_tokens 表對應 entity
 * ============================================================
 * 一個 email 對應一筆紀錄，email 即主鍵（IdType.INPUT = 不自動生成 id）。
 * token 欄位存 SHA-256 hash（不存明文），明文只透過 email 寄給使用者。
 */
@Data
@TableName("password_reset_tokens")
public class PasswordResetToken implements Serializable {

    @TableId(type = IdType.INPUT)
    private String email;

    private String token;
    private LocalDateTime createdAt;
}
