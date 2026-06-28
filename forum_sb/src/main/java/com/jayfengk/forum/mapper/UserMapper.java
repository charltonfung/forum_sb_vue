package com.jayfengk.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jayfengk.forum.entity.User;

/**
 * User mapper
 * ============================================================
 * BaseMapper<User> 自帶以下方法（不用自己寫 SQL）：
 *   - selectById(id)
 *   - selectOne(wrapper)            ← 用 LambdaQueryWrapper 組條件
 *   - selectList(wrapper)
 *   - selectPage(page, wrapper)     ← 分頁
 *   - insert(entity)
 *   - updateById(entity)
 *   - deleteById(id)
 *
 * 之後要寫自訂 SQL，可以在這裡加方法 + 寫 XML 或 @Select 註解。
 */
public interface UserMapper extends BaseMapper<User> {
}
