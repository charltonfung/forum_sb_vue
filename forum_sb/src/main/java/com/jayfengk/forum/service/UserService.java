package com.jayfengk.forum.service;

import com.jayfengk.forum.dto.ChangePasswordRequest;
import com.jayfengk.forum.dto.RequestEmailChangeRequest;
import com.jayfengk.forum.dto.UpdateProfileRequest;
import com.jayfengk.forum.dto.UserDto;

/**
 * 使用者「自己編輯自己資料」相關操作
 * ============================================================
 * 把「個人資料」的操作從 AuthService 拆出來：
 *   - AuthService 管 register / login / forgot-password / reset-password（未登入流程）
 *   - UserService 管已登入使用者修改自己的資料（profile / password / email）
 */
public interface UserService {

    /** 改名稱（不需驗舊密碼，不會讓 JWT 失效） */
    UserDto updateProfile(Long currentUserId, UpdateProfileRequest req);

    /** 改密碼（驗舊密碼 + 推進 credentialsChangedAt → 舊 JWT 失效） */
    void changePassword(Long currentUserId, ChangePasswordRequest req);

    /** 申請改 email：驗舊密碼 + 生 token + 寄信到「新 email」。實際生效在 verify 那步。 */
    void requestEmailChange(Long currentUserId, RequestEmailChangeRequest req);

    /**
     * 驗證 email 變更（不需登入，由信件連結觸發）
     * 通過後：users.email 改成 new_email；推進 credentialsChangedAt → 舊 JWT 失效
     */
    void verifyEmailChange(String plainToken);
}
