package com.lune.service.impl;

import com.lune.common.BusinessException;
import com.lune.entity.Wish;
import com.lune.entity.WishLike;
import com.lune.mapper.CommentMapper;
import com.lune.mapper.UserMapper;
import com.lune.mapper.WishLikeMapper;
import com.lune.mapper.WishMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link WishServiceImpl} 点赞/更新核心逻辑测试（Mockito 隔离数据库）。
 */
@ExtendWith(MockitoExtension.class)
class WishServiceImplTest {

    @Mock
    private WishMapper wishMapper;
    @Mock
    private WishLikeMapper wishLikeMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private CommentMapper commentMapper;

    private WishServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new WishServiceImpl(wishMapper, wishLikeMapper, userMapper, commentMapper);
    }

    private Wish wish(long id, long likeCount) {
        Wish w = new Wish();
        w.setId(id);
        w.setLikeCount(likeCount);
        return w;
    }

    @Test
    void toggleLikeAddsLikeWhenNotLiked() {
        // 第一次 selectById 取到当前心愿，第二次取到点赞后的最新值
        when(wishMapper.selectById(10L)).thenReturn(wish(10L, 5L), wish(10L, 6L));
        when(wishLikeMapper.selectOne(any())).thenReturn(null);

        long result = service.toggleLike(10L, 7L);

        assertThat(result).isEqualTo(6L);
        verify(wishLikeMapper).insert(any(WishLike.class));
        verify(wishMapper).updateLikeCount(10L, 1);
        verify(wishLikeMapper, never()).deleteById(anyLong());
    }

    @Test
    void toggleLikeRemovesLikeWhenAlreadyLiked() {
        WishLike existing = new WishLike();
        existing.setId(99L);
        existing.setWishId(10L);
        existing.setUserId(7L);

        when(wishMapper.selectById(10L)).thenReturn(wish(10L, 6L), wish(10L, 5L));
        when(wishLikeMapper.selectOne(any())).thenReturn(existing);

        long result = service.toggleLike(10L, 7L);

        assertThat(result).isEqualTo(5L);
        verify(wishLikeMapper).deleteById(99L);
        verify(wishMapper).updateLikeCount(10L, -1);
        verify(wishLikeMapper, never()).insert(any(WishLike.class));
    }

    @Test
    void toggleLikeRequiresLogin() {
        assertThatThrownBy(() -> service.toggleLike(10L, null))
                .isInstanceOf(BusinessException.class)
                .hasMessage("请先登录");
    }

    @Test
    void toggleLikeThrowsWhenWishNotFound() {
        when(wishMapper.selectById(404L)).thenReturn(null);
        assertThatThrownBy(() -> service.toggleLike(404L, 7L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("心愿不存在");
    }

    @Test
    void updateThrowsWhenWishNotFound() {
        when(wishMapper.selectById(404L)).thenReturn(null);
        Wish patch = new Wish();
        patch.setTitle("新标题");
        assertThatThrownBy(() -> service.update(404L, patch))
                .isInstanceOf(BusinessException.class)
                .hasMessage("心愿不存在");
    }
}
