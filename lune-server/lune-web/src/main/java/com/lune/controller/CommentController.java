package com.lune.controller;

import com.lune.common.PageResult;
import com.lune.common.Result;
import com.lune.dto.CommentRequest;
import com.lune.entity.Comment;
import com.lune.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public Result<PageResult<Comment>> list(@RequestParam(required = false) Long articleId,
                                            @RequestParam(required = false) String type,
                                            @RequestParam(required = false) Long sourceId,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return Result.success(commentService.listByArticle(articleId, type, sourceId, page, size));
    }

    /**
     * 评论数统计 GET /api/comments/counts?type=article
     *
     * <p>返回 目标ID -> 评论数。供列表页一次性拿齐各条目的评论数，
     * 替代「拉一大页评论到前端自己数」的老办法（见 CommentService#countByTarget）。
     */
    @GetMapping("/counts")
    public Result<Map<Long, Long>> counts(@RequestParam(required = false) String type) {
        return Result.success(commentService.countByTarget(type));
    }

    @PostMapping
    public Result<Comment> create(@Valid @RequestBody CommentRequest request) {
        return Result.success(commentService.createComment(request));
    }
}
