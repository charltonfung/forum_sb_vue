package com.jayfengk.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jayfengk.forum.entity.PasswordResetToken;

/**
 * 密碼重設 token mapper
 * ============================================================
 * 用法：
 *   - 寄送重設信前：deleteById(email) 清掉舊 token
 *   - 接著 insert 新 token
 *   - 驗證重設：selectById(email) 比對 token + 檢查時效
 *   - 重設成功後：deleteById(email)
 */
public interface PasswordResetTokenMapper extends BaseMapper<PasswordResetToken> {
}
