package com.lune.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lune.entity.Wish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface WishMapper extends BaseMapper<Wish> {
    /** 原子更新点赞数，避免并发丢失 */
    @Update("UPDATE wish SET like_count = like_count + #{delta} WHERE id = #{id}")
    int updateLikeCount(Long id, long delta);
}
