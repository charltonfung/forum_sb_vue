package com.jayfengk.forum.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * articles 表對應 entity（含軟刪除）
 * ============================================================
 * @TableLogic 啟用軟刪除後：
 *   - mapper.deleteById(1) 會轉成 UPDATE articles SET deleted_at = NOW() WHERE id = 1
 *   - select 自動加 WHERE deleted_at IS NULL
 *
 * 全域設定（application.yml 的 logic-delete-field / logic-delete-value）
 * 已經告訴 MyBatis-Plus 「未刪 = null，已刪 = NOW()」，
 * 所以這裡只需要標一個 @TableLogic 就行。
 */
@Data
@TableName("articles")
public class Article implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;
    private String content;
    private String state;     // 'draft' / 'published'
    private Long userId;

    @TableLogic
    private LocalDateTime deletedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
