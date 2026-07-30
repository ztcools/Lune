package com.lune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lune.common.PageResult;
import com.lune.dto.CommentRequest;
import com.lune.entity.Comment;
import com.lune.mapper.CommentMapper;
import com.lune.service.CommentService;
import com.lune.service.support.UserInfoFiller;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final UserInfoFiller userInfoFiller;

    public CommentServiceImpl(CommentMapper commentMapper, UserInfoFiller userInfoFiller) {
        this.commentMapper = commentMapper;
        this.userInfoFiller = userInfoFiller;
    }

    private Long getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Claims claims) {
            return claims.get("userId", Long.class);
        }
        return null;
    }

    @Override
    public PageResult<Comment> listByArticle(Long articleId, String type, Long sourceId, int page, int size) {
        var wrapper = new LambdaQueryWrapper<Comment>()
                .eq(Comment::getStatus, 1)
                .orderByDesc(Comment::getCreateTime);
        if (articleId != null && articleId > 0) {
            wrapper.eq(Comment::getArticleId, articleId);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Comment::getType, type);
        }
        if (sourceId != null && sourceId > 0) {
            wrapper.eq(Comment::getSourceId, sourceId);
        }
        var result = commentMapper.selectPage(new Page<>(page, size), wrapper);
        userInfoFiller.fill(result.getRecords());
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public Map<Long, Long> countByTarget(String type) {
        // 文章评论用 article_id 归并，其余（随笔/许愿/树洞）用 source_id。
        // 判别文章不看 type：历史数据里文章评论的 type 有 null 也有 'article'，
        // 用 article_id > 0 才和前端原有的口径一致。
        boolean byArticle = type == null || type.isBlank() || "article".equals(type);
        String keyColumn = byArticle ? "article_id" : "source_id";

        var qw = new QueryWrapper<Comment>();
        qw.select(keyColumn + " AS target_id", "COUNT(*) AS c")
          .eq("status", 1)
          .groupBy(keyColumn);
        if (byArticle) {
            qw.gt("article_id", 0);
        } else {
            qw.eq("type", type).gt("source_id", 0);
        }

        var counts = new HashMap<Long, Long>();
        for (Map<String, Object> row : commentMapper.selectMaps(qw)) {
            Object id = row.get("target_id");
            Object c = row.get("c");
            if (id instanceof Number n && c instanceof Number cn) {
                counts.put(n.longValue(), cn.longValue());
            }
        }
        return counts;
    }

    @Override
    public Comment createComment(CommentRequest request) {
        Long currentUserId = getCurrentUserId();
        var comment = new Comment();
        comment.setArticleId(request.getArticleId() != null ? request.getArticleId() : 0L);
        comment.setType(request.getType());
        comment.setSourceId(request.getSourceId());
        // XSS 清洗：评论为纯文本，转义 HTML 特殊字符防存储型 XSS
        comment.setContent(com.lune.security.XssSanitizer.clean(request.getContent(), 1000));
        comment.setReplyTo(request.getReplyTo());
        comment.setUserId(currentUserId != null ? currentUserId : 0L);
        comment.setParentId(request.getParentId());
        comment.setReplyTo(request.getReplyTo());
        comment.setStatus(1);
        commentMapper.insert(comment);
        userInfoFiller.fillOne(comment);
        return comment;
    }

    @Override
    public void deleteComment(Long id) {
        commentMapper.deleteById(id);
    }
}
