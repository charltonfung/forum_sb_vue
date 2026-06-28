package com.jayfengk.forum.security;

import com.jayfengk.forum.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Spring Security 用的「使用者實作」（UserDetails 介面的具體類）
 * ============================================================
 * 為什麼不讓 User entity 直接 implements UserDetails？
 *   1. entity 只該管「DB 的對應」，不該管 security 的 contract
 *   2. 之後若加 ban / lock / 角色，會直接污染 entity
 *   3. UserDetails 的 getUsername() 概念跟 entity 的 name 容易搞混
 *
 * 用 getter 暴露 User 物件，controller 可以從 SecurityContext 取出後直接拿到 entity。
 */
@Getter
public class AuthUser implements UserDetails {

    private final User user;

    public AuthUser(User user) {
        this.user = user;
    }

    public Long getId() {
        return user.getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // MVP 不做角色權限，全部使用者都同等
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        // Spring Security 的 "username" 概念 = 唯一登入識別。我們用 email。
        return user.getEmail();
    }

    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }
}
