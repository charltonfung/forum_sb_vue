package com.jayfengk.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jayfengk.forum.common.api.ResultCode;
import com.jayfengk.forum.common.exception.ApiException;
import com.jayfengk.forum.dto.CommentDto;
import com.jayfengk.forum.entity.Article;
import com.jayfengk.forum.entity.Comment;
import com.jayfengk.forum.entity.User;
import com.jayfengk.forum.mapper.ArticleMapper;
import com.jayfengk.forum.mapper.CommentLikeMapper;
import com.jayfengk.forum.mapper.CommentMapper;
import com.jayfengk.forum.mapper.UserMapper;
import com.jayfengk.forum.service.CommentService;
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
 * CommentService 實作
 * ============================================================
 * 列表 N+1 處理同 ArticleService — 一次撈作者 / 讚數 / 當前使用者讚過的。
 *
 * 權限規則：
 *   - 列表：任何人可看
 *   - 新增：需登入
 *   - 刪除：只有留言作者可刪
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final CommentLikeMapper commentLikeMapper;

    @Override
    public List<CommentDto> listByArticle(Long currentUserId, Long articleId) {
        List<Comment> comments = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getArticleId, articleId)
                        .orderByAsc(Comment::getId));

        if (comments.isEmpty()) return List.of();

        Map<Long, User> authors = loadAuthors(comments);
        List<Long> commentIds = comments.stream().map(Comment::getId).toList();
        Map<Long, Long> likeCounts = loadLikeCounts(commentIds);
        Set<Long> likedByMe = loadLikedByMe(currentUserId, commentIds);

        return comments.stream()
                .map(c -> CommentDto.from(
                        c,
                        authors.get(c.getUserId()),
                        likeCounts.getOrDefault(c.getId(), 0L),
                        likedByMe.contains(c.getId())))
                .toList();
    }

    @Override
    @Transactional
    public CommentDto create(Long currentUserId, Long articleId, String content) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new ApiException(ResultCode.NOT_FOUND, "文章不存在");
        }

        Comment comment = new Comment();
        comment.setArticleId(articleId);
        comment.setUserId(currentUserId);
        comment.setContent(content);
        commentMapper.insert(comment);

        User author = userMapper.selectById(currentUserId);
        // 剛建立 → 0 讚、自己沒讚過
        return CommentDto.from(comment, author, 0L, false);
    }

    @Override
    @Transactional
    public void delete(Long currentUserId, Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new ApiException(ResultCode.NOT_FOUND, "留言不存在");
        }
        if (!Objects.equals(comment.getUserId(), currentUserId)) {
            throw new ApiException(ResultCode.FORBIDDEN, "沒有權限刪除此留言");
        }
        commentMapper.deleteById(commentId);
    }

    private Map<Long, User> loadAuthors(List<Comment> comments) {
        if (comments.isEmpty()) return new HashMap<>();
        List<Long> userIds = comments.stream().map(Comment::getUserId).distinct().toList();
        List<User> users = userMapper.selectBatchIds(userIds);
        return users.stream().collect(Collectors.toMap(User::getId, u -> u));
    }

    private Map<Long, Long> loadLikeCounts(List<Long> commentIds) {
        if (commentIds.isEmpty()) return new HashMap<>();
        List<Map<String, Object>> rows = commentLikeMapper.countByCommentIds(commentIds);
        Map<Long, Long> map = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Long commentId = ((Number) row.get("commentId")).longValue();
            Long cnt = ((Number) row.get("cnt")).longValue();
            map.put(commentId, cnt);
        }
        return map;
    }

    private Set<Long> loadLikedByMe(Long currentUserId, List<Long> commentIds) {
        if (currentUserId == null || commentIds.isEmpty()) return new HashSet<>();
        return new HashSet<>(commentLikeMapper.selectLikedCommentIds(currentUserId, commentIds));
    }
}
