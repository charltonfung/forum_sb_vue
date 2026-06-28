package com.jayfengk.forum.service;

/**
 * 郵件服務介面
 * ============================================================
 * MVP 只用到一支：寄密碼重設信。
 * 介面 + impl 分離是 Spring 慣例（之後可換成不同實作 / 方便 mock 測試）。
 */
public interface MailService {

    /**
     * 寄密碼重設信
     * @param toEmail    收件人
     * @param resetUrl   完整的重設連結（含 token + email query param）
     */
    void sendPasswordResetMail(String toEmail, String resetUrl);
}
