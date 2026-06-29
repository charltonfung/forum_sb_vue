package com.jayfengk.forum.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * JWT 驗證 filter（每個 request 進來都會檢查 Authorization header）
 * ============================================================
 * 繼承 OncePerRequestFilter：
 *   Spring 的 filter 在某些情況（forward / include）會被觸發兩次，
 *   OncePerRequestFilter 確保一個 request 只跑一次。
 *
 * 流程：
 *   1. 從 Authorization header 取出 "Bearer xxx"
 *   2. 切掉 prefix 拿到 token
 *   3. 解析 token 取 email
 *   4. 用 email 查 UserDetails
 *   5. 把 Authentication 塞進 SecurityContext（後續 controller 才能 @AuthenticationPrincipal 拿到）
 *
 * 注意：這個 filter 不負責「決定要不要擋」，只負責「能驗證就把使用者放進 context」。
 *      擋與不擋是 SecurityConfig 的事。
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProperties props;
    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String token = extractToken(request);
        if (token != null && tokenProvider.validate(token)) {
            String email = tokenProvider.getEmail(token);
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 把 loadUserByUsername 包起來：使用者改了 email 之後，舊 JWT 裡的 email
                // 在 DB 找不到 → UsernameNotFoundException。這時 token 本來就該失效，
                // 不要讓例外冒到 controller 變 500，靜靜走過去讓 Spring Security 回 401。
                UserDetails userDetails;
                try {
                    userDetails = userDetailsService.loadUserByUsername(email);
                } catch (Exception e) {
                    chain.doFilter(request, response);
                    return;
                }

                // ★ 失效水位線檢查：若 token.iat < user.credentials_changed_at，視為失效。
                //   這樣使用者改密碼 / 改 email 後，所有比那時間早簽出的 JWT 都會被拒。
                if (isStaleToken(token, userDetails)) {
                    chain.doFilter(request, response);
                    return; // 不放 Authentication → SecurityConfig 後續會回 401
                }

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(request, response);
    }

    /**
     * token.iat 早於使用者 credentialsChangedAt 就視為失效。
     * credentialsChangedAt = null 表示從沒改過密碼 / email，所有 token 都有效。
     */
    private boolean isStaleToken(String token, UserDetails userDetails) {
        if (!(userDetails instanceof AuthUser authUser)) return false;
        LocalDateTime credChangedAt = authUser.getUser().getCredentialsChangedAt();
        if (credChangedAt == null) return false;
        LocalDateTime iat = tokenProvider.getIssuedAt(token);
        // iat 拿不到代表 token 壞掉 — 算它失效比較安全
        return iat == null || iat.isBefore(credChangedAt);
    }

    /**
     * 從 "Authorization: Bearer xxx" 取出 token
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(props.getHeader());
        if (StringUtils.hasText(header) && header.startsWith(props.getPrefix())) {
            return header.substring(props.getPrefix().length());
        }
        return null;
    }
}
