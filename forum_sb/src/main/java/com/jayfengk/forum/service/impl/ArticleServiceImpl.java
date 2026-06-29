package com.jayfengk.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jayfengk.forum.common.api.ResultCode;
import com.jayfengk.forum.common.exception.ApiException;
import com.jayfengk.forum.dto.ArticleDto;
import com.jayfengk.forum.dto.ArticleRequest;
import com.jayfengk.forum.dto.PageResult;
import com.jayfengk.forum.entity.Article;
import com.jayfengk.forum.entity.User;
import com.jayfengk.forum.mapper.ArticleLikeMapper;
import com.jayfengk.forum.mapper.ArticleMapper;
import com.jayfengk.forum.mapper.UserMapper;
import com.jayfengk.forum.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ArticleService 實作
 * ============================================================
 * 列表 N+1 處理（同樣思路用三次）：
 *   1. 一次撈所有作者 → Map<userId, User>
 *   2. 一次撈所有讚數 → Map<articleId, Long>
 *   3. 一次撈當前使用者讚過的 → Set<articleId>
 * 然後 stream 對映。一頁 10 篇文章 → 4 條 SQL（含主查詢），不會隨筆數爆增。
 */
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final ArticleLikeMapper articleLikeMapper;

    @Override
    public PageResult<ArticleDto> list(Long currentUserId, String keyword, Long authorUserId, long page, long pageSize) {
        Page<Article> pager = new Page<>(page, pageSize);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                // 有 keyword 才加 LIKE %?% 條件；MyBatis-Plus 的 like() 會自動 prepared-statement，
                // 不會 SQL injection。空字串視為「不搜尋」。
                .like(keyword != null && !keyword.isBlank(), Article::getTitle, keyword)
                // 有 authorUserId 才加 user_id = ? 條件（給「我的文章」用）
                .eq(authorUserId != null, Article::getUserId, authorUserId)
                .orderByDesc(Article::getId);
        Page<Article> result = articleMapper.selectPage(pager, wrapper);

        List<Article> articles = result.getRecords();
        Map<Long, User> authors = loadAuthors(articles);
        List<Long> articleIds = articles.stream().map(Article::getId).toList();
        Map<Long, Long> likeCounts = loadLikeCounts(articleIds);
        Set<Long> likedByMe = loadLikedByMe(currentUserId, articleIds);

        return PageResult.from(result, a -> ArticleDto.from(
                a,
                authors.get(a.getUserId()),
                likeCounts.getOrDefault(a.getId(), 0L),
                likedByMe.contains(a.getId())
        ));
    }

    @Override
    public ArticleDto get(Long currentUserId, Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new ApiException(ResultCode.NOT_FOUND, "文章不存在");
        }
        User author = userMapper.selectById(article.getUserId());
        long likeCount = loadLikeCounts(List.of(id)).getOrDefault(id, 0L);
        boolean likedByMe = loadLikedByMe(currentUserId, List.of(id)).contains(id);
        return ArticleDto.from(article, author, likeCount, likedByMe);
    }

    @Override
    @Transactional
    public ArticleDto create(Long currentUserId, ArticleRequest req) {
        Article article = new Article();
        article.setTitle(req.getTitle());
        article.setContent(req.getContent());
        article.setState("published");
        article.setUserId(currentUserId);
        articleMapper.insert(article);

        User author = userMapper.selectById(currentUserId);
        // 剛建立 → 0 讚、自己沒讚過
        return ArticleDto.from(article, author, 0L, false);
    }

    @Override
    @Transactional
    public ArticleDto update(Long currentUserId, Long id, ArticleRequest req) {
        Article article = ensureOwnedByCurrentUser(currentUserId, id);
        article.setTitle(req.getTitle());
        article.setContent(req.getContent());
        articleMapper.updateById(article);

        User author = userMapper.selectById(currentUserId);
        long likeCount = loadLikeCounts(List.of(id)).getOrDefault(id, 0L);
        boolean likedByMe = loadLikedByMe(currentUserId, List.of(id)).contains(id);
        return ArticleDto.from(article, author, likeCount, likedByMe);
    }

    @Override
    @Transactional
    public void delete(Long currentUserId, Long id) {
        ensureOwnedByCurrentUser(currentUserId, id);
        articleMapper.deleteById(id);
    }

    // ------------------------------------------------------------
    // private helpers
    // ------------------------------------------------------------

    private Article ensureOwnedByCurrentUser(Long currentUserId, Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new ApiException(ResultCode.NOT_FOUND, "文章不存在");
        }
        if (!Objects.equals(article.getUserId(), currentUserId)) {
            throw new ApiException(ResultCode.FORBIDDEN, "沒有權限操作此文章");
        }
        return article;
    }

    private Map<Long, User> loadAuthors(List<Article> articles) {
        if (articles.isEmpty()) return new HashMap<>();
        List<Long> userIds = articles.stream().map(Article::getUserId).distinct().toList();
        List<User> users = userMapper.selectBatchIds(userIds);
        return users.stream().collect(Collectors.toMap(User::getId, u -> u));
    }

    /** 把 mapper 回的 List<Map> 轉成 Map<articleId, Long> */
    private Map<Long, Long> loadLikeCounts(List<Long> articleIds) {
        if (articleIds.isEmpty()) return new HashMap<>();
        List<Map<String, Object>> rows = articleLikeMapper.countByArticleIds(articleIds);
        Map<Long, Long> map = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Long articleId = ((Number) row.get("articleId")).longValue();
            Long cnt = ((Number) row.get("cnt")).longValue();
            map.put(articleId, cnt);
        }
        return map;
    }

    private Set<Long> loadLikedByMe(Long currentUserId, List<Long> articleIds) {
        if (currentUserId == null || articleIds.isEmpty()) return new HashSet<>();
        return new HashSet<>(articleLikeMapper.selectLikedArticleIds(currentUserId, articleIds));
    }
}
