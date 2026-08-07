package com.lune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lune.common.BusinessException;
import com.lune.common.PageResult;
import com.lune.entity.Comment;
import com.lune.entity.Wish;
import com.lune.entity.WishLike;
import com.lune.mapper.CommentMapper;
import com.lune.mapper.UserMapper;
import com.lune.mapper.WishLikeMapper;
import com.lune.mapper.WishMapper;
import com.lune.service.WishService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class WishServiceImpl implements WishService {

    private final WishMapper wishMapper;
    private final WishLikeMapper wishLikeMapper;
    private final UserMapper userMapper;
    private final CommentMapper commentMapper;

    public WishServiceImpl(WishMapper wishMapper, WishLikeMapper wishLikeMapper,
                           UserMapper userMapper, CommentMapper commentMapper) {
        this.wishMapper = wishMapper;
        this.wishLikeMapper = wishLikeMapper;
        this.userMapper = userMapper;
        this.commentMapper = commentMapper;
    }

    private void populate(List<Wish> list, Long currentUserId) {
        if (list.isEmpty()) return;
        // 填充作者信息
        Set<Long> userIds = list.stream().map(Wish::getUserId)
                .filter(id -> id != null && id > 0).collect(Collectors.toSet());
        Map<Long, com.lune.entity.User> userMap = userIds.isEmpty() ? Map.of()
                : userMapper.selectBatchIds(userIds).stream()
                    .collect(Collectors.toMap(com.lune.entity.User::getId, Function.identity()));
        // 填充评论数：COUNT 下推到 SQL。原先是把这些许愿的评论整行捞回来在 JVM 里
        // groupingBy —— 只为了几个数字，把全部评论正文读进了内存。
        Set<Long> wishIds = list.stream().map(Wish::getId).collect(Collectors.toSet());
        var countQw = new QueryWrapper<Comment>();
        countQw.select("source_id AS target_id", "COUNT(*) AS c")
               .eq("type", "wish").in("source_id", wishIds).groupBy("source_id");
        Map<Long, Long> commentCount = new HashMap<>();
        for (Map<String, Object> row : commentMapper.selectMaps(countQw)) {
            if (row.get("target_id") instanceof Number id && row.get("c") instanceof Number c) {
                commentCount.put(id.longValue(), c.longValue());
            }
        }
        // 当前用户点赞状态
        Set<Long> likedIds = Set.of();
        if (currentUserId != null) {
            likedIds = wishLikeMapper.selectList(new LambdaQueryWrapper<WishLike>()
                    .eq(WishLike::getUserId, currentUserId)
                    .in(WishLike::getWishId, wishIds))
                    .stream().map(WishLike::getWishId).collect(Collectors.toSet());
        }
        for (Wish w : list) {
            var u = userMap.get(w.getUserId());
            if (u != null) {
                w.setUsername(u.getUsername());
                w.setNickname(u.getNickname());
                w.setAvatar(u.getAvatar());
            }
            w.setCommentCount(commentCount.getOrDefault(w.getId(), 0L));
            w.setLiked(likedIds.contains(w.getId()));
        }
    }

    @Override
    public PageResult<Wish> listWishes(int page, int size, Long currentUserId) {
        var wrapper = new LambdaQueryWrapper<Wish>()
                .eq(Wish::getStatus, 1)
                .orderByDesc(Wish::getLikeCount)
                .orderByDesc(Wish::getCreateTime);
        var result = wishMapper.selectPage(new Page<>(page, size), wrapper);
        populate(result.getRecords(), currentUserId);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public Wish create(Wish wish) {
        wish.setId(null);
        wish.setUserId(com.lune.security.SecurityUtils.getCurrentUserId());
        wish.setLikeCount(0L);
        wish.setStatus(1);
        wishMapper.insert(wish);
        return wish;
    }

    @Override
    public Wish update(Long id, Wish wish) {
        var exist = wishMapper.selectById(id);
        if (exist == null) throw new BusinessException(404, "心愿不存在");
        if (wish.getTitle() != null) exist.setTitle(wish.getTitle());
        if (wish.getContent() != null) exist.setContent(wish.getContent());
        if (wish.getStatus() != null) exist.setStatus(wish.getStatus());
        wishMapper.updateById(exist);
        return exist;
    }

    @Override
    public void delete(Long id) {
        wishMapper.deleteById(id);
        wishLikeMapper.delete(new LambdaQueryWrapper<WishLike>().eq(WishLike::getWishId, id));
    }

    @Override
    @Transactional
    public long toggleLike(Long wishId, Long userId) {
        if (userId == null) throw new BusinessException(401, "请先登录");
        var wish = wishMapper.selectById(wishId);
        if (wish == null) throw new BusinessException(404, "心愿不存在");
        var existLike = wishLikeMapper.selectOne(new LambdaQueryWrapper<WishLike>()
                .eq(WishLike::getWishId, wishId).eq(WishLike::getUserId, userId));
        if (existLike != null) {
            wishLikeMapper.deleteById(existLike.getId());
            wishMapper.updateLikeCount(wishId, -1);
        } else {
            var like = new WishLike();
            like.setWishId(wishId);
            like.setUserId(userId);
            wishLikeMapper.insert(like);
            wishMapper.updateLikeCount(wishId, 1);
        }
        var fresh = wishMapper.selectById(wishId);
        return fresh.getLikeCount();
    }
}
