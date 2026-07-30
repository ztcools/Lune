package com.lune.controller;

import com.lune.common.PageResult;
import com.lune.common.Result;
import com.lune.entity.Article;
import com.lune.service.ArticleService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public Result<PageResult<Article>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        return Result.success(articleService.listArticles(page, size, categoryId, keyword));
    }

    @GetMapping("/{id}")
    public Result<Article> getById(@PathVariable Long id) {
        return Result.success(articleService.getArticleById(id));
    }

    /**
     * 点赞 / 取消点赞。该接口允许匿名访问，因此 delta 必须收敛为 ±1：
     * 原实现直接把请求参数透传进 SQL 累加，任何人 ?delta=1000000 就能
     * 把点赞数刷成任意值（总点赞数还会展示在前台个人卡片上）。
     */
    @PatchMapping("/{id}/like")
    public Result<?> like(@PathVariable Long id, @RequestParam(defaultValue = "1") int delta) {
        articleService.updateLikeCount(id, delta >= 0 ? 1 : -1);
        return Result.success(null);
    }

    /** 获取所有文章的总点赞数（前台个人信息卡片展示用） */
    @GetMapping("/total-likes")
    public Result<Long> totalLikes() {
        return Result.success(articleService.getTotalLikes());
    }
}
