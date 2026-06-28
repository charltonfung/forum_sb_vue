package com.jayfengk.forum.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 設定（對應 application.yml 裡的 forum.jwt.*）
 * ============================================================
 * @ConfigurationProperties = 把 yml 設定自動綁到 Java 物件，
 * 比起 @Value("${forum.jwt.secret}") 一個一個讀，集中綁進物件比較好維護、
 * 也可以做 Bean Validation 校驗。
 *
 * 用法：在需要用的類別注入 JwtProperties，呼叫 props.getSecret() 即可。
 */
@Data
@Component
@ConfigurationProperties(prefix = "forum.jwt")
public class JwtProperties {

    /** HS256 簽章密鑰（至少 32 bytes） */
    private String secret;

    /** Token 有效時間（秒） */
    private long expirationSeconds;

    /** HTTP header 名稱，預設 Authorization */
    private String header;

    /** HTTP header 值的 prefix，預設 "Bearer " */
    private String prefix;
}
