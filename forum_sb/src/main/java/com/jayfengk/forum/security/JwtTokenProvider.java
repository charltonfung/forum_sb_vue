package com.jayfengk.forum.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 產生 / 解析
 * ============================================================
 * 用法：
 *   String token = jwtTokenProvider.createToken(userId, email);
 *   Long uid = jwtTokenProvider.getUserId(token);
 *
 * 為什麼把 userId 放 subject、email 放 claim？
 *   - subject 是 JWT 標準欄位，慣例放「使用者唯一識別」
 *   - 其他資料用自定 claim，避免 token 太大
 */
@Component
public class JwtTokenProvider {

    private final JwtProperties props;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties props) {
        this.props = props;
        // HS256 簽章金鑰必須 ≥ 32 bytes，這裡用 UTF-8 編碼後丟給 Keys.hmacShaKeyFor()
        this.secretKey = Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 簽發 token
     * @param userId 使用者 id（會放進 subject）
     * @param email  使用者 email（會放進 claim）
     */
    public String createToken(Long userId, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + props.getExpirationSeconds() * 1000L);

        return Jwts.builder()
                .subject(String.valueOf(userId))     // sub: userId
                .claim("email", email)               // 自定 claim
                .issuedAt(now)                       // iat
                .expiration(expiry)                  // exp
                .signWith(secretKey)                 // HS256 簽章
                .compact();
    }

    /**
     * 從 token 取出 userId
     * @return 解析失敗（過期 / 簽章錯）回傳 null
     */
    public Long getUserId(String token) {
        Claims claims = parse(token);
        if (claims == null) return null;
        try {
            return Long.parseLong(claims.getSubject());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String getEmail(String token) {
        Claims claims = parse(token);
        return claims == null ? null : claims.get("email", String.class);
    }

    /**
     * 取 token 的 issued-at（簽發時間），轉成系統預設時區的 LocalDateTime。
     * JwtAuthenticationFilter 用這個跟 user.credentialsChangedAt 比，
     * 若 iat < credentialsChangedAt 就視為失效 token。
     * 解析失敗回 null。
     */
    public java.time.LocalDateTime getIssuedAt(String token) {
        Claims claims = parse(token);
        if (claims == null || claims.getIssuedAt() == null) return null;
        return java.time.LocalDateTime.ofInstant(
                claims.getIssuedAt().toInstant(),
                java.time.ZoneId.systemDefault());
    }

    public boolean validate(String token) {
        return parse(token) != null;
    }

    /**
     * 解析 token，失敗回傳 null（不丟例外，呼叫端自己判 null）
     */
    private Claims parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
