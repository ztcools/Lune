package com.lune.service;

import com.lune.common.PageResult;
import com.lune.dto.CommentRequest;
import com.lune.entity.Comment;

public interface CommentService {
    PageResult<Comment> listByArticle(Long articleId, String type, Long sourceId, int page, int size);
    Comment createComment(CommentRequest request);
    void deleteComment(Long id);
}
