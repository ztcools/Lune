package com.lune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lune.common.PageResult;
import com.lune.dto.CommentRequest;
import com.lune.entity.Comment;
import com.lune.mapper.CommentMapper;
import com.lune.mapper.UserMapper;
import com.lune.service.CommentService;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;

    public CommentServiceImpl(CommentMapper commentMapper, UserMapper userMapper) {
        this.commentMapper = commentMapper;
        this.userMapper = userMapper;
    }

    private Long getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Claims claims) {
            return claims.get("userId", Long.class);
        }
        return null;
    }

    private void populateUserInfo(java.util.List<Comment> comments) {
        Set<Long> userIds = comments.stream()
                .map(Comment::getUserId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) return;
        var users = userMapper.selectBatchIds(userIds);
        Map<Long, com.lune.entity.User> userMap = users.stream()
                .collect(Collectors.toMap(com.lune.entity.User::getId, Function.identity()));
        for (Comment c : comments) {
            var u = userMap.get(c.getUserId());
            if (u != null) {
                c.setUsername(u.getUsername());
                c.setNickname(u.getNickname());
                c.setAvatar(u.getAvatar());
            }
        }
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
        populateUserInfo(result.getRecords());
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public Comment createComment(CommentRequest request) {
        Long currentUserId = getCurrentUserId();
        var comment = new Comment();
        comment.setArticleId(request.getArticleId() != null ? request.getArticleId() : 0L);
        comment.setType(request.getType());
        comment.setSourceId(request.getSourceId());
        comment.setContent(request.getContent());
        comment.setUserId(currentUserId != null ? currentUserId : 0L);
        comment.setParentId(request.getParentId());
        comment.setReplyTo(request.getReplyTo());
        comment.setStatus(1);
        commentMapper.insert(comment);
        if (currentUserId != null) {
            var user = userMapper.selectById(currentUserId);
            if (user != null) {
                comment.setUsername(user.getUsername());
                comment.setNickname(user.getNickname());
                comment.setAvatar(user.getAvatar());
            }
        }
        return comment;
    }

    @Override
    public void deleteComment(Long id) {
        commentMapper.deleteById(id);
    }
}
