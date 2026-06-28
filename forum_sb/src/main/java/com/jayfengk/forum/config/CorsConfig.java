package com.jayfengk.forum.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * CORS 跨域設定
 * ============================================================
 * Vue 前端跑在 http://localhost:5173，後端跑在 http://localhost:8090，
 * 不同 port = 跨域，必須在後端開白名單瀏覽器才會放行。
 *
 * 允許的來源從 application.yml 的 forum.cors.allowed-origins 讀，
 * 部署時改 yml 或環境變數即可，不用改程式碼。
 */
@Configuration
@ConfigurationProperties(prefix = "forum.cors")
@Data
public class CorsConfig {

    private List<String> allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));   // 前端要能讀到 Authorization
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);                              // 預檢 cache 1 小時

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
