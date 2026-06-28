package com.jayfengk.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jayfengk.forum.entity.CommentLike;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * CommentLike mapper（用法同 ArticleLikeMapper，只是目標換成 comment）
 */
public interface CommentLikeMapper extends BaseMapper<CommentLike> {

    @Select("<script>" +
            "SELECT comment_id AS commentId, COUNT(*) AS cnt " +
            "FROM comment_likes " +
            "WHERE comment_id IN " +
            "<foreach collection='commentIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> " +
            "GROUP BY comment_id" +
            "</script>")
    List<Map<String, Object>> countByCommentIds(@Param("commentIds") List<Long> commentIds);

    @Select("<script>" +
            "SELECT comment_id FROM comment_likes " +
            "WHERE user_id = #{userId} AND comment_id IN " +
            "<foreach collection='commentIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    List<Long> selectLikedCommentIds(@Param("userId") Long userId,
                                     @Param("commentIds") List<Long> commentIds);
}
