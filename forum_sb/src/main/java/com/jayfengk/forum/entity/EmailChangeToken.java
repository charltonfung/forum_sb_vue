package com.jayfengk.forum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * email_change_tokens 表對應 entity
 * ============================================================
 * 一個 user_id 對應一筆紀錄（PK），第二次申請會覆蓋第一次的。
 * token 欄位存 SHA-256 hash（不存明文），明文只透過 email 寄給使用者。
 * new_email = 待生效的新 email；verify 通過後才會真正寫到 users.email。
 */
@Data
@TableName("email_change_tokens")
public class EmailChangeToken implements Serializable {

    @TableId(type = IdType.INPUT)
    private Long userId;

    private String newEmail;
    private String token;
    private LocalDateTime createdAt;
}
