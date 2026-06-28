package com.jayfengk.forum.dto;

import com.jayfengk.forum.common.constant.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增留言請求 DTO
 * ============================================================
 * 留言長度上限取 ValidationConstants.COMMENT_CONTENT_MAX（夠長一段話，
 * 又不會讓使用者貼整篇小說）。
 */
@Data
public class CommentRequest {

    @NotBlank(message = "content 必填")
    @Size(max = ValidationConstants.COMMENT_CONTENT_MAX, message = "content 最長 2000 字")
    private String content;
}
