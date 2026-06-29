package com.jayfengk.forum.common.constant;

/**
 * 安全性相關常數
 * ============================================================
 * 跟密碼加密 / token 生成有關的參數，集中在這。
 *
 * 為什麼不放 application.yml？
 *   這些值改動代表「演算法強度調整」，是 code 層級的安全決策，
 *   不該讓部署人員任意改。yml 適合放「環境差異」的設定（DB host / port），
 *   不適合放「需要 code review 才能改」的演算法參數。
 *
 * SQL Injection 防護策略
 *   本專案不存在 SQL 字串拼接，所有查詢均透過：
 *     1. MyBatis-Plus LambdaQueryWrapper（型別安全、自動 prepared statement）
 *     2. @Select 註解搭配 #{param}（MyBatis prepared statement 參數綁定）
 *   絕對禁止使用 ${param} 字串插值。
 */
public final class SecurityConstants {

    private SecurityConstants() {
        // 防止實例化
    }

    /**
     * BCrypt 加密強度
     * - 每 +1 加密時間翻倍：10 約 100ms、12 約 400ms、14 約 1.6s
     * - 10 對 2024+ 硬體已足夠抵抗暴力破解
     */
    public static final int BCRYPT_STRENGTH = 10;

    /**
     * 密碼重設 token 的隨機 byte 長度
     * - 32 bytes = 256 bits 熵，遠超 NIST 建議的 128 bits
     * - 即使知道 SHA-256 hash，反推明文是 2^256 級別工作量
     */
    public static final int PASSWORD_RESET_TOKEN_BYTES = 32;
}
