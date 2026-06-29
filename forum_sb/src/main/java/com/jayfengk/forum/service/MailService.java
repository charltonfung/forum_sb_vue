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

    /**
     * 寄變更 email 驗證信（寄到「新 email」，使用者點連結才會生效）
     * @param toNewEmail  新 email 收件人
     * @param verifyUrl   完整的驗證連結（含 token query param）
     */
    void sendEmailChangeVerificationMail(String toNewEmail, String verifyUrl);
}
