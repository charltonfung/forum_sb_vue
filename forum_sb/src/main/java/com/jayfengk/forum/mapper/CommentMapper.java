package com.jayfengk.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jayfengk.forum.entity.Comment;

/**
 * Comment mapper
 * ============================================================
 * BaseMapper 提供 selectList(wrapper) / insert / deleteById 就夠了，
 * 不需要自訂 SQL。
 */
public interface CommentMapper extends BaseMapper<Comment> {
}
