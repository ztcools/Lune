package com.lune.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lune.common.PageResult;
import com.lune.dto.ArticleRequest;
import com.lune.entity.Article;

public interface ArticleService {
    PageResult<Article> listArticles(int page, int size, Long categoryId, String keyword);
    Article getArticleById(Long id);
    Article createArticle(ArticleRequest request);
    Article updateArticle(Long id, ArticleRequest request);
    void deleteArticle(Long id);
}
