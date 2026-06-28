package com.jayfengk.forum.common.api;

import lombok.Getter;

/**
 * 業務狀態碼列舉
 * ============================================================
 * 集中管理所有 Result 用到的 code/message 配對。
 * 跟 e-commerce 的 ResultCode 風格一致（兩個專案以後好維護）。
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAILED(500, "操作失敗"),
    VALIDATE_FAILED(400, "參數檢驗失敗"),
    UNAUTHORIZED(401, "未登入或 token 已過期"),
    FORBIDDEN(403, "沒有相關權限"),
    NOT_FOUND(404, "資源不存在");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
