package com.jayfengk.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jayfengk.forum.entity.ArticleLike;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * ArticleLike mapper
 * ============================================================
 * 兩個自訂查詢用 @Select 註解直接寫 SQL（不需要 XML 檔）：
 *
 * 1. countByArticleIds  → 批次回傳「每篇文章的讚數」，給文章列表用
 *                         （避免 N+1：一次 SQL 拿一頁所有文章的讚數）
 *
 * 2. selectLikedArticleIds → 當前使用者在這批文章裡讚過哪些
 *                            前端能標出哪幾顆愛心要點亮
 *
 * 為什麼用 @Select 而不是 wrapper.groupBy?
 *   MyBatis-Plus 的 LambdaQueryWrapper 不太適合 GROUP BY + 多欄輸出，
 *   寫原生 SQL 反而清晰、可控。學 MyBatis 也要練手寫 SQL。
 */
public interface ArticleLikeMapper extends BaseMapper<ArticleLike> {

    /**
     * 批次查讚數
     * @return List of Map（每筆 {article_id: x, cnt: y}）
     */
    @Select("<script>" +
            "SELECT article_id AS articleId, COUNT(*) AS cnt " +
            "FROM article_likes " +
            "WHERE article_id IN " +
            "<foreach collection='articleIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> " +
            "GROUP BY article_id" +
            "</script>")
    List<Map<String, Object>> countByArticleIds(@Param("articleIds") List<Long> articleIds);

    /**
     * 查當前使用者讚過的 article_id（在這批文章裡面）
     */
    @Select("<script>" +
            "SELECT article_id FROM article_likes " +
            "WHERE user_id = #{userId} AND article_id IN " +
            "<foreach collection='articleIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    List<Long> selectLikedArticleIds(@Param("userId") Long userId,
                                     @Param("articleIds") List<Long> articleIds);
}
