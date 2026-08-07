package com.lune.service;

import com.lune.common.PageResult;
import com.lune.dto.CommentRequest;
import com.lune.entity.Comment;

import java.util.Map;

public interface CommentService {
    PageResult<Comment> listByArticle(Long articleId, String type, Long sourceId, int page, int size);

    /**
     * 按目标统计评论数，返回 目标ID -> 条数。
     *
     * <p>加这个接口是为了替掉前端的一种通行写法：首页、随笔页都在拉一大页评论
     * （{@code size=500} / {@code size=1000}）到浏览器里自己 groupBy 计数。
     * 那样做既把全部评论正文传给了每个访客，又在评论数超过那个 size 之后
     * 开始静默少算 —— 页面不会报错，只是数字慢慢变得不对。计数下推到 SQL 后
     * 返回行数只与目标数量相关，与评论总量无关。
     *
     * @param type {@code null} 或 {@code "article"} 按 article_id 归并（文章评论），
     *             其余按 source_id 归并（随笔/许愿/树洞等）
     */
    Map<Long, Long> countByTarget(String type);
    Comment createComment(CommentRequest request);
    void deleteComment(Long id);
    void likeComment(Long id, int delta);
}
