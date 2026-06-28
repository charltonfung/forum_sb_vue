package com.jayfengk.forum.common.exception;

import com.jayfengk.forum.common.api.ResultCode;
import lombok.Getter;

/**
 * 業務例外
 * ============================================================
 * Service 層 throw 這個例外，GlobalExceptionHandler 會接住並轉成 Result.failed(...)。
 * 用法：throw new ApiException(ResultCode.NOT_FOUND, "文章不存在")。
 *
 * extends RuntimeException：unchecked 例外，不用在 method 簽章寫 throws。
 */
@Getter
public class ApiException extends RuntimeException {

    /** 對應的業務狀態碼（決定 HTTP code 與預設訊息） */
    private final ResultCode resultCode;

    public ApiException(String message) {
        super(message);
        this.resultCode = ResultCode.FAILED;
    }

    public ApiException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }

    public ApiException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }
}
