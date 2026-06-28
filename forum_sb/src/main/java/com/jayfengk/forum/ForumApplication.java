package com.jayfengk.forum;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Forum 啟動類（整個應用程式的進入點）
 * ============================================================
 * @SpringBootApplication 是 3 個註解的合體：
 *   - @Configuration          這個類本身可以放 @Bean 定義
 *   - @EnableAutoConfiguration 看 classpath 自動裝配（看到 spring-web 就啟 Tomcat、
 *                              看到 mysql-connector 就準備 DataSource……）
 *   - @ComponentScan          掃描「同 package 與所有子 package」的 @Component
 *                              所以這個檔案放在 com.jayfengk.forum 是有意義的
 *                              ─ 子 package（controller/service/...）才會被掃到
 *
 * @MapperScan：
 *   告訴 MyBatis-Plus「去這個 package 找所有 interface，把它們當 Mapper 註冊成 Bean」。
 *   不寫的話 mapper 就要每個都加 @Mapper 標註，集中寫這裡比較乾淨。
 */
@SpringBootApplication
@MapperScan("com.jayfengk.forum.mapper")
public class ForumApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForumApplication.class, args);
    }
}
