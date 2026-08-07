package com.lune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lune.common.BusinessException;
import com.lune.common.PageResult;
import com.lune.dto.ArticleRequest;
import com.lune.entity.Article;
import com.lune.mapper.ArticleMapper;
import com.lune.security.JwtTokenProvider;
import com.lune.security.SecurityUtils;
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
            throw new BusinessException(404, "文章不存在");
        }
        // 原子更新阅读数（避免并发丢失）
        var updateWrapper = new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Article>()
                .eq(Article::getId, id)
                .setSql("view_count = view_count + 1");
        articleMapper.update(null, updateWrapper);
        article.setViewCount(article.getViewCount() + 1);
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
        article.setUserId(SecurityUtils.getCurrentUserId());
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
        if (request.getStatus() != null) {
            article.setStatus(request.getStatus());
        }
        articleMapper.updateById(article);
        return article;
    }

    @Override
    public void deleteArticle(Long id) {
        articleMapper.deleteById(id);
    }

    @Override
    public void updateLikeCount(Long id, int delta) {
        var updateWrapper = new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Article>()
                .eq(Article::getId, id)
                .setSql("like_count = GREATEST(0, COALESCE(like_count, 0) + " + delta + ")");
        articleMapper.update(null, updateWrapper);
    }

    @Override
    public Long getTotalLikes() {
        // 求和下推到 SQL，避免把每篇文章都取回内存再累加
        var qw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Article>();
        qw.select("IFNULL(SUM(like_count), 0) AS total");
        var rows = articleMapper.selectMaps(qw);
        if (rows.isEmpty() || rows.get(0).get("total") == null) return 0L;
        Object total = rows.get(0).get("total");
        return total instanceof Number n ? n.longValue() : 0L;
    }
}
