package com.jayfengk.forum.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayfengk.forum.common.api.Result;
import com.jayfengk.forum.common.api.ResultCode;
import com.jayfengk.forum.common.constant.SecurityConstants;
import com.jayfengk.forum.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 主設定
 * ============================================================
 * 設定重點：
 *   - 關掉 CSRF：JWT 是無狀態，不需要 CSRF
 *   - 關掉 session：每個 request 都靠 token 驗證
 *   - 把 JwtAuthenticationFilter 塞在 UsernamePasswordAuthenticationFilter 之前
 *     （這樣 JWT 驗過就不會走到內建的 form login）
 *   - 自訂 401 / 403 回應，回 JSON 而不是 Spring 預設的 HTML
 *
 * @EnableMethodSecurity 啟用方法層級註解（@PreAuthorize 等），雖然 MVP 沒用到
 *                       但先開著，未來加角色不用再改設定。
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 關掉 CSRF（JWT 無狀態 + 前端是 SPA，CSRF 機制不適用）
                .csrf(csrf -> csrf.disable())

                // CORS 啟用，實際規則在 CorsConfig
                .cors(cors -> {})

                // 完全無狀態，不維護 session
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 授權規則
                .authorizeHttpRequests(auth -> auth
                        // 公開端點：登入註冊、密碼重設、email 變更驗證、文章列表/單篇瀏覽、留言列表
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/verify-email-change").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/articles", "/api/articles/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/articles/*/comments").permitAll()
                        // 預檢請求 OPTIONS 一律放行（CORS 機制）
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 其他都要登入
                        .anyRequest().authenticated()
                )

                // 自訂 401 / 403 回應，回 JSON 不要 Spring 預設的 HTML
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint((req, res, ex) -> writeJson(res, 401, Result.failed(ResultCode.UNAUTHORIZED)))
                        .accessDeniedHandler((req, res, ex) -> writeJson(res, 403, Result.failed(ResultCode.FORBIDDEN)))
                )

                // 把 JWT filter 塞在預設的 username/password filter 前面
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 密碼加密器（BCrypt）。
     * strength 從 SecurityConstants 取，便於日後統一調整。
     * 一次設定，整個 app 都用這個 bean 來 encode / matches 密碼。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(SecurityConstants.BCRYPT_STRENGTH);
    }

    /**
     * 暴露 AuthenticationManager 成 bean，讓 AuthService 可以注入後呼叫
     * authenticate(...) 來做登入驗證。
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /** 把 Result 轉成 JSON 寫進 response */
    private void writeJson(jakarta.servlet.http.HttpServletResponse res, int status, Result<?> body) throws java.io.IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
