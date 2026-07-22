package com.lune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lune.common.BusinessException;
import com.lune.common.PageResult;
import com.lune.dto.ArticleRequest;
import com.lune.entity.Article;
import com.lune.mapper.ArticleMapper;
import com.lune.security.JwtTokenProvider;
import com.lune.service.ArticleService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final JwtTokenProvider jwtTokenProvider;

    public ArticleServiceImpl(ArticleMapper articleMapper, JwtTokenProvider jwtTokenProvider) {
        this.articleMapper = articleMapper;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public PageResult<Article> listArticles(int page, int size, Long categoryId, String keyword) {
        var wrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, 1)
                .orderByDesc(Article::getIsTop)
                .orderByDesc(Article::getCreateTime);
        if (categoryId != null) {
            wrapper.eq(Article::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Article::getTitle, keyword).or().like(Article::getSummary, keyword));
        }
        var result = articleMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public Article getArticleById(Long id) {
        var article = articleMapper.selectById(id);
        if (article == null || article.getStatus() != 1) {
            throw new BusinessException("文章不存在");
        }
        article.setViewCount(article.getViewCount() + 1);
        articleMapper.updateById(article);
        return article;
    }

    @Override
    public Article createArticle(ArticleRequest request) {
        var article = new Article();
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setSummary(request.getSummary());
        article.setCover(request.getCover());
        article.setCategoryId(request.getCategoryId());
        article.setUserId(1L);
        article.setStatus(1);
        articleMapper.insert(article);
        return article;
    }

    @Override
    public Article updateArticle(Long id, ArticleRequest request) {
        var article = articleMapper.selectById(id);
        if (article == null) throw new BusinessException("文章不存在");
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setSummary(request.getSummary());
        article.setCover(request.getCover());
        article.setCategoryId(request.getCategoryId());
        articleMapper.updateById(article);
        return article;
    }

    @Override
    public void deleteArticle(Long id) {
        articleMapper.deleteById(id);
    }
}
