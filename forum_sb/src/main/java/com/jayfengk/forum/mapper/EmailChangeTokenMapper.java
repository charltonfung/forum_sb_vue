package com.jayfengk.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jayfengk.forum.entity.EmailChangeToken;

/**
 * email_change_tokens mapper
 * ============================================================
 * 用法：
 *   - 申請改 email：deleteById(userId) 清舊 token → insert 新 token
 *   - verify：selectOne(token=hash) 找紀錄、檢查時效、改完即 deleteById
 *
 * 跟 PasswordResetTokenMapper 一樣不加 @Mapper：ForumApplication 已用
 * @MapperScan("com.jayfengk.forum.mapper") 全掃。
 */
public interface EmailChangeTokenMapper extends BaseMapper<EmailChangeToken> {
}
