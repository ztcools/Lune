package com.lune.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lune.entity.Tag;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {
}
