package com.jayfengk.forum.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jayfengk.forum.entity.User;
import com.jayfengk.forum.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security 用來「依 email 查使用者」的服務
 * ============================================================
 * 登入流程：
 *   1. AuthController 收到 email + password
 *   2. authenticationManager.authenticate(...) 觸發
 *   3. Spring 用「這個類」依 email 查到 User，再用 PasswordEncoder 比對密碼
 *   4. 對的話回傳 AuthUser，錯的話丟 BadCredentialsException
 */
@Service
@RequiredArgsConstructor // Lombok 自動產生 constructor，把 final 欄位注入
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (user == null) {
            // 訊息不要寫「使用者不存在」，避免被攻擊者拿來列舉帳號
            throw new UsernameNotFoundException("帳號或密碼錯誤");
        }
        return new AuthUser(user);
    }
}
