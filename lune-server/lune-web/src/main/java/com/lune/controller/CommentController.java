package com.lune.controller;

import com.lune.common.PageResult;
import com.lune.common.Result;
import com.lune.dto.CommentRequest;
import com.lune.entity.Comment;
import com.lune.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public Result<PageResult<Comment>> list(@RequestParam(required = false) Long articleId,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return Result.success(commentService.listByArticle(articleId, page, size));
    }

    @PostMapping
    public Result<Comment> create(@Valid @RequestBody CommentRequest request) {
        return Result.success(commentService.createComment(request));
    }
}
