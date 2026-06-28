package com.jayfengk.forum.dto;

import com.jayfengk.forum.common.constant.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 建立/編輯文章的請求 DTO
 * ============================================================
 * 長度限制全部從 ValidationConstants 取，避免散落的魔術數字。
 */
@Data
public class ArticleRequest {

    @NotBlank(message = "title 必填")
    @Size(max = ValidationConstants.ARTICLE_TITLE_MAX, message = "title 最長 255 字")
    private String title;

    @NotBlank(message = "content 必填")
    @Size(min = ValidationConstants.ARTICLE_CONTENT_MIN, message = "content 至少 10 字")
    private String content;
}
