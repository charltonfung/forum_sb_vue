package com.jayfengk.forum.common.api;

import lombok.Data;

import java.io.Serializable;

/**
 * 統一 API 回應格式
 * ============================================================
 * 所有 Controller 回傳的 JSON 都會用這個包起來：
 *   { "code": 200, "message": "操作成功", "data": {...} }
 *
 * 為什麼要 implements Serializable？
 *   Spring 把 bean 放進 Redis / session / 跨服務時會序列化，
 *   實作這個介面是 Java 慣例，避免之後踩坑。
 *
 * @param <T> data 欄位的實際型別（泛型）
 */
@Data
public class Result<T> implements Serializable {

    private int code;
    private String message;
    private T data;

    protected Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /** 成功 + 帶資料：Result.success(article) */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /** 成功 + 自訂訊息：Result.success(null, "文章已刪除") */
    public static <T> Result<T> success(T data, String message) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /** 成功不帶資料 */
    public static <T> Result<T> success() {
        return success(null);
    }

    /** 失敗 + 預設訊息 */
    public static <T> Result<T> failed(ResultCode code) {
        return new Result<>(code.getCode(), code.getMessage(), null);
    }

    /** 失敗 + 自訂訊息 */
    public static <T> Result<T> failed(ResultCode code, String message) {
        return new Result<>(code.getCode(), message, null);
    }

    public static <T> Result<T> failed(String message) {
        return new Result<>(ResultCode.FAILED.getCode(), message, null);
    }
}
