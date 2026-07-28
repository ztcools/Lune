package com.lune.service;

import com.lune.common.PageResult;
import com.lune.entity.Wish;

public interface WishService {
    /** 按点赞数降序分页 */
    PageResult<Wish> listWishes(int page, int size, Long currentUserId);
    Wish create(Wish wish);
    Wish update(Long id, Wish wish);
    void delete(Long id);
    /** 点赞 / 取消点赞，返回最新点赞数 */
    long toggleLike(Long wishId, Long userId);
}
