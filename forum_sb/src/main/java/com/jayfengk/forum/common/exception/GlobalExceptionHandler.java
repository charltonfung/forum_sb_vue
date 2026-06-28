package com.jayfengk.forum.common.exception;

import com.jayfengk.forum.common.api.Result;
import com.jayfengk.forum.common.api.ResultCode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全域例外處理
 * ============================================================
 * 所有 Controller throw 出來的例外都會經過這裡。
 *
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 *   ↑ 自動把回傳的物件序列化成 JSON（不需要再加 @ResponseBody）
 *
 * 處理順序：Spring 會找「最精確匹配」的 @ExceptionHandler，
 *           找不到才往父類別找，最後才用 Exception.class 兜底。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 業務例外（service 主動丟出來的）
     */
    @ExceptionHandler(ApiException.class)
    public Result<Void> handleApiException(ApiException e) {
        return Result.failed(e.getResultCode(), e.getMessage());
    }

    /**
     * @Valid 校驗失敗（@RequestBody DTO 不通過時）
     * 把所有欄位錯誤合併成一句訊息：例如 "email: 格式錯誤; password: 至少 8 字"
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return Result.failed(ResultCode.VALIDATE_FAILED, msg);
    }

    /**
     * @Valid 校驗失敗（form 表單 / query param 不通過時）
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return Result.failed(ResultCode.VALIDATE_FAILED, msg);
    }

    /**
     * 帳密錯誤
     * 注意：BadCredentialsException 必須先於 AuthenticationException 處理
     *       （Spring 抓「最精確的」例外，子類別要寫在父類別前面）
     */
    @ExceptionHandler(BadCredentialsException.class)
    public Result<Void> handleBadCredentials(BadCredentialsException e) {
        return Result.failed(ResultCode.UNAUTHORIZED, "帳號或密碼錯誤");
    }

    /**
     * 其他驗證失敗（未登入 / token 過期）
     */
    @ExceptionHandler(AuthenticationException.class)
    public Result<Void> handleAuthenticationException(AuthenticationException e) {
        return Result.failed(ResultCode.UNAUTHORIZED);
    }

    /**
     * 權限不足（已登入但不能做這件事）
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> handleAccessDenied(AccessDeniedException e) {
        return Result.failed(ResultCode.FORBIDDEN);
    }

    /**
     * 兜底：其他所有沒被前面接走的例外
     * 開發階段方便除錯，會把例外訊息回傳給前端；正式環境應該關掉
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        e.printStackTrace(); // dev 階段：直接印 stack trace 方便除錯
        return Result.failed(ResultCode.FAILED, e.getMessage());
    }
}
