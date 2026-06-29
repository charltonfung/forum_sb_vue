package com.jayfengk.forum.service.impl;

import com.jayfengk.forum.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 郵件實作（使用 Spring Mail 的 JavaMailSender）
 * ============================================================
 * 開發環境建議搭配 MailHog：
 *   - 安裝：scoop install mailhog  或  docker run -p 1025:1025 -p 8025:8025 mailhog/mailhog
 *   - 啟動後 SMTP 在 :1025、Web UI 在 http://localhost:8025
 *   - 所有寄出的信都會被攔下來，可以在 web UI 看到內容（不會真的寄出）
 */
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Value("${forum.password-reset.mail-from}")
    private String mailFrom;

    @Override
    public void sendPasswordResetMail(String toEmail, String resetUrl) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(mailFrom);
        msg.setTo(toEmail);
        msg.setSubject("【Forum】密碼重設通知");
        msg.setText("""
                您好，

                我們收到您的密碼重設請求。請點以下連結重設密碼（連結 60 分鐘內有效）：

                %s

                如果不是您本人操作，請忽略此信件。

                — Forum
                """.formatted(resetUrl));
        mailSender.send(msg);
    }

    @Override
    public void sendEmailChangeVerificationMail(String toNewEmail, String verifyUrl) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(mailFrom);
        msg.setTo(toNewEmail);
        msg.setSubject("【Forum】Email 變更驗證");
        msg.setText("""
                您好，

                我們收到將帳號 email 變更為此信箱的請求。請點以下連結完成驗證（連結 60 分鐘內有效）：

                %s

                點擊後，您的 Forum 帳號 email 將會變更為這個信箱，舊登入狀態會失效，請用新 email 重新登入。

                如果不是您本人操作，請忽略此信件。

                — Forum
                """.formatted(verifyUrl));
        mailSender.send(msg);
    }
}
