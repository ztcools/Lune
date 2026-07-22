package com.lune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lune.common.PageResult;
import com.lune.dto.CommentRequest;
import com.lune.entity.Comment;
import com.lune.mapper.CommentMapper;
import com.lune.service.CommentService;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;

    public CommentServiceImpl(CommentMapper commentMapper) {
        this.commentMapper = commentMapper;
    }

    @Override
    public PageResult<Comment> listByArticle(Long articleId, int page, int size) {
        var wrapper = new LambdaQueryWrapper<Comment>()
                .eq(Comment::getStatus, 1)
                .orderByDesc(Comment::getCreateTime);
        if (articleId != null) {
            wrapper.eq(Comment::getArticleId, articleId);
        }
        var result = commentMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public Comment createComment(CommentRequest request) {
        var comment = new Comment();
        comment.setArticleId(request.getArticleId() != null ? request.getArticleId() : 0L);
        comment.setContent(request.getContent());
        comment.setUserId(1L);
        comment.setParentId(request.getParentId());
        comment.setReplyTo(request.getReplyTo());
        comment.setStatus(1);
        commentMapper.insert(comment);
        return comment;
    }

    @Override
    public void deleteComment(Long id) {
        commentMapper.deleteById(id);
    }
}
